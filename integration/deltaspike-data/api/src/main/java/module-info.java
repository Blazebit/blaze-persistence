/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.deltaspike.data {
    requires java.sql;
    requires jakarta.persistence;
    requires jakarta.cdi;
    requires com.blazebit.persistence.view;
    requires deltaspike.data.module.api;
    requires deltaspike.core.api;
    exports com.blazebit.persistence.deltaspike.data;
}