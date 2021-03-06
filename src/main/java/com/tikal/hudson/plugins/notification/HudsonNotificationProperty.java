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

import hudson.model.JobProperty;
import hudson.model.AbstractProject;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

public class HudsonNotificationProperty extends
    JobProperty<AbstractProject<?, ?>> {

  final public List<Endpoint> endpoints;
  final public int maxLinesForLog;

  @DataBoundConstructor
  public HudsonNotificationProperty(List<Endpoint> endpoints, int maxLinesForLog) {
    this.endpoints = endpoints;
    this.maxLinesForLog = maxLinesForLog;
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public HudsonNotificationPropertyDescriptor getDescriptor() {
    return (HudsonNotificationPropertyDescriptor) super.getDescriptor();
  }

  public int getMaxLinesForLog() {
    return maxLinesForLog;
  }
}
