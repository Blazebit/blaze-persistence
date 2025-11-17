/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.jaxrs.jsonb {
    requires transitive com.blazebit.persistence.view;
    requires transitive com.blazebit.persistence.integration.jaxrs;
    requires com.blazebit.common.utils;
    requires com.blazebit.persistence.integration.jsonb;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires jakarta.json.bind;
    requires jakarta.json;
    exports com.blazebit.persistence.integration.jaxrs.jsonb;
}