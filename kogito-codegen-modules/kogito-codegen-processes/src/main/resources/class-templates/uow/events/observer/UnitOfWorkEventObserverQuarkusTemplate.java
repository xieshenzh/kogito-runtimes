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

@javax.enterprise.context.ApplicationScoped
public class UnitOfWorkEventObserver {

    @javax.inject.Inject
    private javax.enterprise.inject.Instance<org.kie.kogito.uow.events.UnitOfWorkEventListener> eventListeners;

    void onBeforeStartEvent(@javax.enterprise.event.Observes org.kie.kogito.uow.events.BeforeStartEvent event) {
        eventListeners.forEach(l -> l.onBeforeStartEvent(event));
    }

    void onAfterEndEvent(@javax.enterprise.event.Observes org.kie.kogito.uow.events.AfterEndEvent event) {
        eventListeners.forEach(l -> l.onAfterEndEvent(event));
    }

    void onAfterAbortEvent(@javax.enterprise.event.Observes org.kie.kogito.uow.events.AfterAbortEvent event) {
        eventListeners.forEach(l -> l.onAfterAbortEvent(event));
    }
}
