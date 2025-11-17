/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.core {
    requires transitive java.sql;
    requires transitive jakarta.persistence;
    exports com.blazebit.persistence;
    exports com.blazebit.persistence.spi;
    exports com.blazebit.persistence.internal;
    uses com.blazebit.persistence.spi.CriteriaBuilderConfigurationProvider;
}