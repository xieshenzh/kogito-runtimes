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

package org.kie.kogito.integrationtests;

import org.kie.kogito.uow.events.AfterAbortEvent;
import org.kie.kogito.uow.events.AfterEndEvent;
import org.kie.kogito.uow.events.BeforeStartEvent;
import org.kie.kogito.uow.events.UnitOfWorkEventListener;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventListener implements UnitOfWorkEventListener {

    private BeforeStartEvent beforeStartEvent;

    private AfterEndEvent afterEndEvent;

    private AfterAbortEvent afterAbortEvent;

    @Override
    public void onBeforeStartEvent(BeforeStartEvent event) {
        this.beforeStartEvent = event;
    }

    @Override
    public void onAfterEndEvent(AfterEndEvent event) {
        this.afterEndEvent = event;
    }

    @Override
    public void onAfterAbortEvent(AfterAbortEvent event) {
        this.afterAbortEvent = event;
    }

    public void reset() {
        this.beforeStartEvent = null;
        this.afterEndEvent = null;
        this.afterAbortEvent = null;
    }

    public BeforeStartEvent getBeforeStartEvent() {
        return beforeStartEvent;
    }

    public AfterEndEvent getAfterEndEvent() {
        return afterEndEvent;
    }

    public AfterAbortEvent getAfterAbortEvent() {
        return afterAbortEvent;
    }
}
