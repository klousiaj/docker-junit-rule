package com.github.geowarin.junit;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
}
