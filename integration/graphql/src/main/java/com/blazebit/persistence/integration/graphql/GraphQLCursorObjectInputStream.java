/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Set;


/**
 * An {@link ObjectInputStream} implementation for deserializing {@link GraphQLCursor} that allows to deserialize only certain basic types.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLCursorObjectInputStream extends ObjectInputStream {

    private final Set<String> serializableBasicTypes;

    /**
     * Creates a new input stream reading from the given input stream and allowing only the given basic types.
     *
     * @param in The input stream to read from
     * @param serializableBasicTypes The allowed set of fully qualified class names for deserialization
     * @throws IOException When an IO error occurs
     */
    public GraphQLCursorObjectInputStream(InputStream in, Set<String> serializableBasicTypes) throws IOException {
        super(in);
        this.serializableBasicTypes = serializableBasicTypes;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        if (serializableBasicTypes.contains(desc.getName())) {
            return super.resolveClass(desc);
        }
        throw new IllegalArgumentException("Illegal attempt to deserialize disallowed type: " + desc.getName());
    }
}
