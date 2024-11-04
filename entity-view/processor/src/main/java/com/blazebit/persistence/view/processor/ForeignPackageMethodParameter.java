/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class ForeignPackageMethodParameter {

    private final String name;
    private final String type;
    private final String realType;

    public ForeignPackageMethodParameter(String name, String type, String realType) {
        this.name = name;
        this.type = type;
        this.realType = realType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRealType() {
        return realType;
    }
}
