package com.github.klousiaj.junit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RabbitIntegrationTest {

  @ClassRule
  public static DockerRule rabbitRule =
    DockerRule.builder()
      .image("rabbitmq:management")
      .ports("5672", ":32779", "32880:5671")
      .envs("RABBITMQ_DEFAULT_PASS=password1234")
//      .waitForPort("5672/tcp")
      .waitForLog("Server startup complete")
      .useRunning(true)
      .leaveRunning(true)
      .cleanVolumes(true)
      .build();

  @Test
  public void testConnectsToDocker() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("guest");
    factory.setPassword("password1234");
    factory.setHost(rabbitRule.getDockerHost());

    Assert.assertNotEquals(-1, rabbitRule.getHostPort("5672/tcp"));
    Assert.assertEquals(32779, rabbitRule.getHostPort("32779/tcp"));
    Assert.assertEquals(32880, rabbitRule.getHostPort("5671/tcp"));
    Assert.assertEquals(-1, rabbitRule.getHostPort("5675/tcp"));

    factory.setPort(rabbitRule.getHostPort("5672/tcp"));
    Connection conn = factory.newConnection();
    Map<String, Object> serverProperties = conn.getServerProperties();
    Assert.assertNotNull(serverProperties);
  }

  @Test
  public void testContainerStaysRunning() throws Exception {
    // run the stop on the Rule. This should shutdown the container
    // unless the leaveRunning flag is set.
    rabbitRule.stop();

    Assert.assertNotEquals(-1, rabbitRule.getHostPort("5672/tcp"));
    Assert.assertEquals(32779, rabbitRule.getHostPort("32779/tcp"));
    Assert.assertEquals(32880, rabbitRule.getHostPort("5671/tcp"));
    Assert.assertEquals(-1, rabbitRule.getHostPort("5675/tcp"));

    // set the leaveRunning to false to make sure the container
    // does actually get cleaned up.
    rabbitRule.params.leaveRunning = false;
  }

  @Test
  public void testConnectToAlreadyRunning() throws Throwable {

    DockerClient client = rabbitRule.createDockerClient();
    // TODO - add some additional tests here to ensure that only the right containers are actually running.
    List<Container> preStartContainers = client.listContainers();
    int preCount = preStartContainers.size();
    int preRunningCount = 0;
    int preRabbitCount = 0;
    for (Container container : preStartContainers) {
      if ("running".equals(container.state())) {
        preRunningCount++;
      }
      if ("rabbitmq:management".equals(container.image())) {
        preRabbitCount++;
      }
    }

    DockerRule rule2 = DockerRule.builder()
      .image("rabbitmq:management")
      .ports("5672", ":32779", "32880:5671")
      .envs("RABBITMQ_DEFAULT_PASS=password1234")
      .useRunning(true)
      .build();

    rule2.start();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("guest");
    factory.setPassword("password1234");
    factory.setHost(rule2.getDockerHost());

    Assert.assertNotEquals(-1, rule2.getHostPort("5672/tcp"));
    Assert.assertEquals(32779, rule2.getHostPort("32779/tcp"));
    Assert.assertEquals(32880, rule2.getHostPort("5671/tcp"));
    Assert.assertEquals(-1, rule2.getHostPort("5675/tcp"));

    factory.setPort(rule2.getHostPort("5672/tcp"));
    Connection conn = factory.newConnection();
    Map<String, Object> serverProperties = conn.getServerProperties();
    Assert.assertNotNull(serverProperties);

    List<Container> postStartContainers = client.listContainers();
    int postCount = postStartContainers.size();
    int postRunningCount = 0;
    int postRabbitCount = 0;
    for (Container container : postStartContainers) {
      if ("running".equals(container.state())) {
        postRunningCount++;
      }
      if ("rabbitmq:management".equals(container.image())) {
        postRabbitCount++;
      }
    }

    Assert.assertEquals(preCount, postCount);
    Assert.assertEquals(preRunningCount, postRunningCount);
    Assert.assertEquals(preRabbitCount, postRabbitCount);
  }

  @Test
  public void validateContainerName() throws Exception {
    Pattern validForDocker = Pattern.compile(DockerRule.CONTAINER_NAME_REGEX);
    Assert.assertTrue(validForDocker.matcher(rabbitRule.getContainerName()).matches());
  }
}
