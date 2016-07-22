package com.github.klousiaj.junit;

import com.spotify.docker.client.*;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.spotify.docker.client.DockerClient.LogsParam.follow;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;

/**
 * <p>
 * JUnit rule starting a docker container before the test and killing it
 * afterwards.
 * </p>
 * <p>
 * Uses spotify/docker-client.
 * Adapted from https://gist.github.com/mosheeshel/c427b43c36b256731a0b
 * </p>
 * author: Geoffroy Warin (geowarin.github.io)
 */
public class DockerRule extends ExternalResource {
  protected final Log logger = LogFactory.getLog(getClass());
  public static final String DOCKER_MACHINE_SERVICE_URL = "https://192.168.99.100:2376";
  public static final String PORT_MAPPING_REGEX = "^([\\d]{2,5})?(:\\d{2,5})?$";
  private static Pattern pattern = Pattern.compile(DockerRule.PORT_MAPPING_REGEX);

  private final DockerClient dockerClient;
  private ContainerCreation container;
  private Map<String, List<PortBinding>> ports;
  private DockerRuleParams params;

  public static DockerRuleBuilder builder() {
    return new DockerRuleBuilder();
  }

  protected DockerRule() {
    dockerClient = createDockerClient();
  }

  DockerRule(DockerRuleParams params) {


    this.params = params;
    dockerClient = createDockerClient();
    try {
      ContainerConfig containerConfig = createContainerConfig(params.imageName,
        params.ports, params.envs, params.cmd);

      dockerClient.pull(params.imageName);
      container = dockerClient.createContainer(containerConfig);
    } catch (DockerException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    dockerClient.startContainer(container.id());
    ContainerInfo info = dockerClient.inspectContainer(container.id());
    ports = info.networkSettings().ports();

    if (params.portToWaitOn != null) {
      waitForPort(getHostPort(params.portToWaitOn), params.waitTimeout);
    }

    if (params.logToWait != null) {
      waitForLog(params.logToWait);
    }
  }

  @Override
  protected void after() {
    super.after();
    try {
      dockerClient.killContainer(container.id());
      dockerClient.removeContainer(container.id());
      dockerClient.close();
    } catch (DockerException | InterruptedException e) {
      throw new RuntimeException("Unable to stop/remove docker container " + container.id(), e);
    }
  }

  /**
   * Utility method to get the docker host.
   * Can be different from localhost if using docker-machine
   *
   * @return The current docker host
   */
  public String getDockerHost() {
    return dockerClient.getHost();
  }

  public void waitForPort(int port, long timeoutInMillis) {
    SocketAddress address = new InetSocketAddress(getDockerHost(), port);
    long totalWait = 0;
    while (true) {
      try {
        SocketChannel.open(address);
        return;
      } catch (IOException e) {
        try {
          Thread.sleep(100);
          totalWait += 100;
          if (totalWait > timeoutInMillis) {
            throw new IllegalStateException("Timeout while waiting for port " + port);
          }
        } catch (InterruptedException ie) {
          throw new IllegalStateException(ie);
        }
      }
    }
  }

  private DockerClient createDockerClient() {
    if (isUnix() || System.getenv("DOCKER_HOST") != null) {
      try {
        return DefaultDockerClient.fromEnv().build();
      } catch (DockerCertificateException e) {
        System.err.println(e.getMessage());
      }
    }

    logger.info("Could not create docker client from the environment. Assuming docker-machine environment with url " + DOCKER_MACHINE_SERVICE_URL);
    DockerCertificates dockerCertificates = null;
    try {
      String userHome = System.getProperty("user.home");
      dockerCertificates = new DockerCertificates(Paths.get(userHome, ".docker/machine/certs"));
    } catch (DockerCertificateException e) {
      System.err.println(e.getMessage());
    }
    return DefaultDockerClient.builder()
      .uri(URI.create(DOCKER_MACHINE_SERVICE_URL))
      .dockerCertificates(dockerCertificates)
      .build();
  }

  protected ContainerConfig createContainerConfig(String imageName, String[] ports, String[] envs, String cmd) throws DockerException {
    Map<String, List<PortBinding>> portBinding = generatePortBinding(ports);
    HostConfig hostConfig = HostConfig.builder()
      .portBindings(portBinding)
      .build();

    ContainerConfig.Builder configBuilder = ContainerConfig.builder()
      .hostConfig(hostConfig)
      .image(imageName)
      .env(envs)
      .networkDisabled(false)
      .exposedPorts(portBinding.keySet());

    if (cmd != null) {
      configBuilder = configBuilder.cmd(cmd);
    }
    return configBuilder.build();
  }

  protected static Map<String, List<PortBinding>> generatePortBinding(String... ports) throws DockerException {

    Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : ports) {
      // check to see if the port is able to be parsed
      if ("".equals(port) || !pattern.matcher(port).matches())
        throw new DockerException("Invalid port mapping. Was empty or did not match regex /" + PORT_MAPPING_REGEX + "/g. Unable to process " + port);

      // it will be random port if there is not a semi-colon included
      PortBinding binding = PortBinding.randomPort("0.0.0.0");
      String[] split = port.split(":");
      if (split.length > 1) {
        // clean up the container port description
        port = split[1];
        binding = "".equals(split[0]) ? PortBinding.of("0.0.0.0", split[1]) : PortBinding.of("0.0.0.0", split[0]);
      }
      List<PortBinding> hostPorts = Collections.singletonList(binding);
      portBindings.put(port, hostPorts);
    }
    return portBindings;
  }


  public int getHostPort(String containerPort) {
    List<PortBinding> portBindings = ports.get(containerPort);
    if (portBindings == null || portBindings.isEmpty()) {
      return -1;
    }
    return Integer.parseInt(portBindings.get(0).hostPort());
  }

  private static boolean isUnix() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux") || os.contains("aix");
  }

  protected void waitForLog(String messageToMatch) throws DockerException, InterruptedException, UnsupportedEncodingException {
    LogStream logs = dockerClient.logs(container.id(), follow(), stdout());
    String log;
    do {
      LogMessage logMessage = logs.next();
      ByteBuffer buffer = logMessage.content();
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      log = new String(bytes);
    } while (!log.contains(messageToMatch));
  }
}