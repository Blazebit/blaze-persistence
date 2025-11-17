/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.hibernate.base {
    requires org.hibernate.orm.core;
    requires java.naming;
	requires jakarta.persistence;
    requires com.blazebit.common.utils;
    requires com.blazebit.persistence.integration.jpa;
    requires com.blazebit.persistence.core;
    exports com.blazebit.persistence.integration.hibernate.base;
    exports com.blazebit.persistence.integration.hibernate.base.spi;
    exports com.blazebit.persistence.integration.hibernate.base.function;
    uses com.blazebit.persistence.integration.hibernate.base.HibernateAccess;
    uses com.blazebit.persistence.integration.hibernate.base.spi.HibernateVersionProvider;
    provides com.blazebit.persistence.spi.ExtendedQuerySupport with com.blazebit.persistence.integration.hibernate.base.HibernateExtendedQuerySupport;
}