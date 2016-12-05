package com.github.klousiaj.junit.integration;

import com.github.klousiaj.junit.DockerRule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by klousiaj on 11/30/16.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
  {
    MongoCreateIntegrationTest.class,
    MongoReadIntegrationTest.class,
    MongoUpdateIntegrationTest.class,
    MongoDeleteIntegrationTest.class
  })
public class IntegrationTestSuite {

  static final Log logger = LogFactory.getLog(IntegrationTestSuite.class);

  static final String DB_NAME = "rule-test-db";
  static final String COLLECTION_NAME = "docker-rule-collection";
  static final Long DOC_COUNT = 11L;

  static MongoClient MDB_CLIENT;

  private static List<Document> expected = new ArrayList<>();

  @ClassRule
  public static DockerRule mongoRule =
    DockerRule.builder()
      .image("mongo:latest")
      .ports("27017")
      .waitForLog("waiting for connections on port 27017")
      .cleanVolumes(true)
      .build();

  @AfterClass
  public static void afterClass() {
    logger.info("AfterClass method fired.");
    if (MDB_CLIENT != null) {
      MDB_CLIENT.close();
    }
  }

  public static MongoDatabase getMongoDatabase() {
    if (MDB_CLIENT == null) {
      MDB_CLIENT = new MongoClient(mongoRule.getDockerHost(), mongoRule.getHostPort("27017/tcp"));
    }
    return MDB_CLIENT.getDatabase(DB_NAME);
  }

  public static Document generateDocument() {
    Long now = Calendar.getInstance().getTimeInMillis();
    Document doc = new Document("name", "docker-test-rule")
      .append("type", "json-document")
      .append("createDate", now)
      .append("number", ThreadLocalRandom.current().nextLong());
    expected.add(doc);
    return doc;
  }

  public static List<Document> getExpected() {
    return expected;
  }
}
