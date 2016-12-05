package com.github.klousiaj.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DockerRuleBuilder {
  private final DockerRuleParams params = new DockerRuleParams();

  static final String DEFAULT_LABEL_KEY = "com.github.klousiaj.creator";
  static final String DEFAULT_LABEL_VALUE = "docker-junit-rule";

  private static final String LABEL_PATTERN_STR = "[a-zA-Z0-9\\.-]+:{1}.+";
  static final Pattern LABEL_PATTERN = Pattern.compile(LABEL_PATTERN_STR);


  /**
   * @param imageName The name of the docker image to download
   * @return The builder
   */
  public DockerRuleBuilder image(String imageName) {
    params.imageName = imageName;
    return this;
  }

  /**
   * @param containerName the name specified for the container
   * @return the builder
   */
  public DockerRuleBuilder containerName(String containerName) {
    params.containerName = containerName;
    return this;
  }

  /**
   * <p>
   * List the container ports that you would like to open in the container.
   * There are three ways to specify a port:
   * <ul>
   * <li>443 - this will map the CONTAINER port (443) to a random port on the HOST</li>
   * <li>:443 - this will map the CONTAINER port (443) to the same port on the HOST (443)</li>
   * <li>8443:443 - this will map the CONTAINER port (443) to the specified HOST port (8443)</li>
   * </ul>
   * <p>
   * To know which ports are used on your host use DockerRule#getHostPort(String).
   * Example:
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

  /**
   * Leave the container running rather than shutting down and removing the
   * container.
   * <p>
   * <b>Note</b> - use this feature with caution. If misused, test cases may
   * have inconsistent results depending on the value of this parameter. Depending on
   * your use case, a better solution may be to use a JUnit Test @Suite.
   *
   * @param leaveRunning true if the container should be left running after test execution
   * @return the builder
   */
  public DockerRuleBuilder leaveRunning(boolean leaveRunning) {
    params.leaveRunning = leaveRunning;
    return this;
  }

  /**
   * If a container of the requested image/version is already running on the requested
   * port an attempt will be made to use that container.
   * <p>
   * <b>Note</b> - use this feature with caution. If misused, test cases may
   * have inconsistent results depending on the value of this parameter. Depending on
   * your use case, a better solution may be to use a JUnit Test @Suite.
   *
   * @param useRunning true if a running container should be used if found
   * @return the builder
   */
  public DockerRuleBuilder useRunning(boolean useRunning) {
    params.useRunning = useRunning;
    return this;
  }

  /**
   * Allow the user to specify whether a volume associated with the requested image
   * should be removed as part of the cleanup.
   * <p>
   * <b>Note</b> - this is defaulted to false to ensure backward compatibility. This is likely
   * not the desired default behavior. In the future this functionality may be changed to default
   * to true.
   *
   * @param cleanVolumes true if volumes should be removed
   * @return the builder
   */
  public DockerRuleBuilder cleanVolumes(boolean cleanVolumes) {
    params.cleanVolumes = cleanVolumes;
    return this;
  }

  /**
   * A convenience method that allows for labels to be specified as concatenated strings in the form:
   * <code>key:value</code>.
   * <p>
   * Must match the PATTERN: [a-zA-Z0-9\\.-]+:{1}.+
   *
   * @param labels the key:value pairs to be applied to the created container.
   * @return the builder
   * @throws IllegalArgumentException if the strings cannot be parsed to match a key-value pair
   */
  public DockerRuleBuilder labels(String... labels) throws IllegalArgumentException {
    if (labels != null) {
      Map<String, String> labelMap = new HashMap<>();
      for (String label : labels) {
        if (!LABEL_PATTERN.matcher(label).matches()) {
          throw new IllegalArgumentException();
        }
        String[] split = label.split(":");
        labelMap.put(split[0], split[1]);
      }
      if (labelMap.size() > 0) {
        return labels(labelMap);
      }
    }
    return this;
  }

  /**
   * Specify labels to associate with this container. By default, the label com.github.klousiaj.creator:docker-junit-rule
   * will be added. Attempts to override this will be ignored.
   *
   * @param labels the labels to add
   * @return the builder
   */
  protected DockerRuleBuilder labels(Map<String, String> labels) {
    params.labels = labels;
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
  @Deprecated
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
    return new DockerRule(params).initialize();
  }

}
