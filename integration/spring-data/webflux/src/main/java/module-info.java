/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.spring.data.webflux {
    requires transitive spring.webflux;
    requires transitive com.blazebit.persistence.integration.spring.data;
    requires com.blazebit.persistence.integration.jackson;
    requires com.fasterxml.jackson.databind;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.data.commons;
    requires reactor.core;
    requires com.blazebit.common.utils;
    requires com.fasterxml.jackson.core;
    requires org.reactivestreams;
    requires org.slf4j;
    exports com.blazebit.persistence.spring.data.webflux;
}