/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.criteria.impl {
    requires com.blazebit.common.utils;
    requires transitive com.blazebit.persistence.criteria;
    requires jakarta.persistence;
    requires com.blazebit.persistence.core.parser;
    exports com.blazebit.persistence.criteria.impl;
    provides com.blazebit.persistence.criteria.spi.BlazeCriteriaBuilderFactory with com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderFactoryImpl;
}