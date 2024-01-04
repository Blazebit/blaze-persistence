/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.integration.graphql;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLDeserializerTest {

    @Test
    public void testValid() throws Exception {
        Set<String> allowedTypes = new HashSet<>();
        allowedTypes.add(Integer.class.getName());
        allowedTypes.add(Number.class.getName());
        allowedTypes.add(Serializable[].class.getName());

        Serializable[] array = new Serializable[]{ 1 };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(array);
        }

        Serializable[] readObject = (Serializable[]) new GraphQLCursorObjectInputStream(new ByteArrayInputStream(baos.toByteArray()), allowedTypes).readObject();
        Assert.assertArrayEquals(array, readObject);
    }

    @Test
    public void testInvalid() throws Exception {
        Set<String> allowedTypes = new HashSet<>();
        allowedTypes.add(Integer.class.getName());
        allowedTypes.add(Number.class.getName());
        allowedTypes.add(Serializable[].class.getName());

        Serializable[] array = new Serializable[]{ UUID.randomUUID() };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(array);
        }

        try {
            new GraphQLCursorObjectInputStream(new ByteArrayInputStream(baos.toByteArray()), allowedTypes).readObject();
            Assert.fail("Expected to fail deserializing of UUID");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Illegal attempt to deserialize disallowed type: java.util.UUID", ex.getMessage());
        }
    }
}
