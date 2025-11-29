/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.criteria {
    requires transitive com.blazebit.persistence.core;
    requires jakarta.persistence;
    exports com.blazebit.persistence.criteria;
    exports com.blazebit.persistence.criteria.spi;
}