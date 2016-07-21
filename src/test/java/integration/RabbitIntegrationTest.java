package integration;

import com.github.geowarin.junit.DockerRule;
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
      .ports("5672")
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
    factory.setPort(rabbitRule.getHostPort("5672/tcp"));
    Connection conn = factory.newConnection();
    Map<String, Object> serverProperties = conn.getServerProperties();
    Assert.assertNotNull(serverProperties);
  }
}
