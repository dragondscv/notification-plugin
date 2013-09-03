notification plugin
===================================================

The open source notification plugin - https://wiki.jenkins-ci.org/display/JENKINS/Notification+Plugin

Plugin for creating generic events from Jenkins and send them to Redis

Third party library included: Jedis - https://github.com/xetorthio/jedis

To start Jenkins server, run "mvn hpi:run"

Configure the plugin:

  1. Go to "Manage Jenkins"=>"Configure System"

  2. In the configuration page, go to "Job Notifications" section.

  3. Add Endpoint:

    3.1 Format: default is JSON, but it does not matter if JEDIS is chosen as the
    protocol because JEDIS will write data into multiple sets in Redis.

    3.2 Protocol: JEDIS.

    3.3 URL: the URL points to Redis server, the format is "host_name:port_number".

    3.4 Max Lines for Log: the maximum lines in log file that will be sent.

  4. Click "Save".
