/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.repository;

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
import static org.junit.Assert.assertNull;

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
        assertEquals("Person 0", nodes.get(0).get("owner").get("name").asText());
        assertEquals("Boy 0", nodes.get(0).get("owner").get("children").get(0).get("name").asText());
        assertEquals("Girl 0", nodes.get(0).get("owner").get("children").get(1).get("name").asText());
        assertEquals("Doll 0", nodes.get(0).get("owner").get("children").get(1).get("dollName").asText());

        requestGraphQL = request(5, connection.get("pageInfo").get("endCursor").asText());
        response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
        connection = response.getBody().get("data").get("findAll");
        arrayNode = (ArrayNode) connection.get("edges");
        nodes = arrayNode.findValues("node");

        assertEquals(5, nodes.size());
        assertEquals("Cat 5", nodes.get(0).get("name").asText());
        assertEquals("def", nodes.get(0).get("theData").asText());
        assertEquals("abc", nodes.get(0).get("sampleData").asText());
    }

    @Test
    public void testCreate() {
        String requestGraphQL = "mutation {\n" +
                "  createCat(\n" +
                "    cat: {\n" +
                "      name: \"Test\"\n" +
                "      age: 1\n" +
                // FIXME: don't rely on DB generated ids
                "      owner: {id: 3}\n" +
                "      kittens: [\n" +
                "        { name: \"Kitten 1\", age: 1, owner: {id: 3}}\n" +
                "      ]\n" +
                "  \t}\n" +
                "  )\n" +
                "}";
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql");
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);

        int id = response.getBody().get("data").get("createCat").asInt();

        requestGraphQL = "query { catById(id: " + id + ") { name owner { __typename } } }";
        response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
        String name = response.getBody().get("data").get("catById").get("name").asText();
        assertEquals("Test", name);
        assertEquals("PersonSimpleView", response.getBody().get("data").get("catById").get("owner").get("__typename").asText());
    }

    @Test
    public void testCreateInheritance() {
        String requestGraphQL = "mutation {\n" +
                "  addPersonChild(\n" +
                "    child: {\n" +
                "      boy: { name: \"Test\" }\n" +
                "    },\n" +
                "    personId: 3\n" +
                "  )\n" +
                "}";
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql");
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity("/graphql", new HttpEntity<>(requestGraphQL, headers), JsonNode.class);
        assertNull(response.getBody().get("errors"));
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
                "        nicknames\n" +
                "        theData\n" +
                "        sampleData\n" +
                "        owner {\n" +
                "          name\n" +
                "          children {\n" +
                "            ... on Boy { name }\n" +
                "            ... on Girl { name dollName }\n" +
                "            __typename\n" +
                "          }\n" +
                "        }\n" +
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
