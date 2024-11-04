/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.view.cdi {
    requires java.sql;
    requires jakarta.persistence;
    requires jakarta.cdi;
    requires com.blazebit.persistence.view;
    exports com.blazebit.persistence.integration.view.cdi;
    provides jakarta.enterprise.inject.spi.Extension with com.blazebit.persistence.integration.view.cdi.EntityViewExtension;
}