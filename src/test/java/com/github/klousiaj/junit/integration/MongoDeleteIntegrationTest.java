package com.github.klousiaj.junit.integration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by klousiaj on 12/4/16.
 */
public class MongoDeleteIntegrationTest {
  @Test
  public void updateMongoDocuments() {
    MongoDatabase database = IntegrationTestSuite.getMongoDatabase();
    MongoCollection<Document> collection = database.getCollection(IntegrationTestSuite.COLLECTION_NAME);

    DeleteResult deleteResult = collection.deleteMany(Filters.eq("type", "json-document"));
    Assert.assertEquals(IntegrationTestSuite.DOC_COUNT, new Long(deleteResult.getDeletedCount()));
  }
}
