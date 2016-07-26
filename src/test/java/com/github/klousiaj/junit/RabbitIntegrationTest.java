package com.github.klousiaj.junit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Map;

public class RabbitIntegrationTest {

  @ClassRule
  public static DockerRule rabbitRule =
    DockerRule.builder()
      .image("rabbitmq:management")
      .ports("5672", ":32779", "32880:5671")
      .envs("RABBITMQ_DEFAULT_PASS=password1234")
//      .waitForPort("5672/tcp")
      .waitForLog("Server startup complete")
      .leaveRunning(true)
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
    // run the after on the Rule. This should shutdown the container
    // unless the leaveRunning flag is set.
    rabbitRule.after();

    Assert.assertNotEquals(-1, rabbitRule.getHostPort("5672/tcp"));
    Assert.assertEquals(32779, rabbitRule.getHostPort("32779/tcp"));
    Assert.assertEquals(32880, rabbitRule.getHostPort("5671/tcp"));
    Assert.assertEquals(-1, rabbitRule.getHostPort("5675/tcp"));

    // set the leaveRunning to false to make sure the container
    // does actually get cleaned up.
    rabbitRule.params.leaveRunning = false;
  }
}
