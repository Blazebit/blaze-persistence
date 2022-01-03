/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.integration.quarkus.deployment;

import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.integration.quarkus.deployment.entity.Document;
import com.blazebit.persistence.integration.quarkus.deployment.entity.Person;
import com.blazebit.persistence.integration.quarkus.deployment.listener.DocumentPostPersistEntityListener;
import com.blazebit.persistence.integration.quarkus.deployment.resource.DocumentResource;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.IdMapping;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class QuarkusTest {

    @RegisterExtension
    final static QuarkusUnitTest RUNNER = new QuarkusUnitTest()
            .setArchiveProducer(() -> {
                        Class<?>[] views = {DocumentView.class, DocumentCreateView.class};
                        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
                                .addClasses(Document.class, Person.class)
                                .addClasses(views)
                                .addClasses(DocumentPostPersistEntityListener.class)
                                .addClasses(DocumentResource.class)
                                .addAsResource("application.properties")
                                .addAsResource("META-INF/persistence.xml");
                        for (Class<?> view : views) {
                            addStaticGeneratedClasses(javaArchive, view);
                        }

                        return javaArchive;
                    }
            );

    private static void addStaticGeneratedClasses(JavaArchive javaArchive, Class<?> view) {
        try {
            javaArchive.addClasses(
                    view.getClassLoader().loadClass(view.getPackage().getName() + "." + view.getSimpleName().replace("$", "") + "_"),
                    view.getClassLoader().loadClass(view.getPackage().getName() + "." + view.getSimpleName().replace("$", "") + "Relation"),
                    view.getClassLoader().loadClass(view.getPackage().getName() + "." + view.getSimpleName().replace("$", "") + "MultiRelation"),
                    view.getClassLoader().loadClass(view.getPackage().getName() + "." + view.getSimpleName().replace("$", "") + "Impl"),
                    view.getClassLoader().loadClass(view.getPackage().getName() + "." + view.getSimpleName().replace("$", "") + "Builder")
            );
        } catch (ClassNotFoundException e) {
            // Ignore
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private EntityManager em;
    @Inject
    private EntityViewManager evm;

    @PostConstruct
    void init() {
        new EntityViewAwareObjectMapper(evm, objectMapper, null);
    }

    @Test
    @Transactional
    public void simpleTest() {
        Document d = new Document();
        em.persist(d);
        DocumentView documentView = evm.find(em, DocumentView.class, d.getId());
        assertEquals(d.getId(), documentView.getId());
    }

    @Test
    public void testDocumentResource() throws JsonProcessingException {
        DocumentCreateView documentCreateView = evm.create(DocumentCreateView.class);
        documentCreateView.setName("test1");
        DocumentView result = objectMapper.readValue(RestAssured.given().body(toJsonWithoutId(documentCreateView)).contentType(ContentType.JSON)
                .when().post("/documents")
                .then().contentType(ContentType.JSON).extract().response().asString(), DocumentView.class);
        assertEquals(documentCreateView.getName(), result.getName());
        assertEquals(1, DocumentPostPersistEntityListener.PERSIST_COUNTER);
    }

    private byte[] toJsonWithoutId(Object entityView) throws JsonProcessingException {
        VisibilityChecker<?> old = objectMapper.getVisibilityChecker();
        objectMapper.setVisibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT) {
            @Override
            public boolean isGetterVisible(AnnotatedMethod m) {
                return !m.hasAnnotation(IdMapping.class) && super.isGetterVisible(m);
            }
        });
        byte[] result = objectMapper.writeValueAsBytes(entityView);
        objectMapper.setVisibility(old);
        return result;
    }
}
