/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
open module com.blazebit.persistence.view.testsuite {
    requires java.sql;
    requires jakarta.persistence;
    requires com.blazebit.common.utils;
    requires com.blazebit.persistence.core;
    requires com.blazebit.persistence.core.parser;
    requires com.blazebit.persistence.view;
    requires com.blazebit.persistence.view.impl;
}