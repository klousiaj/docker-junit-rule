package com.github.geowarin.junit;

public class DockerRuleBuilder {
  private final DockerRuleParams params = new DockerRuleParams();

  /**
   * @param imageName The name of the docker image to download
   * @return The builder
   */
  public DockerRuleBuilder image(String imageName) {
    params.imageName = imageName;
    return this;
  }

  /**
   * <p>
   * List the container ports that you would like to open in the container.
   * There are three ways to specify a port:
   * <ul>
   *     <li>443 - this will map the CONTAINER port (443) to a random port on the HOST</li>
   *     <li>:443 - this will map the CONTAINER port (443) to the same port on the HOST (443)</li>
   *     <li>8443:443 - this will map the CONTAINER port (443) to the specified HOST port (8443)</li>
   * </ul>
   * </p>
   * <p>
   * To know which ports are used on your host use DockerRule#getHostPort(String).
   * Example:
   * </p>
   * <pre>
   * {@code
   * myRule.getHostPort("80/tcp")
   * }
   * </pre>
   *
   * @param ports The ports to open on the container
   * @return The builder
   * @see DockerRule#getHostPort(String)
   */
  public DockerRuleBuilder ports(String... ports) {
    params.ports = ports;
    return this;
  }

  /**
   * <p>
   * The environment variables to be passed to the container.
   * </p>
   * <pre>
   *     {@code
   *      MYSQL_ROOT_PASSWORD=my_secret
   *     }
   * </pre>
   *
   * @param envs The environments variables to pass to the container.
   * @return The builder
   */
  public DockerRuleBuilder envs(String... envs) {
    params.envs = envs;
    return this;
  }

  public DockerRuleBuilder cmd(String cmd) {
    params.cmd = cmd;
    return this;
  }

  /**
   * Utility method to ensure a container is started
   *
   * @param portToWaitOn The port to wait on
   * @return The builder
   */
  public DockerRuleBuilder waitForPort(String portToWaitOn) {
    return waitForPort(portToWaitOn, 10000);
  }

  /**
   * Utility method to ensure a container is started
   *
   * @param portToWaitOn    The port to wait on
   * @param timeoutInMillis Maximum waiting time in milliseconds
   * @return The builder
   */
  public DockerRuleBuilder waitForPort(String portToWaitOn, int timeoutInMillis) {
    params.portToWaitOn = portToWaitOn;
    params.waitTimeout = timeoutInMillis;
    return this;
  }

  /**
   * @param logToWait The log message to wait.
   *                  This will stop blocking as soon as the docker logs contains that string
   * @return The builder
   */
  public DockerRuleBuilder waitForLog(String logToWait) {
    params.logToWait = logToWait;
    return this;
  }

  public DockerRule build() {
    return new DockerRule(params);
  }
}
