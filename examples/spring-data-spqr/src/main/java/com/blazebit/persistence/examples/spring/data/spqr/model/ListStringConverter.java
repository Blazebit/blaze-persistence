/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class ListStringConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        String[] strings = s.split("\\.");
        List<String> list = new ArrayList<>(strings.length);
        for (String element : strings) {
            list.add(element);
        }
        return list;
    }
}
