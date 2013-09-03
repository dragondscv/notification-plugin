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
package com.tikal.hudson.plugins.notification.model;

import com.tikal.hudson.plugins.notification.Phase;

import java.util.Map;
import java.util.List;
import java.util.Date;

public class BuildState {

  private String fullUrl;

  private int number;

  private Phase phase;

  private String status;

  private String url;

  private String displayName;

  // add new fields
  private List<String> log;
  private long duration;  // the running time
  private long timeInMillis; // the scheduled time in milliseconds
  private Date time;  // the scheduled time in Date format
  private long startTimeInMillis; // the time the job start running
  private String description; // the description of the build (run)
  private int upstreamBuildNumber;

  private Map<String, String> parameters;

  // add getter and setter for new fields
  public List<String> getLog() {
    return log;
  }

  public void setLog(List<String> log) {
    this.log = log;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getTimeInMillis() {
    return timeInMillis;
  }

  public void setTimeInMillis(long timeInMillis) {
    this.timeInMillis = timeInMillis;
  }

  public int getUpstreamBuildNumber() {
    return upstreamBuildNumber;
  }

  public void setUpstreamBuildNumber(int upstreamBuildNumber) {
    this.upstreamBuildNumber = upstreamBuildNumber;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public long getStartTimeInMillis() {
    return startTimeInMillis;
  }

  public void setStartTimeInMillis(long startTimeInMillis) {
    this.startTimeInMillis = startTimeInMillis;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public Phase getPhase() {
    return phase;
  }

  public void setPhase(Phase phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFullUrl() {
    return fullUrl;
  }

  public void setFullUrl(String fullUrl) {
    this.fullUrl = fullUrl;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> params) {
    this.parameters = params;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
