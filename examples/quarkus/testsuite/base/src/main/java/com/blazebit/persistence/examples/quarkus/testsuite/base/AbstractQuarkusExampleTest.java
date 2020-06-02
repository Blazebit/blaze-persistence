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
package com.blazebit.persistence.examples.quarkus.testsuite.base;

import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractQuarkusExampleTest {

    @TestHTTPResource
    protected URI apiBaseUri;

    @Test
    public void createDocument() {
        given()
                .body("{\"name\": \"Doc1\", \"age\": 0}")
                .contentType(ContentType.JSON)
                .when().post("/documents")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern("http://localhost:" + apiBaseUri.getPort() + "/documents/.*"));
    }

    @Test
    public void getDocuments() {
        given()
                .body("{\"name\": \"Doc1\", \"age\": 1}")
                .contentType(ContentType.JSON)
                .when().post("/documents")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern("http://localhost:" + apiBaseUri.getPort() + "/documents/.*"));

        given()
                .queryParam("age", 1)
                .when().get("/documents")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    public void updateDocumentType() {
        given()
                .body("{\"id\": \"1\", \"name\": \"docType1\"}")
                .contentType(ContentType.JSON)
                .when().post("/document-types")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern("http://localhost:" + apiBaseUri.getPort() + "/document-types/1"));

        given()
                .body("{\"name\": \"docType1-new\"}")
                .contentType(ContentType.JSON)
                .when().put("/document-types/1")
                .then()
                .statusCode(200)
                .body("name", is("docType1-new"));
    }
}
