/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.jaxrs.jackson {
    requires transitive com.blazebit.persistence.view;
    requires com.blazebit.common.utils;
    requires com.blazebit.persistence.integration.jackson;
    requires com.blazebit.persistence.integration.jaxrs;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    exports com.blazebit.persistence.integration.jaxrs.jackson;
}