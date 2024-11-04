/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.hibernate6.entity;

public interface Property<T> {

    String getName();

    T getValue();
}
