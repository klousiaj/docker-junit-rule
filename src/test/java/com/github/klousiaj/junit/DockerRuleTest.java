package com.github.klousiaj.junit;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by klousiaj on 7/20/16.
 */
public class DockerRuleTest {
  protected final Log logger = LogFactory.getLog(getClass());

  @Test
  public void generatePortBinding() {
    try {
      DockerRule.generatePortBinding("");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process ", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("1");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process 1", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("1:1");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process 1:1", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("1521:");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process 1521:", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("2881721");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process 2881721", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("28/");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process 28/", e.getMessage());
    }

    try {
      DockerRule.generatePortBinding("*28");
      Assert.fail("Should throw an exception");
    } catch (DockerException e) {
      Assert.assertEquals("Invalid port mapping. Was empty or did not match regex /^([\\d]{2,5})?(:\\d{2,5})?$/g. Unable to process *28", e.getMessage());
    }

    try {
      Map<String, List<PortBinding>> portBinding = DockerRule.generatePortBinding("4536", "22", "1521");
      Assert.assertEquals(3, portBinding.keySet().size());
      // make sure all of the ports are mapped.
      Assert.assertTrue(portBinding.containsKey("4536"));
      Assert.assertTrue(portBinding.containsKey("22"));
      Assert.assertTrue(portBinding.containsKey("1521"));
      // make sure that they are all generated as random ports
      Assert.assertEquals("", portBinding.get("4536").get(0).hostPort());
      Assert.assertEquals("", portBinding.get("22").get(0).hostPort());
      Assert.assertEquals("", portBinding.get("1521").get(0).hostPort());

    } catch (DockerException e) {
      Assert.fail(e.getMessage());
    }

    try {
      Map<String, List<PortBinding>> portBinding = DockerRule.generatePortBinding(":4536", "9222:22", "1521:1521");
      Assert.assertEquals(3, portBinding.keySet().size());
      // make sure all of the ports are mapped.
      Assert.assertTrue(portBinding.containsKey("4536"));
      Assert.assertTrue(portBinding.containsKey("22"));
      Assert.assertTrue(portBinding.containsKey("1521"));
      // make sure they aren't random, but are actually following the algorithm
      Assert.assertEquals("4536", portBinding.get("4536").get(0).hostPort());
      Assert.assertEquals("9222", portBinding.get("22").get(0).hostPort());
      Assert.assertEquals("1521", portBinding.get("1521").get(0).hostPort());

    } catch (DockerException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void imageNotFound() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";
    DockerClient mockClient = mock(DockerClient.class);

    ContainerCreation container = mock(ContainerCreation.class);

    when(mockClient.inspectImage(image)).thenThrow(new ImageNotFoundException(image));
    doNothing().when(mockClient).pull(image);
    when(mockClient.createContainer(any(ContainerConfig.class))).thenReturn(container);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};
    rule.params = params;

