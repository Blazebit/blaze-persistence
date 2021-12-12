/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package com.blazebit.persistence.integration.jsonb.jsonstructure;

import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.JsonbException;
import javax.json.stream.JsonParser;
import java.util.Iterator;

/**
 * Iterates over {@link JsonArray}.
 */
public class JsonArrayIterator extends JsonStructureIterator {

    private final JsonArray jsonArray;
    private final Iterator<JsonValue> valueIterator;

    private JsonValue currentValue;

    /**
     * Creates new array iterator.
     *
     * @param jsonArray json array
     */
    public JsonArrayIterator(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
        this.valueIterator = jsonArray.iterator();
    }

    /**
     * After {@link JsonParser.Event} END_ARRAY is returned from next() iterator is removed from the stack.
     *
     * @return always true
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public JsonParser.Event next() {
        if (valueIterator.hasNext()) {
            currentValue = valueIterator.next();
            return getValueEvent(currentValue);
        }
        return JsonParser.Event.END_ARRAY;
    }

    @Override
    JsonValue getValue() {
        return currentValue;
    }

    @Override
    JsonbException createIncompatibleValueError() {
        return new JsonbException("Incompatible array type: " + getValue().getValueType());
    }

    @Override
    String getString() {
        if (currentValue instanceof JsonString) {
            return ((JsonString) currentValue).getString();
        }
        return currentValue.toString();
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }
}
