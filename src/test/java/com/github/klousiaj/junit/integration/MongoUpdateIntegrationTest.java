package com.github.klousiaj.junit.integration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by klousiaj on 12/4/16.
 */
public class MongoUpdateIntegrationTest {

  @Test
  public void updateMongoDocuments() {
    MongoDatabase database = IntegrationTestSuite.getMongoDatabase();
    MongoCollection<Document> collection = database.getCollection(IntegrationTestSuite.COLLECTION_NAME);

    UpdateResult updateResult = collection.updateMany(Filters.eq("type", "json-document"),
      new Document("$inc", new Document("number", 100)));
    Assert.assertEquals(IntegrationTestSuite.DOC_COUNT, new Long(updateResult.getModifiedCount()));
  }
}
