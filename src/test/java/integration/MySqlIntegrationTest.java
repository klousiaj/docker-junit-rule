package integration;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

public class MySqlIntegrationTest {

  @ClassRule
  public static DockerRule rabbitRule =
    DockerRule.builder()
      .image("mysql")
      .ports("3306")
      .envs("MYSQL_ROOT_PASSWORD=my_secret")
      .waitForPort("3306/tcp")
//      .waitForLog("")
      .build();

  @Test
  public void testConnectsToDocker() throws Exception {
    // TODO - need to add validation that connects to the DB to ensure that the environment variables are set properly.
  }
}
