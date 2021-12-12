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

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.JsonbException;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

/**
 * Adapter for {@link JsonParser}, that reads a {@link JsonValue} content tree instead of JSON text.
 */
public class JsonValueToParserAdapter implements JsonParser {

    private final JsonValue value;
    private boolean hasNext;

    /**
     * Creates new {@link JsonValue} parser.
     *
     * @param value json value
     */
    public JsonValueToParserAdapter(JsonValue value) {
        this.value = value;
        this.hasNext = true;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public Event next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        hasNext = false;
        switch (value.getValueType()) {
            case TRUE:
                return Event.VALUE_TRUE;
            case FALSE:
                return Event.VALUE_FALSE;
            case NUMBER:
                return Event.VALUE_NUMBER;
            case STRING:
                return Event.VALUE_STRING;
            case NULL:
                return Event.VALUE_NULL;
            default:
                throw new UnsupportedOperationException("Unsupported value: " + value);
        }
    }

    @Override
    public String getString() {
        if (hasNext) {
            throw new NoSuchElementException();
        }

        switch (value.getValueType()) {
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NUMBER:
                return getJsonNumberValue().toString();
            case STRING:
                return ((JsonString) value).getString();
            case NULL:
                return "null";
            default:
                throw new UnsupportedOperationException("Unsupported value: " + value);
        }
    }

    @Override
    public boolean isIntegralNumber() {
        return getJsonNumberValue().isIntegral();
    }

    @Override
    public int getInt() {
        return getJsonNumberValue().intValueExact();
    }

    @Override
    public long getLong() {
        return getJsonNumberValue().longValueExact();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return getJsonNumberValue().bigDecimalValue();
    }

    private JsonNumber getJsonNumberValue() {
        if (hasNext) {
            throw new NoSuchElementException();
        }
        if (value.getValueType() != JsonValue.ValueType.NUMBER) {
            throw new UnsupportedOperationException("Unsupported value: " + value);
        }
        return (JsonNumber) value;
    }

    @Override
    public JsonLocation getLocation() {
        throw new JsonbException("Operation not supported");
    }

    @Override
    public void close() {
        //noop
    }
}
