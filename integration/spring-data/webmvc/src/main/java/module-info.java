/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.spring.data.webmvc {
    requires transitive spring.webmvc;
    requires transitive com.blazebit.persistence.integration.spring.data;
    requires com.blazebit.persistence.integration.jackson;
    requires spring.data.commons;
    requires spring.web;
    requires spring.core;
    requires com.blazebit.common.utils;
    requires com.fasterxml.jackson.databind;
    requires spring.beans;
    requires org.slf4j;
    requires jakarta.servlet;
    requires com.fasterxml.jackson.core;
    exports com.blazebit.persistence.spring.data.webmvc;
    exports com.blazebit.persistence.spring.data.webmvc.impl;
}