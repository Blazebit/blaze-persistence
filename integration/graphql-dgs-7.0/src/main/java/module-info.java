/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.graphql.dgs {
    requires com.blazebit.persistence.integration.graphql;
    requires graphql.dgs;
    requires com.graphqljava;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires com.blazebit.persistence.view.impl;
    requires com.blazebit.persistence.view;
    requires kotlin.reflect;
    requires kotlin.stdlib.common;
    requires static annotations;
    exports com.blazebit.persistence.integration.graphql.dgs;
    opens com.blazebit.persistence.integration.graphql.dgs to spring.core;
}