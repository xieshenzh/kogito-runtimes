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
package org.kie.kogito.quarkus.decisions.deployment;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.quarkus.common.deployment.KogitoCompilationProvider;

public class DecisionsCompilationProvider extends KogitoCompilationProvider {

    @Override
    public Set<String> handledExtensions() {
        return Collections.singleton(".dmn");
    }

    @Override
    protected Generator getGenerator(KogitoBuildContext context, Set<File> filesToCompile, Context quarkusContext) {
        Path path = quarkusContext.getProjectDirectory().toPath().resolve("src").resolve("main").resolve("resources");
        return DecisionCodegen.ofCollectedResources(
                context,
                CollectedResourceProducer.fromDirectory(path));
    }
}
