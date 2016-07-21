package com.github.klousiaj.junit;

public class DockerRuleParams {

  String imageName;

  String[] ports;
  String[] envs;
  String cmd;

  String portToWaitOn;
  public int waitTimeout;
  String logToWait;
}
