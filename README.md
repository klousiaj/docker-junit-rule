# docker-junit-rule

A junit rule to run docker containers. This repository is based on, and extended from the excellent 
library written by Geoffroy Warin. You can find the original code [here](https://github.com/geowarin/docker-junit-rule).

[ ![Download](https://api.bintray.com/packages/klousiaj/maven/docker-junit-rule/images/download.svg) ](https://bintray.com/klousiaj/maven/docker-junit-rule/_latestVersion)
[![Build Status](https://travis-ci.org/klousiaj/docker-junit-rule.svg)](https://travis-ci.org/klousiaj/docker-junit-rule)

## Usage

Example for rabbitMQ:

```java
import DockerRule;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

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
    factory.setHost(rabbitRule.getDockerHost());
    factory.setPort(rabbitRule.getHostPort("5672/tcp"));
    factory.newConnection();
  }
}
```

## Versions
Check out the [ChangeLog](./CHANGELOG.md)

## Installation



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
    <groupId>com.github.klousiaj</groupId>
    <artifactId>docker-junit-rule</artifactId>
    <version>1.2.0</version>
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
  testCompile 'com.github.klousiaj:docker-junit-rule:1.2.0'
}
```

## Principle

Uses https://github.com/spotify/docker-client to connect to the docker daemon API.

You can see the latest and greatest build status by checking on the build at travis-ci. Travis CI 
runs the 

You should probably set the `DOCKER_HOST` and `DOCKER_CERT_PATH` on your machine.
If they are not set and your are not on UNIX, the client will try to connect to `https://192.168.99.100:2376`,
which is the adress of my default docker machine.
It works great for me but your mileage may vary.
