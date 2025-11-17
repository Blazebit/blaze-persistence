/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.spring.data {
    requires transitive spring.context;
    requires transitive spring.data.jpa;
    requires transitive com.blazebit.persistence.criteria;
    requires com.blazebit.persistence.view;
    requires spring.data.commons;
    requires spring.core;
    requires com.blazebit.persistence.core.parser;
    requires spring.tx;
    requires spring.aop;
    requires spring.beans;
    requires com.blazebit.common.utils;
    exports com.blazebit.persistence.spring.data.annotation;
    exports com.blazebit.persistence.spring.data.base;
    exports com.blazebit.persistence.spring.data.base.query;
    exports com.blazebit.persistence.spring.data.base.repository;
    exports com.blazebit.persistence.spring.data.repository;
    exports com.blazebit.persistence.spring.data.repository.config;
}