    rule.initialize();
    verify(mockClient).inspectImage(anyString());
    verify(mockClient).pull(params.imageName);
  }

  @Test
  public void imageFound() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";
    DockerClient mockClient = mock(DockerClient.class);

    ContainerCreation container = mock(ContainerCreation.class);

    when(mockClient.inspectImage(image)).thenReturn(new ImageInfo());
    doNothing().when(mockClient).pull(image);
    when(mockClient.createContainer(any(ContainerConfig.class))).thenReturn(container);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};
    rule.params = params;

    rule.initialize();
    verify(mockClient).inspectImage(anyString());
    verify(mockClient, never()).pull(params.imageName);
  }

  @Test
  public void leaveRunningTest() throws Throwable {
    String image = "kitematic/hello-world-nginx:1.0.0";
    DockerClient mockClient = mock(DockerClient.class);

    ContainerCreation container = mock(ContainerCreation.class);
    when(container.id()).thenReturn("A container id");

    when(mockClient.inspectImage(image)).thenReturn(new ImageInfo());
    doNothing().when(mockClient).pull(image);
    when(mockClient.createContainer(any(ContainerConfig.class))).thenReturn(container);

    doNothing().when(mockClient).killContainer(anyString());
    doNothing().when(mockClient).removeContainer(anyString());
    doNothing().when(mockClient).close();

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};
    params.leaveRunning = true;
    rule.params = params;

    rule.after();
    verify(mockClient, never()).killContainer(anyString());
    verify(mockClient, never()).removeContainer(anyString());
    verify(mockClient, never()).close();

    params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{":8080", "32000:9000", "7000"};
    params.leaveRunning = false;
    rule.params = params;
    DockerRule spyRule = spy(rule);

    ContainerInfo mockInfo = mock(ContainerInfo.class);
    NetworkSettings mockNetworkSettings = mock(NetworkSettings.class);
    when(mockNetworkSettings.ports()).thenReturn(DockerRule.generatePortBinding(params.ports));
    when(mockInfo.networkSettings()).thenReturn(mockNetworkSettings);
    when(mockClient.inspectContainer(anyString())).thenReturn(mockInfo);
    doReturn("eb01ee35d1fe").when(spyRule).foundRunningContainer(anyMap());
    spyRule.before();
    spyRule.after();
    verify(mockClient).killContainer(anyString());
    verify(mockClient).removeContainer(anyString());
    verify(mockClient).close();
  }

  @Test
  public void attachToRunningNotSameImage() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] ports = new String[]{":8080", "32000:9000", "7000"};
    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:not-the-same");
    when(container.state()).thenReturn("running");
    //when(container.ports()).thenReturn(null);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};
    params.leaveRunning = true;
    rule.params = params;

    Assert.assertNull(rule.foundRunningContainer(DockerRule.generatePortBinding(ports)));
  }

  @Test
  public void attachToRunningSameImageDifferentPorts() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] runningContainerPorts = new String[]{":8080", "32000:9000", "7000"};
    String[] requestedPorts = new String[]{":8080", "32000:9000", "7001", "9001"};

    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:1.0.0");
    when(container.state()).thenReturn("running");
    List<Container.PortMapping> mappings = generateMappingList(runningContainerPorts);
    when(container.ports()).thenReturn(mappings);
    containers.add(container);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.useRunning = true;
    rule.params = params;

    Assert.assertNull(rule.foundRunningContainer(DockerRule.generatePortBinding(requestedPorts)));
  }

  @Test
  public void attachToRunningContainerNotRunning() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] ports = new String[]{":8080", "32000:9000", "7000"};
    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:not-the-same");
    when(container.state()).thenReturn("stopped");
    //when(container.ports()).thenReturn(null);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};
    params.leaveRunning = true;
    rule.params = params;

    Assert.assertNull(rule.foundRunningContainer(DockerRule.generatePortBinding(ports)));
  }

  @Test
  public void attachToRunningSameImageSamePorts() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] ports = new String[]{":8080", "32000:9000", "7000"};

    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:1.0.0");
    when(container.state()).thenReturn("running");
    when(container.id()).thenReturn("eb01ee35d1fe");
    List<Container.PortMapping> mappings = generateMappingList(ports);
    when(container.ports()).thenReturn(mappings);
    containers.add(container);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.useRunning = true;
    rule.params = params;

    Assert.assertNotNull(rule.foundRunningContainer(DockerRule.generatePortBinding(ports)));
  }

  @Test
  public void attachToRunningSameImageMoreOpenPorts() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] containerPorts = new String[]{":8080", "32000:9000", "7000"};
    String[] requestedPorts = new String[]{":8080", "32000:9000"};

    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:1.0.0");
    when(container.state()).thenReturn("running");
    when(container.id()).thenReturn("eb01ee35d1fe");
    List<Container.PortMapping> mappings = generateMappingList(containerPorts);
    when(container.ports()).thenReturn(mappings);
    containers.add(container);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.useRunning = true;
    rule.params = params;

    Assert.assertNotNull(rule.foundRunningContainer(DockerRule.generatePortBinding(requestedPorts)));
  }

  @Test
  public void attachToRunningSameImageNoPorts() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    String[] ports = new String[]{};

    // container that is running but isn't the same image
    List<Container> containers = new ArrayList<>();
    Container container = mock(Container.class);
    when(container.image()).thenReturn("kitematic/hello-world-nginx:1.0.0");
    when(container.state()).thenReturn("running");
    when(container.id()).thenReturn("eb01ee35d1fe");
    List<Container.PortMapping> mappings = generateMappingList(ports);
    when(container.ports()).thenReturn(mappings);
    containers.add(container);

    DockerClient mockClient = mock(DockerClient.class);
    // one running container that doesn't match on image
    when(mockClient.listContainers()).thenReturn(containers);

    DockerRule rule = new DockerRule(mockClient);

    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.useRunning = true;
    rule.params = params;

    Assert.assertNotNull(rule.foundRunningContainer(DockerRule.generatePortBinding(ports)));
  }

  @Test(expected = IllegalStateException.class)
  public void catchIllegalStateException() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";

    DockerClient mockClient = mock(DockerClient.class);
    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;
    params.ports = new String[]{"8080"};

    when(mockClient.inspectImage(image)).thenThrow(DockerException.class);

    DockerRule rule = new DockerRule(mockClient);
    rule.params = params;

    rule.initialize();
  }

  @Test(expected = RuntimeException.class)
  public void checkAfterExceptionHandling() throws Exception {
    String image = "kitematic/hello-world-nginx:1.0.0";
    ContainerCreation mockContainer = mock(ContainerCreation.class);
    when(mockContainer.id()).thenReturn("A container id");

    DockerClient mockClient = mock(DockerClient.class);
    DockerRuleParams params = new DockerRuleParams();
    params.imageName = image;

    doThrow(DockerException.class).when(mockClient).killContainer(anyString());
    doThrow(DockerException.class).when(mockClient).removeContainer(anyString());

    DockerRule rule = new DockerRule(mockClient);
    rule.params = params;

    // create a spy on the rule so we can return a mocked container
    DockerRule spyRule = spy(rule);
    doReturn(mockContainer).when(spyRule).getContainer();
    spyRule.after();
  }

  public List<Container.PortMapping> generateMappingList(String... ports) {
    List<Container.PortMapping> portBindings = new ArrayList<>();
    for (String port : ports) {
      // it will be random port if there is not a colon included
      String hostPort = "";
      String containerPort = port;
      String[] split = port.split(":");
      if (split.length > 1) {
        // clean up the container port description
        containerPort = split[1];
        hostPort = "".equals(split[0]) ? split[1] : split[0];
      }
      portBindings.add(generateMockPortMapping(containerPort, hostPort));
    }
    return portBindings;
  }

  Container.PortMapping generateMockPortMapping(String privatePort, String hostPort) {
    Container.PortMapping mockMapping = mock(Container.PortMapping.class);
    Integer port = "".equals(hostPort) ? 30000 : Integer.valueOf(hostPort);
    when(mockMapping.getPrivatePort()).thenReturn(Integer.valueOf(privatePort));
    when(mockMapping.getPublicPort()).thenReturn(port);
    return mockMapping;
  }

  @Test
  public void validPortMap() {
    List<Container.PortMapping> mapping = generateMappingList("8080", "38000:80", ":7000");
    DockerClient mockClient = mock(DockerClient.class);
    DockerRule rule = new DockerRule(mockClient);

    // valid - not specified
    Assert.assertTrue(rule.validPortMap("8080", "", mapping));

    // valid - different mapping
    Assert.assertTrue(rule.validPortMap("80", "38000", mapping));

    // valid - mapping shorthand
    Assert.assertTrue(rule.validPortMap("7000", "7000", mapping));

    // invalid - container port not mapped
    Assert.assertFalse(rule.validPortMap("8000", "38001", mapping));

    // invalid - container port not mapped - random
    Assert.assertFalse(rule.validPortMap("8000", "38001", mapping));

    // invalid - incorrect mapping
    Assert.assertFalse(rule.validPortMap("80", "9000", mapping));
  }
}
