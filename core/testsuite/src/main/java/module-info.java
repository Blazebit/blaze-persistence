/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
open module com.blazebit.persistence.core.testsuite {
    requires com.blazebit.persistence.testsuite.assertion;
    requires com.blazebit.persistence.testsuite.base;
    requires com.blazebit.persistence.testsuite.base.jpa;
    requires com.blazebit.persistence.core.impl;
    requires com.blazebit.persistence.core.parser;
    requires com.blazebit.persistence.core;
    requires com.blazebit.common.utils;
    requires jakarta.annotation;
    exports com.blazebit.persistence.testsuite;
    exports com.blazebit.persistence.testsuite.entity;
    exports com.blazebit.persistence.testsuite.treat.entity;
    exports com.blazebit.persistence.testsuite.tx;
    uses com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
}