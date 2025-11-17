/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.graphql {
    requires com.graphqljava;
    requires com.blazebit.persistence.core.parser;
    requires com.blazebit.persistence.view.impl;
    requires com.blazebit.persistence.view;
    requires com.blazebit.persistence.core.impl;
    requires com.blazebit.common.utils;
    requires jakarta.persistence;
	requires static kotlin.stdlib;
    exports com.blazebit.persistence.integration.graphql;
}