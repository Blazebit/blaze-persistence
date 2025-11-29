/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.view {
    requires transitive com.blazebit.persistence.core;
    requires jakarta.transaction;
    exports com.blazebit.persistence.view;
    exports com.blazebit.persistence.view.change;
    exports com.blazebit.persistence.view.filter;
    exports com.blazebit.persistence.view.metamodel;
    exports com.blazebit.persistence.view.spi;
    exports com.blazebit.persistence.view.spi.type;
    uses com.blazebit.persistence.view.spi.EntityViewConfigurationProvider;
}