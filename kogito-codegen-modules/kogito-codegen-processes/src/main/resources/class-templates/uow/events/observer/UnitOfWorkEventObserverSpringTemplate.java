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

@org.springframework.stereotype.Component
public class UnitOfWorkEventObserver {

    @org.springframework.beans.factory.annotation.Autowired(required=false)
    private java.util.List<org.kie.kogito.uow.events.UnitOfWorkEventListener> eventListeners;

    @org.springframework.context.event.EventListener
    void onBeforeStartEvent(org.kie.kogito.uow.events.BeforeStartEvent event) {
        if (eventListeners != null) {
            eventListeners.forEach(l -> l.onBeforeStartEvent(event));
        }
    }

    @org.springframework.context.event.EventListener
    void onAfterEndEvent(org.kie.kogito.uow.events.AfterEndEvent event) {
        if (eventListeners != null) {
            eventListeners.forEach(l -> l.onAfterEndEvent(event));
        }
    }

    @org.springframework.context.event.EventListener
    void onAfterAbortEvent(org.kie.kogito.uow.events.AfterAbortEvent event) {
        if (eventListeners != null) {
            eventListeners.forEach(l -> l.onAfterAbortEvent(event));
        }
    }
}
