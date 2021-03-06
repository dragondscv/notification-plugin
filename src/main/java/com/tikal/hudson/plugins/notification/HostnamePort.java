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

import java.util.Scanner;
import java.util.regex.MatchResult;

public class HostnamePort {

  final public String hostname;

  final public int port;

  public HostnamePort(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  static public HostnamePort parseUrl(String url) {
    try {
      Scanner scanner = new Scanner(url);
      scanner.findInLine("(.+):(\\d{1,5})");
      MatchResult result = scanner.match();
      if (result.groupCount() != 2) {
        return null;
      }
      String hostname = result.group(1);
      int port = Integer.valueOf(result.group(2));
      return new HostnamePort(hostname, port);
    } catch (Exception e) {
      return null;
    }
  }
}
