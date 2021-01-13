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

package org.kie.kogito.codegen.process.uow.events.publisher;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.SpringBootKogitoBuildContext;
import org.kie.kogito.uow.events.UnitOfWorkEvent;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnitOfWorkEventPublisherGeneratorTest {

    @Test
    void generate_quarkus() {
        KogitoBuildContext context = QuarkusKogitoBuildContext.builder().build();
        UnitOfWorkEventPublisherGenerator generator = new UnitOfWorkEventPublisherGenerator(context);

        String generated = generator.generate();
        assertNotNull(generated);

        ClassOrInterfaceDeclaration clazz = StaticJavaParser
                .parse(generated)
                .getClassByName("UnitOfWorkEventPublisherImpl")
                .orElseThrow(() -> new IllegalArgumentException("Class does not exists"));
        assertNotNull(clazz);

        assertThat(clazz.getImplementedTypes()
                .stream().filter(t -> "UnitOfWorkEventPublisher".equals(t.getName().getIdentifier()))
                .count()).isEqualTo(1L);

        assertThat(clazz.getFieldByName("publisher")
                .filter(f -> f.getAnnotationByName("Inject").isPresent())
                .map(f -> f.getVariables().stream().map(v -> ((ClassOrInterfaceType) v.getType()))
                        .filter(t -> "Event".equals(t.getName().getIdentifier())).map(ClassOrInterfaceType::getTypeArguments).filter(Optional::isPresent)
                        .map(t -> t.get().stream().filter(a -> "UnitOfWorkEvent".equals(((ClassOrInterfaceType) a).getName().getIdentifier())))
                        .count()).get()).isEqualTo(1L);

        assertThat(clazz.getMethodsByName("publish").stream()
                .map(m -> m.getParameterByType(UnitOfWorkEvent.class.getCanonicalName()))
                .count()).isEqualTo(1L);
    }

    @Test
    void generate_spring() {
        KogitoBuildContext context = SpringBootKogitoBuildContext.builder().build();
        UnitOfWorkEventPublisherGenerator generator = new UnitOfWorkEventPublisherGenerator(context);

        String generated = generator.generate();
        assertNotNull(generated);

        ClassOrInterfaceDeclaration clazz = StaticJavaParser
                .parse(generated)
                .getClassByName("UnitOfWorkEventPublisherImpl")
                .orElseThrow(() -> new IllegalArgumentException("Class does not exists"));
        assertNotNull(clazz);

        assertThat(clazz.getImplementedTypes()
                .stream().filter(t -> "UnitOfWorkEventPublisher".equals(t.getName().getIdentifier()))
                .count()).isEqualTo(1L);

        assertThat(clazz.getFieldByName("publisher")
                .filter(f -> f.getAnnotationByName("Autowired").isPresent())
                .map(f -> f.getVariables().stream().map(v -> ((ClassOrInterfaceType) v.getType()))
                        .filter(t -> "ApplicationEventPublisher".equals(t.getName().getIdentifier()))
                        .count()).get()).isEqualTo(1L);

        assertThat(clazz.getMethodsByName("publish").stream()
                .map(m -> m.getParameterByType(UnitOfWorkEvent.class.getCanonicalName()))
                .count()).isEqualTo(1L);
    }


    @Test
    void generatedFilePath() {
        KogitoBuildContext context = QuarkusKogitoBuildContext.builder().build();
        UnitOfWorkEventPublisherGenerator generator = new UnitOfWorkEventPublisherGenerator(context);
        assertEquals("org/kie/kogito/app/UnitOfWorkEventPublisherImpl.java", generator.generatedFilePath());
    }
}