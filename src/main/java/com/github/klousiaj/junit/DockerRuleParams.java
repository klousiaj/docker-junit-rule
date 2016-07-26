package com.github.klousiaj.junit;

class DockerRuleParams {

  String imageName;

  String[] ports;
  String[] envs;
  String cmd;

  boolean useRunning = false;
  boolean leaveRunning = false;

  String portToWaitOn;
  public int waitTimeout;
  String logToWait;
}
