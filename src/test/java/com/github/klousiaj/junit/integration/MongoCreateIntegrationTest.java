package com.github.klousiaj.junit.integration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klousiaj on 12/4/16.
 */
public class MongoCreateIntegrationTest {

  @Test
  public void createMongoDocument() {
    MongoDatabase database = IntegrationTestSuite.getMongoDatabase();
    MongoCollection<Document> collection = database.getCollection(IntegrationTestSuite.COLLECTION_NAME);

    List<Document> docs = new ArrayList<>();
    for (int ii = 0; ii < IntegrationTestSuite.DOC_COUNT; ii++) {
      docs.add(IntegrationTestSuite.generateDocument());
    }
    collection.insertMany(docs);

    Assert.assertEquals(IntegrationTestSuite.DOC_COUNT, Long.valueOf(collection.count()));
  }
}
