/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tikal.hudson.plugins.notification;


import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tikal.hudson.plugins.notification.model.BuildState;
import com.tikal.hudson.plugins.notification.model.JobState;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;

import redis.clients.jedis.Connection;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@SuppressWarnings("rawtypes")
public enum Protocol {

  UDP {
    @Override
    protected void send(String url, String strdata) throws IOException {
            byte[] data = strdata.getBytes();
            HostnamePort hostnamePort = HostnamePort.parseUrl(url);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(hostnamePort.hostname), hostnamePort.port);
            socket.send(packet);
    }

    @Override
    public void validateUrl(String url) {
      try {
        HostnamePort hnp = HostnamePort.parseUrl(url);
        if (hnp == null) {
          throw new Exception();
        }
      } catch (Exception e) {
        throw new RuntimeException("Invalid Url: hostname:port");
      }
    }
  },
  TCP {
    @Override
    protected void send(String url, String strdata) throws IOException {
            byte[] data = strdata.getBytes();
            HostnamePort hostnamePort = HostnamePort.parseUrl(url);
            SocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(hostnamePort.hostname), hostnamePort.port);
            Socket socket = new Socket();
            socket.connect(endpoint);
            OutputStream output = socket.getOutputStream();
            output.write(data);
            output.flush();
            output.close();
    }
  },
  HTTP {
    @Override
    protected void send(String url, String strdata) throws IOException {
            byte[] data = strdata.getBytes();
            URL targetUrl = new URL(url);
            if (!targetUrl.getProtocol().startsWith("http")) {
              throw new IllegalArgumentException("Not an http(s) url: " + url);
            }

            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            String userInfo = targetUrl.getUserInfo();
            if (null != userInfo) {
              String b64UserInfo = DatatypeConverter.printBase64Binary(userInfo.getBytes());
              String authorizationHeader = "Basic " + b64UserInfo;
              connection.setRequestProperty("Authorization", authorizationHeader);
            }
            connection.setFixedLengthStreamingMode(data.length);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.connect();
            try {
              OutputStream output = connection.getOutputStream();
              try {
                output.write(data);
                output.flush();
              } finally {
                output.close();
              }
            } finally {
              // Follow an HTTP Temporary Redirect if we get one,
              //
              // NB: Normally using the HttpURLConnection interface, we'd call
              // connection.setInstanceFollowRedirects(true) to enable 307 redirect following but
              // since we have the connection in streaming mode this does not work and we instead
              // re-direct manually.
              if (307 == connection.getResponseCode()) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                send(location, strdata);
              } else {
                connection.disconnect();
              }
            }
    }

    public void validateUrl(String url) {
      try {
        new URL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException("Invalid Url: http://hostname:port/path");
      }
    }
  },
  JEDIS {
    @Override
    protected void send(String url, String data) throws IOException {
    }

    // this method is only used by JEDIS protocol
    @Override
    protected void send(String url, JobState jobState, String data, String nodeName, String jobName, int
        buildNumber) throws IOException {
            HostnamePort hostnamePort = HostnamePort.parseUrl(url);
            BuildState buildState = jobState.getBuild();

            JedisPool pool = new JedisPool(new JedisPoolConfig(), hostnamePort.hostname, hostnamePort.port);
            Jedis jedis = pool.getResource();
            try {
              String nodeKey = nodeName;
              String jobKey = nodeName+":"+jobName;
              String buildKey = nodeName+":"+jobName+":"+buildNumber;

              // add keys to sets
              jedis.sadd("hosts", nodeKey);
              jedis.sadd("jobs", jobKey);
              jedis.sadd("builds", buildKey);

              // add job keys for each host
              jedis.sadd(nodeKey, jobKey);

              // add build keys for each job
              jedis.sadd(jobKey, buildKey);

              // add builds for each job
              HashMap<String, String> mymap = new HashMap<String, String>();
              mymap.put("host_name", nodeName);
              mymap.put("job_name", jobState.getName());
              mymap.put("description", jobState.getDescription());
              mymap.put("build_number", String.valueOf(buildState.getNumber()));
              mymap.put("build_description", buildState.getDescription());
              mymap.put("build_duration", String.valueOf(buildState.getDuration()));
              mymap.put("build_time_in_millis",
                  String.valueOf(buildState.getTimeInMillis()));
              mymap.put("build_time", buildState.getTime().toString());
              mymap.put("build_start_time_in_millis", String.valueOf(buildState.getStartTimeInMillis()));
              //mymap.put("build_log", buildState.getLog());
              mymap.put("build_phase", buildState.getPhase().toString());
              mymap.put("build_status", buildState.getStatus());
              mymap.put("build_url", buildState.getUrl());
              mymap.put("upstream_job_name", jobState.getUpstreamJobName());
              mymap.put("upstream_build_number", String.valueOf(buildState.getUpstreamBuildNumber()));

              Gson gson = new GsonBuilder().setFieldNamingPolicy(
                  FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
              mymap.put("test_result", gson.toJson(jobState.getTestResultState()));
              jedis.hmset(buildKey, mymap);

              // add to sorted sets for sorting
              jedis.zadd("sort:build_duration", buildState.getDuration(), buildKey);
              jedis.zadd("sort:build_time_in_millis", buildState.getTimeInMillis(), buildKey);
              jedis.zadd("sort:build_start_time_in_millis", buildState.getStartTimeInMillis(), buildKey);
              jedis.zadd("sort:"+buildState.getStatus(), buildState.getTimeInMillis(), buildKey);

              // publish to channel. channel name is nodeName:jobName
              jedis.publish(jobKey, data);

              System.out.println(data);
            } finally {
              // it's important to return the Jedis instance to the pool once you've finished using it
              pool.returnResource(jedis);
            }
            // when closing your application:
            pool.destroy();
    }

    @Override
    public void validateUrl(String url) {
      try {
        HostnamePort hnp = HostnamePort.parseUrl(url);
        if (hnp == null) {
          throw new Exception();
        }
      } catch (Exception e) {
        throw new RuntimeException("Invalid Url: hostname:port");
      }
    }
  };


  private Gson gson = new GsonBuilder().setFieldNamingPolicy(
      FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  public void sendNotification(String url, Job job, Run run, Phase phase, String status) throws IOException {
    send(url, buildMessage(job, run, phase, status));
  }

  private String buildMessage(Job job, Run run, Phase phase, String status) {
    JobState jobState = new JobState();
    jobState.setName(job.getName());
    jobState.setUrl(job.getUrl());
    BuildState buildState = new BuildState();
    buildState.setNumber(run.number);
    buildState.setUrl(run.getUrl());
    buildState.setPhase(phase);
    buildState.setStatus(status);
    buildState.setDisplayName(run.getDisplayName());

    String rootUrl = Hudson.getInstance().getRootUrl();
    if (rootUrl != null) {
      buildState.setFullUrl(rootUrl + run.getUrl());
    }

    jobState.setBuild(buildState);

    ParametersAction paramsAction = run.getAction(ParametersAction.class);
    if (paramsAction != null && run instanceof AbstractBuild) {
      AbstractBuild build = (AbstractBuild) run;
      EnvVars env = new EnvVars();
      for (ParameterValue value : paramsAction.getParameters())
        if (!value.isSensitive())
          value.buildEnvVars(build, env);
      buildState.setParameters(env);
    }

    return gson.toJson(jobState);
  }

  abstract protected void send(String url, String strdata) throws IOException;
  protected void send(String url, JobState JobState, String data, String nodeName, String jobName, int 
      buildNumber) throws IOException {}

  public void validateUrl(String url) {
    try {
      HostnamePort hnp = HostnamePort.parseUrl(url);
      if (hnp == null) {
        throw new Exception();
      }
    } catch (Exception e) {
      throw new RuntimeException("Invalid Url: hostname:port");
    }
  }
}
