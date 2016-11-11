package com.github.klousiaj.junit;

import com.spotify.docker.client.*;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
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
import java.util.*;
import java.util.regex.Matcher;
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

  DockerRuleParams params;

  private final DockerClient dockerClient;
  private ContainerCreation container;
  private Map<String, List<PortBinding>> ports;

  public static DockerRuleBuilder builder() {
    return new DockerRuleBuilder();
  }

  DockerRule(DockerClient client) {
    this.dockerClient = client;
  }

  DockerRule(DockerRuleParams params) {
    this.params = params;
    dockerClient = createDockerClient();
  }

  public DockerRule initialize() {
    try {
      try {
        // try to use a local copy of the image if one exists
        ImageInfo imageInfo = dockerClient.inspectImage(params.imageName);
      } catch (ImageNotFoundException e) {
        logger.info("Unable to find the requested image locally. Will attempt to pull from docker hub.");
        dockerClient.pull(params.imageName);
      }
    } catch (DockerException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
    return this;
  }

  @Override
  protected void before() throws Throwable {
    super.before();

    // attach to a running container, or create and start one
    attachToContainer();

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
    if (!params.leaveRunning) {
      try {
        try {
          dockerClient.killContainer(getContainer().id());
        } catch (DockerException | InterruptedException e) {
          logger.error("Unable to stop docker container " + getContainer().id(), e);
          logger.info("Will attempt to remove container anyway.");
        }
        dockerClient.removeContainer(getContainer().id());
      } catch (DockerException | InterruptedException e) {
        throw new RuntimeException("Unable to remove docker container " + getContainer().id(), e);
      } finally {
        dockerClient.close();
      }
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

  /**
   * set the container for the test run by either attaching to an already running
   * container or by creating and starting a brand new one.
   *
   * @throws DockerException
   * @throws InterruptedException
   */
  void attachToContainer() throws DockerException, InterruptedException {
    Map<String, List<PortBinding>> portBinding = generatePortBinding(params.ports);
    String containerId = null;
    if (params.useRunning) {
      containerId = foundRunningContainer(portBinding);
    }
    if (containerId == null) {
      // configure the container based on the provided parameters
      ContainerConfig containerConfig = createContainerConfig(params.imageName,
        portBinding, params.envs, params.cmd);
      // create the container
      container = dockerClient.createContainer(containerConfig);
      dockerClient.startContainer(getContainer().id());
    } else {
      logger.warn("Connecting to an already running container (" + containerId + "). Please note this is not the default behavior and should only be used by advanced users.");
      this.container = new ContainerCreation(containerId);
    }

    ContainerInfo info = dockerClient.inspectContainer(getContainer().id());
    ports = info.networkSettings().ports();
  }

  /**
   * Search for an already running container and set the container object if one is found
   *
   * @return true if a container with the same image:tag/port is already running
   */
  String foundRunningContainer(Map<String, List<PortBinding>> bindings) throws DockerException, InterruptedException {
    for (Container instanceContainers : dockerClient.listContainers()) {
      // only consider running containers of the same image.
      if (params.imageName.equals(instanceContainers.image()) && "running".equals(instanceContainers.state())) {
        // the requested ports count must be less than or equal to the existing containers exposed port count
        if (instanceContainers.ports().size() >= bindings.size()) {
          boolean portsAlign = true;
          Iterator<String> iter = bindings.keySet().iterator();
          while (iter.hasNext() && portsAlign) {
            String key = iter.next();
            portsAlign = this.validPortMap(key, bindings.get(key).get(0).hostPort(), instanceContainers.ports());
          }
          if (portsAlign) {
            return instanceContainers.id();
          }
        }
      }
    }
    return null;
  }

  boolean validPortMap(String privatePort, String publicPort, List<Container.PortMapping> containerMapping) {
    boolean mappingFound = false;
    for (int ii = 0; ii < containerMapping.size() && !mappingFound; ii++) {
      String containerPort = String.valueOf(containerMapping.get(ii).getPrivatePort());
      String hostPort = String.valueOf(containerMapping.get(ii).getPublicPort());
      if (containerPort.equals(privatePort) && ("".equals(publicPort) || publicPort.equals(hostPort))) {
        mappingFound = true;
      }
    }
    return mappingFound;
  }

  /**
   * Wait for the requested port to be accessible.
   *
   * @param port the port number
   * @param timeoutInMillis the time to wait before failing
   */
  @Deprecated
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

  protected DockerClient createDockerClient() {
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

  protected ContainerConfig createContainerConfig(String imageName, Map<String, List<PortBinding>> portBinding,
                                                  String[] envs, String cmd) throws DockerException {
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

      // it will be random port if there is not a colon included
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
    return os.contains("nix") || os.contains("nux") || os.contains("aix") || os.equals("mac os x");
  }

  protected void waitForLog(String messageToMatch) throws DockerException, InterruptedException, UnsupportedEncodingException {
    LogStream logs = dockerClient.logs(getContainer().id(), follow(), stdout());
    String log;
    do {
      LogMessage logMessage = logs.next();
      ByteBuffer buffer = logMessage.content();
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      log = new String(bytes);
    } while (!log.contains(messageToMatch));
  }

  protected ContainerCreation getContainer() {
    return this.container;
  }
}
