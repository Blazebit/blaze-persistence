/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.graphql.dgs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleTest extends AbstractSampleTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSanity() {
        this.restTemplate.getForObject("/graphql/schema.json", String.class);
    }

    @Test
    public void testRequestScope() {
        String requestGraphQL = request(5, null);
        ResponseEntity<JsonNode> response = sendGraphQlRequest(requestGraphQL);
        JsonNode connection = response.getBody().get("data").get("findAll");
        ArrayNode arrayNode = (ArrayNode) connection.get("edges");
        List<JsonNode> nodes = arrayNode.findValues("node");

        assertEquals(5, nodes.size());
        assertEquals("Cat 0", nodes.get(0).get("name").asText());

        requestGraphQL = request(5, connection.get("pageInfo").get("endCursor").asText());
        response = sendGraphQlRequest(requestGraphQL);
        connection = response.getBody().get("data").get("findAll");
        arrayNode = (ArrayNode) connection.get("edges");
        nodes = arrayNode.findValues("node");

        assertEquals(5, nodes.size());
        assertEquals("Cat 5", nodes.get(0).get("name").asText());
    }

    @Test
    public void testCreate() {
        String requestGraphQL = "mutation {\n" +
                "  createCat(\n" +
                "    cat: {\n" +
                "      name: \"Test\"\n" +
                "      age: 1\n" +
                "      owner: {id: 1}\n" +
                "      kittens: [\n" +
                "        { name: \"Kitten 1\", age: 1, owner: {id: 1}}\n" +
                "      ]\n" +
                "  \t}\n" +
                "  )\n" +
                "}";
        ResponseEntity<JsonNode> response = sendGraphQlRequest(requestGraphQL);
        int id = response.getBody().get("data").get("createCat").asInt();

        requestGraphQL = "query { catById(id: " + id + ") { name } }";
        response = sendGraphQlRequest(requestGraphQL);

        String name = response.getBody().get("data").get("catById").get("name").asText();
        assertEquals("Test", name);
    }

    @Test
    public void shouldFindAllWithTypeName() {
        String requestGraphQL =  "query { "
          + "  findAll(first: 3) { "
          + "    edges { "
          + "      node {  "
          + "        id,"
          + "        name,"
          + "        __typename,"
          + "      }"
          + "    }"
          + "  }"
          + "}";
          ResponseEntity<JsonNode> response = sendGraphQlRequest(requestGraphQL);
          List<JsonNode> nodes = getResponseNodes(response);
          nodes.forEach(node -> {
              assertThat(node.get("id")).isNotNull();
              assertThat(node.get("name").asText()).contains("Cat");
              assertThat(node.get("__typename").asText()).isEqualTo("CatWithOwnerView");
          });
    }

    @Test
    public void shouldFindAllWithOutTypeName() {
        String requestGraphQL =  "query { "
          + "  findAll(first: 3) { "
          + "    edges { "
          + "      node {  "
          + "        id,"
          + "        name,"
          + "      }"
          + "    }"
          + "  }"
          + "}";
        ResponseEntity<JsonNode> response = sendGraphQlRequest(requestGraphQL);
        List<JsonNode> nodes = getResponseNodes(response);
        nodes.forEach(node -> {
          assertThat(node.get("id")).isNotNull();
          assertThat(node.get("name").asText()).contains("Cat");
        });
    }

    private List<JsonNode> getResponseNodes(ResponseEntity<JsonNode> response ) {
        JsonNode connection = response.getBody().get("data").get("findAll");
        ArrayNode arrayNode = (ArrayNode) connection.get("edges");
        return arrayNode.findValues("node");
    }

    private ResponseEntity<JsonNode> sendGraphQlRequest(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql");
        return this.restTemplate.postForEntity("/graphql", new HttpEntity<>(query, headers), JsonNode.class);
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
