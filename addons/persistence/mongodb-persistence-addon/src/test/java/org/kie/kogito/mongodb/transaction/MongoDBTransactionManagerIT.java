/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.mongodb.transaction;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.KogitoMongoDBContainer;
import org.kie.kogito.uow.events.AfterAbortEvent;
import org.kie.kogito.uow.events.AfterEndEvent;
import org.kie.kogito.uow.events.BeforeStartEvent;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class MongoDBTransactionManagerIT {

    static class TestTransactionManager extends MongoDBTransactionManager {

        public TestTransactionManager(MongoClient mongoClient) {
            super(mongoClient);
        }

        @Override
        public boolean enabled() {
            return true;
        }
    }

    @Container
    private static KogitoMongoDBContainer mongoDBContainer = new KogitoMongoDBContainer();
    private static MongoClient mongoClient;

    private static final String DOCUMENT_ID = "_id";
    private static final String TEST_KEY = "test";

    private static final int TEST_THREADS = 2;

    @BeforeAll
    public static void setup() {
        mongoDBContainer.start();
        mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());
    }

    @AfterAll
    public static void close() {
        mongoDBContainer.stop();
    }

    @Test
    void test_insertion() throws InterruptedException {
        String testName = "test_insertion";

        MongoDatabase mongoDatabase = mongoClient.getDatabase(testName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(testName);
        mongoCollection.insertOne(new Document().append(DOCUMENT_ID, "test0"));

        MongoDBTransactionManager transactionManager1 = new TestTransactionManager(mongoClient);
        MongoDBTransactionManager transactionManager2 = new TestTransactionManager(mongoClient);

        String id1 = "test1";
        String value1 = "test1";
        String id2 = "test2";
        String value2 = "test2";

        ExecutorService service = Executors.newFixedThreadPool(TEST_THREADS);
        CountDownLatch latch = new CountDownLatch(TEST_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        service.execute(() -> {
            try {
                startLatch.await();
                try {
                    transactionManager1.onBeforeStartEvent(new BeforeStartEvent());

                    MongoCollection<Document> mongoCollection1 = mongoDatabase.getCollection(testName);
                    mongoCollection1.insertOne(transactionManager1.getResource(), new Document().append(DOCUMENT_ID, id1).append(TEST_KEY, value1));

                    Document result1 = mongoCollection1.find(transactionManager1.getResource(), Filters.eq(DOCUMENT_ID, id1)).first();
                    assertEquals(new Document().append(DOCUMENT_ID, id1).append(TEST_KEY, value1), result1);

                    int size1 = (int) mongoCollection1.countDocuments(transactionManager1.getResource());
                    assertEquals(2, size1);

                    transactionManager1.onAfterEndEvent(new AfterEndEvent());
                } finally {
                    latch1.countDown();
                    latch2.await(1, TimeUnit.MINUTES);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                startLatch.await();
                try {
                    transactionManager2.onBeforeStartEvent(new BeforeStartEvent());

                    MongoCollection<Document> mongoCollection2 = mongoDatabase.getCollection(testName);
                    mongoCollection2.insertOne(transactionManager2.getResource(), new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value2));

                    Document result2 = mongoCollection2.find(transactionManager2.getResource(), Filters.eq(DOCUMENT_ID, id2)).first();
                    assertEquals(new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value2), result2);

                    int size2 = (int) mongoCollection2.countDocuments(transactionManager2.getResource());
                    assertEquals(2, size2);

                    transactionManager2.onAfterEndEvent(new AfterEndEvent());
                } finally {
                    latch2.countDown();
                    latch1.await(1, TimeUnit.MINUTES);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                latch.countDown();
            }
        });
        startLatch.countDown();
        latch.await(2, TimeUnit.MINUTES);

        MongoCollection<Document> mongoCollectionNew = mongoDatabase.getCollection(testName);
        int size = (int) mongoCollectionNew.countDocuments();
        assertEquals(3, size);
    }

    @Test
    void test_deletion_update() throws InterruptedException {
        String testName = "test_deletion_update";

        MongoDatabase mongoDatabase = mongoClient.getDatabase(testName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(testName);

        MongoDBTransactionManager transactionManager1 = new TestTransactionManager(mongoClient);
        MongoDBTransactionManager transactionManager2 = new TestTransactionManager(mongoClient);

        String id1 = "test1";
        String value1 = "test1";
        String id2 = "test2";
        String value2 = "test2";

        mongoCollection.insertOne(new Document().append(DOCUMENT_ID, id1).append(TEST_KEY, value1));
        mongoCollection.insertOne(new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value2));

        ExecutorService service = Executors.newFixedThreadPool(TEST_THREADS);
        CountDownLatch latch = new CountDownLatch(TEST_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        service.execute(() -> {
            try {
                startLatch.await();
                try {
                    transactionManager1.onBeforeStartEvent(new BeforeStartEvent());

                    MongoCollection<Document> mongoCollection1 = mongoDatabase.getCollection(testName);
                    mongoCollection1.deleteOne(transactionManager1.getResource(), Filters.eq(DOCUMENT_ID, id1));

                    List<Document> values1 = new ArrayList<>();
                    try (MongoCursor<Document> cursor = mongoCollection1.find(transactionManager1.getResource()).iterator()) {
                        while (cursor.hasNext()) {
                            values1.add(cursor.next());
                        }

                        assertEquals(1, values1.size());
                        assertTrue(values1.stream().anyMatch(v -> id2.equals(v.get(DOCUMENT_ID).toString())));
                    }

                    Document value = mongoCollection1.find(transactionManager1.getResource(), Filters.eq(DOCUMENT_ID, id2)).first();
                    assertEquals(new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value2), value);

                    transactionManager1.onAfterEndEvent(new AfterEndEvent());
                } finally {
                    latch1.countDown();
                    latch2.await(1, TimeUnit.MINUTES);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                latch.countDown();
            }
        });

        service.execute(() -> {
            try {
                startLatch.await();
                try {
                    transactionManager2.onBeforeStartEvent(new BeforeStartEvent());

                    MongoCollection<Document> mongoCollection2 = mongoDatabase.getCollection(testName);
                    mongoCollection2.replaceOne(transactionManager2.getResource(), Filters.eq(DOCUMENT_ID, id2), new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value1));
                    Document values2 = mongoCollection2.find(transactionManager2.getResource(), Filters.eq(DOCUMENT_ID, id2)).first();
                    assertEquals(new Document().append(DOCUMENT_ID, id2).append(TEST_KEY, value1), values2);

                    int size2 = (int) mongoCollection2.countDocuments(transactionManager2.getResource());
                    assertEquals(2, size2);

                    transactionManager2.onAfterEndEvent(new AfterEndEvent());
                } finally {
                    latch2.countDown();
                    latch1.await(1, TimeUnit.MINUTES);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                latch.countDown();
            }
        });
        startLatch.countDown();
        latch.await(2, TimeUnit.MINUTES);

        MongoCollection<Document> mongoCollectionNew = mongoDatabase.getCollection(testName);
        int size = (int) mongoCollectionNew.countDocuments();
        assertEquals(1, size);
        assertEquals(value1, Objects.requireNonNull(mongoCollectionNew.find(Filters.eq(DOCUMENT_ID, id2)).first()).getString(TEST_KEY));
    }

    @Test
    void test_abort() {
        String testName = "test_abort";

        MongoDatabase mongoDatabase = mongoClient.getDatabase(testName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(testName);
        mongoCollection.insertOne(new Document().append(DOCUMENT_ID, "test0"));

        MongoDBTransactionManager transactionManager = new TestTransactionManager(mongoClient);

        String id1 = "test1";
        String value1 = "test1";

        transactionManager.onBeforeStartEvent(new BeforeStartEvent());

        mongoCollection.insertOne(transactionManager.getResource(), new Document().append(DOCUMENT_ID, id1).append(TEST_KEY, value1));

        Document result = mongoCollection.find(transactionManager.getResource(), Filters.eq(DOCUMENT_ID, id1)).first();
        assertEquals(new Document().append(DOCUMENT_ID, id1).append(TEST_KEY, value1), result);

        int size1 = (int) mongoCollection.countDocuments(transactionManager.getResource());
        assertEquals(2, size1);

        int size2 = (int) mongoCollection.countDocuments();
        assertEquals(1, size2);

        transactionManager.onAfterAbortEvent(new AfterAbortEvent());

        int size3 = (int) mongoCollection.countDocuments();
        assertEquals(1, size3);
    }
}
