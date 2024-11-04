/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webflux.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class Filter {

    private Kind kind;
    private String field;
    private List<String> values;

    public Filter() {
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @JsonIgnore
    public String getValue() {
        return values.get(0);
    }

    public void setValue(String value) {
        if (values == null) {
            values = new ArrayList<>();
        } else {
            values.clear();
        }
        values.add(value);
    }

    @JsonIgnore
    public String getLow() {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public void setLow(String low) {
        if (values == null) {
            values = new ArrayList<>();
        } else if (values.size() > 2) {
            values = new ArrayList<>(values.subList(0, 2));
        }
        values.set(0, low);
    }

    @JsonIgnore
    public String getHigh() {
        if (values == null || values.size() < 2) {
            return null;
        }
        return values.get(1);
    }

    public void setHigh(String high) {
        if (values == null) {
            values = new ArrayList<>();
        } else if (values.size() > 2) {
            values = new ArrayList<>(values.subList(0, 2));
        }
        values.set(1, high);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public static enum Kind {
        EQ,
        LT,
        GT,
        GTE,
        LTE,
        BETWEEN,
        IN,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }
}
