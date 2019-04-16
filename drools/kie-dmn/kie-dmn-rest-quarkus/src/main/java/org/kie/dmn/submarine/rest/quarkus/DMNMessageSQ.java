/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.submarine.rest.quarkus;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNMessageType;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.internal.builder.InternalMessage;

public class DMNMessageSQ implements Serializable, DMNMessage {

    public static enum DMNMessageSeverityKS {
        INFO,
        WARN,
        ERROR;

        public static DMNMessageSeverityKS of(Severity value) {
            switch (value) {
                case ERROR:
                    return DMNMessageSeverityKS.ERROR;
                case INFO:
                    return DMNMessageSeverityKS.INFO;
                case TRACE:
                    return DMNMessageSeverityKS.INFO;
                case WARN:
                    return DMNMessageSeverityKS.WARN;
                default:
                    return DMNMessageSeverityKS.ERROR;
            }
        }

        public Severity asSeverity() {
            switch (this) {
                case ERROR:
                    return Severity.ERROR;
                case INFO:
                    return Severity.INFO;
                case WARN:
                    return Severity.WARN;
                default:
                    return Severity.ERROR;
            }
        }
    }

    @JsonbProperty("dmn-message-severity")
    private DMNMessageSeverityKS severity;

    @JsonbProperty("message")
    private String message;

    @JsonbProperty("message-type")
    private DMNMessageType messageType;

    @JsonbProperty("source-id")
    private String sourceId;

    public DMNMessageSQ() {
        // Intentionally blank.
    }

    public static DMNMessageSQ of(DMNMessage value) {
        DMNMessageSQ res = new DMNMessageSQ();
        res.severity = DMNMessageSeverityKS.of(value.getSeverity());
        res.message = value.getMessage();
        res.messageType = value.getMessageType();
        res.sourceId = value.getSourceId();
        return res;
    }

    @Override
    public Severity getSeverity() {
        return severity.asSeverity();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public DMNMessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @JsonbTransient
    @Override
    public Throwable getException() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public FEELEvent getFeelEvent() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public Object getSourceReference() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public String getKieBaseName() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public InternalMessage setKieBaseName(String kieBaseName) {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public long getId() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public String getPath() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public int getLine() {
        throw new UnsupportedOperationException();
    }

    @JsonbTransient
    @Override
    public int getColumn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Level getLevel() {
        switch (severity) {
            case ERROR:
                return Level.ERROR;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            default:
                return Level.ERROR;
        }
    }

    @JsonbTransient
    @Override
    public String getText() {
        throw new UnsupportedOperationException();
    }
}
