/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.integration.quarkus.deployment.entity.Document;
import com.blazebit.persistence.integration.quarkus.deployment.resource.DocumentResource;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentView;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class HotReloadTest {

    @RegisterExtension
    final static QuarkusDevModeTest RUNNER = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Document.class, DocumentCreateView.class)
                    .addClasses(DocumentResource.class)
                    .addAsResource("application.properties")
                    .addAsResource("META-INF/persistence.xml")
            );

    @Test
    public void testAddNewEntityView() {
        RUNNER.addSourceFile(DocumentView.class);
        try {
            System.out.println("sleeping");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String documentName = "test1";
//        JsonPath result = RestAssured.given().body("{ \"name\": \"" + documentName + "\" }").contentType(ContentType.JSON)
//                .when().post("/documents")
//                .then().contentType(ContentType.JSON).extract().response().jsonPath();
        String result = RestAssured.given().body("{ \"name\": \"" + documentName + "\" }").contentType(ContentType.JSON)
                .when().post("/documents")
                .then().extract().response().asString();
        System.out.println(result);
//        assertEquals(documentName, result.getString("name"));
    }
}
