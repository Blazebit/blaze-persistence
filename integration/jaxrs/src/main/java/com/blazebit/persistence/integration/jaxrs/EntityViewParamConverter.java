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

package com.blazebit.persistence.integration.jaxrs;

import com.fasterxml.jackson.databind.ObjectReader;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Provider
public class EntityViewParamConverter implements ParamConverter<Object> {

    private final ObjectReader reader;

    public EntityViewParamConverter(ObjectReader reader) {
        this.reader = reader;
    }

    public Object fromInputStream(InputStream value) {
        try {
            return reader.readValue(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Object fromString(String value) {
        try {
            return reader.readValue(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString(Object value) {
        throw new UnsupportedOperationException();
    }

}
