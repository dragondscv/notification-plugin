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

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("job")
public class JobState {

  private String name;

  private String url;

  // add more fields
  private String upstreamJobName;

  private String description;

  private String displayName;

  private String fullDisplayName;

  private BuildState build;

  private TestResultState testResultState;

  public String getUpstreamJobName() {
    return upstreamJobName;
  }

  public void setUpstreamJobName(String upstreamJobName) {
    this.upstreamJobName = upstreamJobName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  // add getter and setter for newly added fields
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName= displayName;  
  }

  public String getFullDisplayName() {
    return fullDisplayName;
  }

  public void setFullDisplayName(String fullDisplayName) {
    this.fullDisplayName = fullDisplayName;
  }


  public BuildState getBuild() {
    return build;
  }

  public void setBuild(BuildState build) {
    this.build = build;
  }

  public TestResultState getTestResultState() {
    return testResultState;
  }

  public void setTestResultState(TestResultState testResultState) {
    this.testResultState = testResultState;
  }
}
