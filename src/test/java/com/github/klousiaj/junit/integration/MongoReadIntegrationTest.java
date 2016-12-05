package com.github.klousiaj.junit.integration;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by klousiaj on 12/4/16.
 */
public class MongoReadIntegrationTest {

  @Test
  public void readMongoDocuments() {
    MongoDatabase database = IntegrationTestSuite.getMongoDatabase();
    MongoCollection<Document> collection = database.getCollection(IntegrationTestSuite.COLLECTION_NAME);

    Assert.assertEquals(IntegrationTestSuite.DOC_COUNT, Long.valueOf(collection.count()));

    for (Document expect : IntegrationTestSuite.getExpected()) {
      Document actual = collection.find(Filters.eq("number", expect.get("number"))).first();
      Assert.assertEquals(expect.get("createdDate"), actual.get("createdDate"));
      Assert.assertNotNull(actual.get("_id"));
      IntegrationTestSuite.logger.info(actual.toJson());
    }
  }
}
