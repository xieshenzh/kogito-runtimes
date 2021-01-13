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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.kie.kogito.persistence.transaction.TransactionManager;
import org.kie.kogito.uow.events.AfterAbortEvent;
import org.kie.kogito.uow.events.AfterEndEvent;
import org.kie.kogito.uow.events.BeforeStartEvent;
import org.kie.kogito.uow.events.UnitOfWorkEventListener;

public abstract class MongoDBTransactionManager implements TransactionManager<ClientSession>, UnitOfWorkEventListener {

    private MongoClient mongoClient;

    private ThreadLocal<ClientSession> clientSessionLocal;

    public MongoDBTransactionManager(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.clientSessionLocal = new ThreadLocal<>();
    }

    @Override
    public void onBeforeStartEvent(BeforeStartEvent event) {
        if (!enabled()) {
            return;
        }

        ClientSession clientSession = mongoClient.startSession();
        this.clientSessionLocal.set(clientSession);
        TransactionOptions txnOptions = TransactionOptions.builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
        clientSession.startTransaction(txnOptions);
    }

    @Override
    public void onAfterEndEvent(AfterEndEvent event) {
        if (!enabled()) {
            return;
        }

        ClientSession clientSession = this.getResource();
        if (clientSession != null) {
            try {
                clientSession.commitTransaction();
            } finally {
                clientSession.close();
                clientSessionLocal.remove();
            }
        }
    }

    @Override
    public void onAfterAbortEvent(AfterAbortEvent event) {
        if (!enabled()) {
            return;
        }

        ClientSession clientSession = this.getResource();
        if (clientSession != null) {
            try {
                clientSession.abortTransaction();
            } finally {
                clientSession.close();
                clientSessionLocal.remove();
            }
        }
    }

    @Override
    public ClientSession getResource() {
        return clientSessionLocal.get();
    }

    public abstract boolean enabled();
}
