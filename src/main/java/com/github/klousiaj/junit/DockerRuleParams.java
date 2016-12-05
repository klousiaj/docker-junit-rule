package com.github.klousiaj.junit;

import java.util.Map;

class DockerRuleParams {

  String imageName;
  String containerName;

  String[] ports;
  String[] envs;
  Map<String,String> labels;
  String cmd;

  boolean useRunning = false;
  boolean leaveRunning = false;
  boolean cleanVolumes = false;

  @Deprecated
  String portToWaitOn;
  public int waitTimeout;
  String logToWait;
}
