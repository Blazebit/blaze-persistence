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
