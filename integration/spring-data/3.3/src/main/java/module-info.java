/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.spring.data.impl {
    requires transitive com.blazebit.persistence.integration.spring.data;
    requires spring.data.commons;
    requires com.blazebit.persistence.view;
    requires jakarta.persistence;
    requires spring.expression;
    requires spring.core;
    requires com.blazebit.persistence.core.parser;
    requires spring.aop;
    requires spring.beans;
    requires spring.tx;
    exports com.blazebit.persistence.spring.data.impl.query;
    exports com.blazebit.persistence.spring.data.impl.repository;
}