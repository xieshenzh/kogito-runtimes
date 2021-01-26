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

package org.kie.kogito.events.mongodb.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.kie.kogito.services.event.ProcessInstanceDataEvent;
import org.kie.kogito.services.event.UserTaskInstanceDataEvent;
import org.kie.kogito.services.event.VariableInstanceDataEvent;

public class EventMongoDBCodecProvider implements CodecProvider {

    private final ProcessInstanceDataEventCodec processInstanceDataEventCodec = new ProcessInstanceDataEventCodec();
    private final UserTaskInstanceDataEventCodec userTaskInstanceDataEventCodec = new UserTaskInstanceDataEventCodec();
    private final VariableInstanceDataEventCodec variableInstanceDataEventCodec = new VariableInstanceDataEventCodec();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == ProcessInstanceDataEvent.class) {
            return (Codec<T>) processInstanceDataEventCodec;
        }
        if (aClass == UserTaskInstanceDataEvent.class) {
            return (Codec<T>) userTaskInstanceDataEventCodec;
        }
        if (aClass == VariableInstanceDataEvent.class) {
            return (Codec<T>) variableInstanceDataEventCodec;
        }
        return null;
    }
}
