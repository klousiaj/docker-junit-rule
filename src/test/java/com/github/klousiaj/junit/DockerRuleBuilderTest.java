package com.github.klousiaj.junit;

import org.junit.Test;

/**
 * Created by klousiaj on 12/5/16.
 */
public class DockerRuleBuilderTest {

  @Test(expected = IllegalArgumentException.class)
  public void specialCharacterLabelKeyString() {
    DockerRuleBuilder builder = new DockerRuleBuilder();
    builder.labels("com.github%sklousiaj.example:a sample");
  }

  @Test(expected = IllegalArgumentException.class)
  public void numberCharacterLabelValueString() {
    DockerRuleBuilder builder = new DockerRuleBuilder();
    builder.labels("com.github.klousiaj.example:a \nsample");
  }
}
