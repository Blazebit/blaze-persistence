/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.jackson {
    requires transitive com.blazebit.persistence.view;
    requires com.blazebit.common.utils;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    exports com.blazebit.persistence.integration.jackson;
}