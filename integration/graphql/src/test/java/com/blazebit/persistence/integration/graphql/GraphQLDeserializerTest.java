/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
