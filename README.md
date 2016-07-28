# docker-junit-rule [ ![Download](https://api.bintray.com/packages/klousiaj/maven/docker-junit-rule/images/download.svg) ](https://bintray.com/klousiaj/maven/docker-junit-rule/_latestVersion) [![Build Status](https://travis-ci.org/klousiaj/docker-junit-rule.svg)](https://travis-ci.org/klousiaj/docker-junit-rule) [![codecov.io](https://codecov.io/gh/klousiaj/docker-junit-rule/coverage.svg?branch=master)](https://codecov.io/github/klousia/docker-junit-rule?branch=master)


A junit rule to run docker containers. This repository is based on, and extended from the excellent 
library written by Geoffroy Warin. You can find the original code [here](https://github.com/geowarin/docker-junit-rule).

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

This is hosted on bintray.

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
    <version>1.2.2</version>
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
  testCompile 'com.github.klousiaj:docker-junit-rule:1.2.2'
}
```

## Principle

This plugin relies on https://github.com/spotify/docker-client to connect to the docker daemon API.

You can see the latest and greatest build status by checking on the build at travis-ci. Travis CI 
runs the tests against multiple versions of Docker:
 - 1.10.3
 - 1.11.2
 - 1.12.0
 
It has also been validated using docker-toolbox. You should probably set the `DOCKER_HOST` and `DOCKER_CERT_PATH` on your machine.
If they are not set and your are not on UNIX, the client will try to connect to `https://192.168.99.100:2376`,
which is the address of my default docker machine. It works great for me but your mileage may vary.
