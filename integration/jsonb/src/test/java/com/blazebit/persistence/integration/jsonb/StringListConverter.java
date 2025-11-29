/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jsonb;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int size = value.size();
        sb.append(value.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(',').append(value.get(i));
        }
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(dbData.split(",")));
    }
}
