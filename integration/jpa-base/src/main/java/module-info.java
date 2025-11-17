/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.jpa {
    requires java.sql;
    requires jakarta.persistence;
    requires com.blazebit.persistence.core;
    requires com.blazebit.persistence.core.parser;
    exports com.blazebit.persistence.integration.jpa;
    exports com.blazebit.persistence.integration.jpa.function;
}