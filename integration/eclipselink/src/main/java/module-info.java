/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.eclipselink {
    requires eclipselink;
    requires com.blazebit.common.utils;
    requires com.blazebit.persistence.integration.jpa;
    requires com.blazebit.persistence.core;
    provides com.blazebit.persistence.spi.EntityManagerFactoryIntegrator with com.blazebit.persistence.integration.eclipselink.function.EclipseLinkEntityManagerIntegrator;
}