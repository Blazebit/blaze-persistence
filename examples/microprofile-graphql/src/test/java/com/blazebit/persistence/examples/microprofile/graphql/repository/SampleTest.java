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

package com.blazebit.persistence.examples.microprofile.graphql.repository;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import io.quarkus.test.common.http.TestHTTPResource;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class SampleTest extends AbstractSampleTest {

    @TestHTTPResource
    protected URI apiBaseUri;

    @AfterEach
    public void clearData() {
//        given().when().delete("/documents");
//        given().when().delete("/document-types");
//        given().when().delete("/persons");
    }

    @Test
    public void testSanity() {
        given()
                .when().get("/graphql/schema.json")
                .then()
                .statusCode(200);
    }

    @Test
    public void testRequestScope() {
        given()
                .contentType("application/graphql")
                .body(request(5, null))
                .when().post("/graphql")
                .then()
                .statusCode(200)
                .body("size(data.findAll.edges)", is(5));
//        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
//
//        JsonNode connection = response.getBody().get("data").get("findAll");
//        ArrayNode arrayNode = (ArrayNode) connection.get("edges");
//        List<JsonNode> nodes = arrayNode.findValues("node");
//
//        assertEquals(5, nodes.size());
//        assertEquals("Cat 0", nodes.get(0).get("name").asText());
//
//        requestGraphQL = request(5, connection.get("pageInfo").get("endCursor").asText());
//        response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
//        connection = response.getBody().get("data").get("findAll");
//        arrayNode = (ArrayNode) connection.get("edges");
//        nodes = arrayNode.findValues("node");
//
//        assertEquals(5, nodes.size());
//        assertEquals("Cat 5", nodes.get(0).get("name").asText());
    }

    static String request(int first, String after) {
        String other = "";
        if (after != null) {
            other = ", after: \"" + after + "\"";
        }
        String requestGraphQL = "query {\n" +
                "  findAll(first: " + first + other + "){\n" +
                "    edges {\n" +
                "      node {\n" +
                "        id\n" +
                "        name\n" +
                "      }\n" +
                "    }\n" +
                "    pageInfo {\n" +
                "      startCursor\n" +
                "      endCursor\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return requestGraphQL;
    }
}
