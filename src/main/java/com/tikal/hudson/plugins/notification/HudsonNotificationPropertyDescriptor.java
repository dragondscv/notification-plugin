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

import hudson.Extension;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Job;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public final class HudsonNotificationPropertyDescriptor extends JobPropertyDescriptor {

  public HudsonNotificationPropertyDescriptor() {
    super(HudsonNotificationProperty.class);
    load();
  }

  private List<Endpoint> endpoints = new ArrayList<Endpoint>();
  private int maxLinesForLog = 100;
  private boolean enableLog = false;
  private boolean enableTestResult = false;

  public boolean isEnabled() {
    return !endpoints.isEmpty();
  }

  public int getMaxLinesForLog() {
    return maxLinesForLog;
  }

  public boolean getEnableLog() {
    return enableLog;
  }

  public boolean getEnableTestResult() {
    return enableTestResult;
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }

  @Override
  public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends Job> jobType) {
    return true;
  }

  public String getDisplayName() {
    return "Hudson Job Notification";
  }

  @Override
  public HudsonNotificationProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {

    List<Endpoint> endpoints = new ArrayList<Endpoint>();
    int maxLinesForLog = 100;

    if (formData != null && !formData.isNullObject()) {

      maxLinesForLog = formData.getInt("maxLinesForLog");

      JSON endpointsData = (JSON) formData.get("endpoints");
      if (endpointsData != null && !endpointsData.isEmpty()) {
        if (endpointsData.isArray()) {
          JSONArray endpointsArrayData = (JSONArray) endpointsData;
          endpoints.addAll(req.bindJSONToList(Endpoint.class, endpointsArrayData));
        } else {
          JSONObject endpointsObjectData = (JSONObject) endpointsData;
          endpoints.add(req.bindJSON(Endpoint.class, endpointsObjectData));
        }
      }
    }
    HudsonNotificationProperty notificationProperty = new HudsonNotificationProperty(endpoints, maxLinesForLog);
    return notificationProperty;
  }

  public FormValidation doCheckUrl(@QueryParameter(value = "url", fixEmpty = true) String url, @QueryParameter(value = "protocol") String protocolParameter) {
    Protocol protocol = Protocol.valueOf(protocolParameter);
    try {
      protocol.validateUrl(url);
      return FormValidation.ok();
    } catch (Exception e) {
      return FormValidation.error(e.getMessage());
    }
  }

  // check if maxLinesForLog is a valid number
  public FormValidation doCheckMaxLinesForLog(@QueryParameter String value) {
    try {
      Integer.parseInt(value);
      return FormValidation.ok();
    } catch (NumberFormatException e) {
      return FormValidation.error("Not a number");
    }
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) throws FormException{

    if (formData != null && !formData.isNullObject()) {

      // check if test result is enabled
      if(req.getParameter("enableTestResult") == null) {
        enableTestResult = false;
      } else {
        enableTestResult = true;
      }

      // check if log is enabled
      if(req.getParameter("enableLog") == null) {
        enableLog = false;
      } else {
        enableLog = true;
      }

      if (enableLog) {
        maxLinesForLog = Integer.parseInt(formData.getJSONObject("enableLog").getString("maxLinesForLog"));
      }

      endpoints.clear();

      JSON endpointsData = (JSON) formData.get("endpoints");

      if (endpointsData != null && !endpointsData.isEmpty()) {
        if (endpointsData.isArray()) {
          JSONArray endpointsArrayData = (JSONArray) endpointsData;
          endpoints.addAll(req.bindJSONToList(Endpoint.class, endpointsArrayData));
        } else {
          JSONObject endpointsObjectData = (JSONObject) endpointsData;
          endpoints.add(req.bindJSON(Endpoint.class, endpointsObjectData));
        }
      }
    }

    save();
    //return true;
    return super.configure(req,formData);
  }
}
