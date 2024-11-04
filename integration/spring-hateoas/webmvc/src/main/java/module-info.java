/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.spring.hateoas.webmvc {
    requires com.blazebit.persistence.integration.spring.data.webmvc;
    requires spring.boot.autoconfigure;
    requires com.blazebit.persistence.view;
    requires com.fasterxml.jackson.databind;
    requires spring.beans;
    requires spring.core;
    requires spring.data.commons;
    requires spring.hateoas;
    requires spring.web;
    requires org.slf4j;
    exports com.blazebit.persistence.spring.hateoas.webmvc;
}