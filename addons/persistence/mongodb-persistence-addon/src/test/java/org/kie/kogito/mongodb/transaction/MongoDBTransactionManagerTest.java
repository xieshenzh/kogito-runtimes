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

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.uow.events.AfterAbortEvent;
import org.kie.kogito.uow.events.AfterEndEvent;
import org.kie.kogito.uow.events.BeforeStartEvent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoDBTransactionManagerTest {

    private static final int TEST_THREADS = 10;

    private MongoClient mongoClient;
    private ClientSession clientSession;
    private MongoDBTransactionManager manager;

    @BeforeEach
    void setUp() {
        mongoClient = mock(MongoClient.class);
        clientSession = mock(ClientSession.class);
        when(mongoClient.startSession()).thenReturn(clientSession);

        manager = new MongoDBTransactionManager(mongoClient) {
            @Override
            public boolean enabled() {
                return true;
            }
        };
    }

    @Test
    void test() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(TEST_THREADS);
        CountDownLatch latch = new CountDownLatch(TEST_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < TEST_THREADS; i++) {
            service.execute(() -> {
                try {
                    startLatch.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                MongoClient mongoClient = mock(MongoClient.class);
                ClientSession clientSession = mock(ClientSession.class);
                when(mongoClient.startSession()).thenReturn(clientSession);

                MongoDBTransactionManager manager = new MongoDBTransactionManager(mongoClient) {
                    @Override
                    public boolean enabled() {
                        return true;
                    }
                };

                try {
                    manager.onBeforeStartEvent(new BeforeStartEvent());
                    manager.onAfterEndEvent(new AfterEndEvent());
                    verify(mongoClient, times(1)).startSession();
                    verify(clientSession, times(1)).startTransaction(any());
                    verify(clientSession, times(1)).commitTransaction();
                    verify(clientSession, times(1)).close();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    latch.countDown();
                }
            });
        }
        startLatch.countDown();
        latch.await(2, TimeUnit.MINUTES);
    }

    @Test
    void test_enabled() {
        MongoDBTransactionManager manager = new MongoDBTransactionManager(mongoClient) {
            @Override
            public boolean enabled() {
                return false;
            }
        };

        manager.onBeforeStartEvent(new BeforeStartEvent());
        manager.onAfterAbortEvent(new AfterAbortEvent());

        verify(mongoClient, never()).startSession();
    }

    @Test
    void onBeforeStartEvent() {
        manager.onBeforeStartEvent(new BeforeStartEvent());
        verify(clientSession, times(1)).startTransaction(any());
    }

    @Test
    void onAfterEndEvent() {
        manager.onBeforeStartEvent(new BeforeStartEvent());
        manager.onAfterEndEvent(new AfterEndEvent());
        verify(clientSession, times(1)).commitTransaction();
        verify(clientSession, times(1)).close();
    }

    @Test
    void onAfterAbortEvent() {
        manager.onBeforeStartEvent(new BeforeStartEvent());
        manager.onAfterAbortEvent(new AfterAbortEvent());
        verify(clientSession, times(1)).abortTransaction();
        verify(clientSession, times(1)).close();
    }

    @Test
    void getResource() {
        assertNull(manager.getResource());

        manager.onBeforeStartEvent(new BeforeStartEvent());
        assertEquals(clientSession, manager.getResource());
    }
}