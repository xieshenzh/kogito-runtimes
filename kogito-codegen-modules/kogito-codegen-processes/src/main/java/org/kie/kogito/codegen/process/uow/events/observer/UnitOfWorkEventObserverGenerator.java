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

package org.kie.kogito.codegen.process.uow.events.observer;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.uow.events.AbstractUnitOfWorkEventResourceGenerator;

public class UnitOfWorkEventObserverGenerator extends AbstractUnitOfWorkEventResourceGenerator {

    private static final String TARGET_TYPE_NAME = "UnitOfWorkEventObserver";

    private static final String TEMPLATE_PATH = "/class-templates/uow/events/observer";

    public UnitOfWorkEventObserverGenerator(KogitoBuildContext context) {
        super(context, TARGET_TYPE_NAME, TEMPLATE_PATH);
    }
}
