/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.deltaspike.data.rest.impl {
    requires transitive com.blazebit.persistence.integration.deltaspike.data.rest;
    requires com.blazebit.common.utils;
    requires jakarta.cdi;
    requires com.blazebit.persistence.core;
    requires com.fasterxml.jackson.databind;
    exports com.blazebit.persistence.deltaspike.data.rest.impl;
}