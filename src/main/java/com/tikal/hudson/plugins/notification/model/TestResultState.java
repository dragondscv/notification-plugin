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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.TestResult;

public class TestResultState {

  private String title;
  private int totalCount;
  private int skipCount;
  private int passCount;
  private int failedCount;
  private float duration;
  private List<CaseResultState> failedTests;
  private List<CaseResultState> passedTests;

  public TestResultState() {}

  public TestResultState(TestResult tr) {
    title = tr.getTitle();
    totalCount = tr.getTotalCount();
    skipCount = tr.getSkipCount();
    passCount = tr.getPassCount();
    failedCount = tr.getFailCount();
    duration = tr.getDuration();
    setFailedTestsFromCaseResults(tr.getFailedTests());
    setPassedTestsFromCaseResults(tr.getChildren());
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setSkipCount(int skipCount) {
    this.skipCount = skipCount;
  }

  public int getSkipCount() {
    return skipCount;
  }

  public void setPassCount(int passCount) {
    this.passCount = passCount;
  }

  public int getPassCount() {
    return passCount;
  }

  public void setFailedCount(int failedCount) {
    this.failedCount = failedCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }

  public float getDuration() {
    return duration;
  }

  public void setFailedTestsFromCaseResults(List<CaseResult> failedTests) {
    List<CaseResultState> myCaseResults = new ArrayList<CaseResultState>();

    for (CaseResult caseResult : failedTests) {
      CaseResultState crs = new CaseResultState();
      crs.setTitle(caseResult.getTitle());
      crs.setDuration(caseResult.getDuration());
      myCaseResults.add(crs);
    }

    this.failedTests = myCaseResults;
  }

  public void setFailedTests(List<CaseResultState> failedTests) {
    this.failedTests = failedTests;
  }

  public List<CaseResultState> getFailedTests() {
    return failedTests;
  }

  public void setPassedTestsFromCaseResults(Collection<PackageResult> packageResults) {
    // traverse each PackageResult to get passedTests because TestResult has not implmented getPassedTests()
    List<CaseResultState> myCaseResults = new ArrayList<CaseResultState>();

    for (PackageResult pr : packageResults) {
      List<CaseResult> caseResults = (List<CaseResult>) pr.getPassedTests();

      for (CaseResult caseResult : caseResults) {
        CaseResultState crs = new CaseResultState();
        crs.setTitle(caseResult.getTitle());
        crs.setDuration(caseResult.getDuration());
        myCaseResults.add(crs);
      }
    }

    this.passedTests = myCaseResults;
  }

  public void setPassedTests(List<CaseResultState> passedTests) {
    this.passedTests = passedTests;
  }

  public List<CaseResultState> getPassedTests() {
    return passedTests;
  }

}
