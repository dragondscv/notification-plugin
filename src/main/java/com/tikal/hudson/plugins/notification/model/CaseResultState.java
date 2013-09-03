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

public class CaseResultState {

  private String title;
  private float duration;   // milliseconds

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }
  public float getDuration() {
    return duration;
  }
}
