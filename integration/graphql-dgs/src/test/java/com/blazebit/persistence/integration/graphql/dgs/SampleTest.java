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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql");
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);

        JsonNode connection = response.getBody().get("data").get("findAll");
        ArrayNode arrayNode = (ArrayNode) connection.get("edges");
        List<JsonNode> nodes = arrayNode.findValues("node");

        assertEquals(5, nodes.size());
        assertEquals("Cat 0", nodes.get(0).get("name").asText());

        requestGraphQL = request(5, connection.get("pageInfo").get("endCursor").asText());
        response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
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
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql");
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);

        int id = response.getBody().get("data").get("createCat").asInt();

        requestGraphQL = "query { catById(id: " + id + ") { name } }";
        response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
        String name = response.getBody().get("data").get("catById").get("name").asText();
        assertEquals("Test", name);
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
