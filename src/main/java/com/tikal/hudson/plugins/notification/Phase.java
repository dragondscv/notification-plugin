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

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.TestResult;

import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.tikal.hudson.plugins.notification.model.BuildState;
import com.tikal.hudson.plugins.notification.model.JobState;
import com.tikal.hudson.plugins.notification.model.TestResultState;
import com.tikal.hudson.plugins.notification.model.CaseResultState;



@SuppressWarnings({ "unchecked", "rawtypes" })
public enum Phase {
  STARTED {

    @Override
    public void handle(Run run, TaskListener listener) {
    }

  },
  COMPLETED {

    @Override
    public void handle(Run run, TaskListener listener) {

      // use property if config.jelly is used
      //HudsonNotificationProperty property = (HudsonNotificationProperty) run.getParent().getProperty(HudsonNotificationProperty.class);

      // use descriptor directly if global.jelly is used
      HudsonNotificationPropertyDescriptor descriptor = (HudsonNotificationPropertyDescriptor)
            run.getParent().getDescriptorByName(HudsonNotificationProperty.class.getName());

      if (descriptor != null) {
        List<Endpoint> targets = descriptor.getEndpoints();
        for (Endpoint target : targets) {
          try {
            JobState jobState = buildJobState(run.getParent(), run, descriptor.getMaxLinesForLog(), 
                  descriptor.getEnableLog(), descriptor.getEnableTestResult());
            Protocol protocol = target.getProtocol();

            switch (protocol) {
              case JEDIS:
                String nodeName = run.getExecutor().getOwner().getHostName();
                String jobName = jobState.getName();
                int buildNumber = jobState.getBuild().getNumber();
                //protocol.send(target.getUrl(), target.getFormat().toString(jobState),
                protocol.send(target.getUrl(), jobState, target.getFormat().toString(jobState),
                    nodeName, jobName, buildNumber);
                break;
              default:
                protocol.send(target.getUrl(), target.getFormat().toString(jobState));
                break;
            }
          } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to notify "+target));
          } catch (InterruptedException e) {
            System.out.println(e);
          }
        }
      } else {
        System.out.println("descriptor is null.");
      }
    }


  },
  FINISHED {

    @Override
    public void handle(Run run, TaskListener listener) {
    }

  };

  abstract public void handle(Run run, TaskListener listener);

  public JobState buildJobState(Job job, Run run, int maxLinesForLog, boolean enableLog, boolean enableTestResult) {

    JobState jobState = new JobState();
    jobState.setName(job.getName());
    jobState.setUrl(job.getUrl());

    if (job.getDescription() == null) {
      // jedis does not accept null, insert empty string instead
      jobState.setDescription("");
    }
    else {
      jobState.setDescription(job.getDescription());
    }
    //jobState.setDisplayName(job.getDisplayName());
    //jobState.setFullDisplayName(job.getFullDisplayName());

    BuildState buildState = new BuildState();
    buildState.setNumber(run.number);
    buildState.setUrl(run.getUrl());
    buildState.setPhase(this);
    buildState.setStatus(getStatus(run));

    if (enableLog) {
      try {
        // only call run.getLog() if maxLinesForLog is great than 0, otherwise it will throw
        // exception
        if (maxLinesForLog > 0) {
          buildState.setLog(run.getLog(maxLinesForLog));
        }
      }
      catch (IOException e) {
        System.out.println(e);
      }
    } else {
      System.out.println("Sending log is not enabled. Do not send log.");
    }
    buildState.setDuration(run.getDuration());
    // disable getStartTimeInMillis() because Jenkins 1.424 does not support it
    //buildState.setStartTimeInMillis(run.getStartTimeInMillis());
    buildState.setTimeInMillis(run.getTimeInMillis());
    buildState.setTime(run.getTime());

    if (run.getDescription() == null)
    {
      // jedis does not accept null, insert empty string instead
      buildState.setDescription("");
    }
    else {
      buildState.setDescription(run.getDescription());
    }

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

    // get upstream jobs/builds and downstream jobs/builds
    String upstreamJobName = "";
    int upstreamBuildNumber = -1;

    if (job instanceof AbstractProject) {
      AbstractProject project = (AbstractProject) job;
      AbstractBuild build = (AbstractBuild) run;
      List<AbstractProject> upProjects = project.getUpstreamProjects();

      if (!upProjects.isEmpty()) {
        AbstractProject upProject = (AbstractProject) upProjects.get(0);

        upstreamJobName = upProject.getName();
        upstreamBuildNumber = build.getUpstreamRelationship(upProject);
      }

      jobState.setUpstreamJobName(upstreamJobName);
      buildState.setUpstreamBuildNumber(upstreamBuildNumber);


      if (enableTestResult) {
        // get test result
        AbstractTestResultAction absTestResultAct = build.getTestResultAction();
        if (absTestResultAct != null && absTestResultAct instanceof TestResultAction) {
          TestResultState trs = new TestResultState(((TestResultAction) absTestResultAct).getResult());
          jobState.setTestResultState(trs);
        }
      }

    } else {
      System.out.println("not a abstract project.");
    }


    return jobState;
  }

  private String getStatus(Run r) {
    Result result = r.getResult();
    String status = null;
    if (result != null) {
      status = result.toString();
    }
    return status;
  }
}
