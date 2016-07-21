# docker-junit-rule

A junit rule to run docker containers.

[![Build Status](https://travis-ci.org/klousiaj/docker-junit-rule.svg)](https://travis-ci.org/klousiaj/docker-junit-rule)

## Usage

Example for rabbitMQ:

```java
import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

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
    factory.setHost(rabbitRule.getDockerHost());
    factory.setPort(rabbitRule.getHostPort("5672/tcp"));
    factory.newConnection();
  }
}
```

## Versions
Check out the [ChangeLog](./CHANGELOG.md)

## Installation

The library is available on jcenter

### Maven

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>

...

<dependency>
    <groupId>com.github.geowarin</groupId>
    <artifactId>docker-junit-rule</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```groovy
repositories {
  jcenter()
}

dependencies {
  testCompile 'com.github.geowarin:docker-junit-rule:1.1.0'
}
```

## Principle

Uses https://github.com/spotify/docker-client to connect to the docker daemon API.

You should probably set the `DOCKER_HOST` and `DOCKER_CERT_PATH` on your machine.
If they are not set and your are not on UNIX, the client will try to connect to `https://192.168.99.100:2376`,
which is the adress of my default docker machine.
It works great for me but your mileage may vary.
