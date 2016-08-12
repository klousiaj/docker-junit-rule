package com.github.klousiaj.junit;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Created by klousiaj on 8/11/16.
 */
public class NginxIntegrationTest {

  @ClassRule
  public static DockerRule nginxRule =
    DockerRule.builder()
      .image("kitematic/hello-world-nginx:latest")
      .ports("80")
      .waitForPort("80/tcp")
      .build();

  @Test
  public void checkNginxRunning() throws Exception {
    ProcessBuilder pb = new ProcessBuilder("curl", "-s", "http://" + nginxRule.getDockerHost() + ":" + nginxRule.getHostPort("80/tcp") + "/");
    Process p = pb.start();
    BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
    String output = new String();
    byte[] contents = new byte[1024];
    int bytesRead = 0;
    while ((bytesRead = bis.read(contents)) != -1) {
      output += new String(contents, 0, bytesRead);
    }
    Assert.assertEquals("<div style=\"color: #35393B; margin-top: 100px; text-align: center; font-family: HelveticaNeue-Light, sans-serif;\">\n" +
      "  <img src=\"https://cloud.githubusercontent.com/assets/251292/5254757/a08a277c-7981-11e4-9ec0-d49934859400.png\">\n" +
      "  <h2>Voil&agrave;! Your nginx container is running!</h2>\n" +
      "  <div style=\"color: #838789;\">\n" +
      "    <p>To edit files, double click the <strong>website_files</strong> folder in Kitematic and edit the <strong>index.html</strong> file.</p>\n" +
      "  </div>\n" +
      "</div>\n", output);

  }

}
