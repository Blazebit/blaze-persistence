/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.graphql.spqr {
    requires com.blazebit.persistence.integration.jackson;
    requires com.blazebit.persistence.integration.graphql;
    requires com.fasterxml.jackson.databind;
    requires spqr;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    exports com.blazebit.persistence.integration.graphql.spqr;
    opens com.blazebit.persistence.integration.graphql.spqr to spring.core;
}