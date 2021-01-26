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

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.services.event.UserTaskInstanceDataEvent;
import org.kie.kogito.services.event.impl.ProcessInstanceEventBody;
import org.kie.kogito.services.event.impl.UserTaskInstanceEventBody;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserTaskInstanceDataEventCodecTest {

    private UserTaskInstanceDataEventCodec codec;

    private UserTaskInstanceDataEvent event;

    @BeforeEach
    void setUp() {
        codec = new UserTaskInstanceDataEventCodec();

        String source = "testSource";
        String kogitoProcessinstanceId = "testKogitoProcessinstanceId";
        String kogitoRootProcessinstanceId = "testKogitoRootProcessinstanceId";
        String kogitoProcessId = "testKogitoProcessId";
        String kogitoRootProcessId = "testKogitoRootProcessId";
        String kogitoAddons = "testKogitoAddons";
        String kogitoUserTaskinstanceId = "testKogitoUserTaskinstanceId";
        String kogitoUserTaskinstanceState = "testKogitoUserTaskinstanceState";
        String id = "testId";
        String taskName = "testTaskName";
        String taskDescription = "testTaskDescription";
        String taskPriority = "testTaskPriority";
        String referenceName = "testReferenceName";
        Date startDate = new Date();
        Date completeDate = new Date();
        String state = "testState";
        String actualOwner = "testActualOwner";
        Set<String> potentialUsers = Collections.singleton("testPotentialUsers");
        Set<String> potentialGroups = Collections.singleton("testPotentialGroups");
        Set<String> excludedUsers = Collections.singleton("testExcludedUsers");
        Set<String> adminUsers = Collections.singleton("testAdminUsers");
        Set<String> adminGroups = Collections.singleton("testAdminGroups");
        Map<String, Object> inputs = Collections.singletonMap("testInputsKey", "testInputsValue");
        Map<String, Object> outputs = Collections.singletonMap("testOutputsKey", "testOutputsValue");

        Map<String, String> metaData = new HashMap<>();
        metaData.put(ProcessInstanceEventBody.ID_META_DATA, kogitoProcessinstanceId);
        metaData.put(ProcessInstanceEventBody.ROOT_ID_META_DATA, kogitoRootProcessinstanceId);
        metaData.put(ProcessInstanceEventBody.PROCESS_ID_META_DATA, kogitoProcessId);
        metaData.put(ProcessInstanceEventBody.ROOT_PROCESS_ID_META_DATA, kogitoRootProcessId);
        metaData.put(UserTaskInstanceEventBody.UT_STATE_META_DATA, kogitoUserTaskinstanceState);
        metaData.put(UserTaskInstanceEventBody.UT_ID_META_DATA, kogitoUserTaskinstanceId);

        UserTaskInstanceEventBody body = mock(UserTaskInstanceEventBody.class);
        when(body.getId()).thenReturn(id);
        when(body.getTaskName()).thenReturn(taskName);
        when(body.getTaskDescription()).thenReturn(taskDescription);
        when(body.getTaskPriority()).thenReturn(taskPriority);
        when(body.getReferenceName()).thenReturn(referenceName);
        when(body.getStartDate()).thenReturn(startDate);
        when(body.getCompleteDate()).thenReturn(completeDate);
        when(body.getState()).thenReturn(state);
        when(body.getActualOwner()).thenReturn(actualOwner);
        when(body.getPotentialUsers()).thenReturn(potentialUsers);
        when(body.getPotentialGroups()).thenReturn(potentialGroups);
        when(body.getExcludedUsers()).thenReturn(excludedUsers);
        when(body.getAdminUsers()).thenReturn(adminUsers);
        when(body.getAdminGroups()).thenReturn(adminGroups);
        when(body.getInputs()).thenReturn(inputs);
        when(body.getOutputs()).thenReturn(outputs);
        when(body.getProcessInstanceId()).thenReturn(kogitoProcessinstanceId);
        when(body.getRootProcessInstanceId()).thenReturn(kogitoRootProcessinstanceId);
        when(body.getProcessId()).thenReturn(kogitoProcessId);
        when(body.getRootProcessId()).thenReturn(kogitoRootProcessId);

        event = new UserTaskInstanceDataEvent(source, kogitoAddons, metaData, body);
    }

    @Test
    void generateIdIfAbsentFromDocument() {
        assertEquals(event, codec.generateIdIfAbsentFromDocument(event));
    }

    @Test
    void documentHasId() {
        assertTrue(codec.documentHasId(event));
    }

    @Test
    void getDocumentId() {
        assertEquals(new BsonString(event.getId()), codec.getDocumentId(event));
    }

    @Test
    void decode() {
        assertNull(codec.decode(mock(BsonReader.class), DecoderContext.builder().build()));
    }

    @Test
    void encode() {
        try (MockedStatic<CodecUtils> codecUtils = mockStatic(CodecUtils.class)) {
            Codec<Document> mockCodec = mock(Codec.class);
            codecUtils.when(CodecUtils::codec).thenReturn(mockCodec);
            codecUtils.when(() -> CodecUtils.encodeDataEvent(any(), any())).thenCallRealMethod();
            BsonWriter writer = mock(BsonWriter.class);
            EncoderContext context = EncoderContext.builder().build();

            codec.encode(writer, event, context);

            ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
            verify(mockCodec, times(1)).encode(eq(writer), captor.capture(), eq(context));
            Document doc = captor.getValue();

            assertEquals(event.getId(), doc.get(ID));
            assertEquals(event.getSpecVersion(), doc.get("specVersion"));
            assertEquals(event.getSource(), doc.get("source"));
            assertEquals(event.getType(), doc.get("type"));
            assertEquals(event.getTime(), doc.get("time"));
            assertEquals(event.getSubject(), doc.get("subject"));
            assertEquals(event.getDataContentType(), doc.get("dataContentType"));
            assertEquals(event.getDataSchema(), doc.get("dataSchema"));
            assertEquals(event.getKogitoProcessinstanceId(), doc.get("kogitoProcessinstanceId"));
            assertEquals(event.getKogitoRootProcessinstanceId(), doc.get("kogitoRootProcessinstanceId"));
            assertEquals(event.getKogitoProcessId(), doc.get("kogitoProcessId"));
            assertEquals(event.getKogitoRootProcessId(), doc.get("kogitoRootProcessId"));
            assertEquals(event.getKogitoAddons(), doc.get("kogitoAddons"));
            assertEquals(event.getKogitoUserTaskinstanceId(), doc.get("kogitoUserTaskinstanceId"));
            assertEquals(event.getKogitoUserTaskinstanceState(), doc.get("kogitoUserTaskinstanceState"));

            assertEquals(event.getData().getId(), ((Document) doc.get("data")).get("id"));
            assertEquals(event.getData().getTaskName(), ((Document) doc.get("data")).get("taskName"));
            assertEquals(event.getData().getTaskDescription(), ((Document) doc.get("data")).get("taskDescription"));
            assertEquals(event.getData().getTaskPriority(), ((Document) doc.get("data")).get("taskPriority"));
            assertEquals(event.getData().getReferenceName(), ((Document) doc.get("data")).get("referenceName"));
            assertEquals(event.getData().getStartDate(), ((Document) doc.get("data")).get("startDate"));
            assertEquals(event.getData().getCompleteDate(), ((Document) doc.get("data")).get("completeDate"));
            assertEquals(event.getData().getState(), ((Document) doc.get("data")).get("state"));
            assertEquals(event.getData().getActualOwner(), ((Document) doc.get("data")).get("actualOwner"));
            assertEquals(event.getData().getPotentialUsers(), ((Document) doc.get("data")).get("potentialUsers"));
            assertEquals(event.getData().getPotentialGroups(), ((Document) doc.get("data")).get("potentialGroups"));
            assertEquals(event.getData().getExcludedUsers(), ((Document) doc.get("data")).get("excludedUsers"));
            assertEquals(event.getData().getAdminUsers(), ((Document) doc.get("data")).get("adminUsers"));
            assertEquals(event.getData().getAdminGroups(), ((Document) doc.get("data")).get("adminGroups"));
            assertEquals(new Document(event.getData().getInputs()), ((Document) doc.get("data")).get("inputs"));
            assertEquals(new Document(event.getData().getOutputs()), ((Document) doc.get("data")).get("outputs"));
            assertEquals(event.getData().getProcessInstanceId(), ((Document) doc.get("data")).get("processInstanceId"));
            assertEquals(event.getData().getRootProcessInstanceId(), ((Document) doc.get("data")).get("rootProcessInstanceId"));
            assertEquals(event.getData().getProcessId(), ((Document) doc.get("data")).get("processId"));
            assertEquals(event.getData().getRootProcessId(), ((Document) doc.get("data")).get("rootProcessId"));
        }
    }

    @Test
    void getEncoderClass() {
        assertTrue(codec.getEncoderClass().isAssignableFrom(UserTaskInstanceDataEvent.class));
    }
}