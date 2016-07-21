package integration;

import com.github.klousiaj.junit.DockerRule;
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
}
