/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.hibernate {
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.blazebit.persistence.core;
    requires transitive com.blazebit.persistence.integration.hibernate.base;
    // Can't make this static even though this would be very desirable, due to https://bugs.openjdk.org/browse/JDK-8299504
    // The only "dependency" on this module is to provide a service, but JPMS can't model this correctly
    requires com.blazebit.persistence.view;
    requires jakarta.transaction;
    provides com.blazebit.persistence.integration.hibernate.base.HibernateAccess with com.blazebit.persistence.integration.hibernate.Hibernate62Access;
    provides com.blazebit.persistence.spi.EntityManagerFactoryIntegrator with com.blazebit.persistence.integration.hibernate.Hibernate62EntityManagerFactoryIntegrator;
    provides com.blazebit.persistence.view.spi.TransactionAccessFactory with com.blazebit.persistence.integration.hibernate.Hibernate6TransactionAccessFactory;
    provides org.hibernate.integrator.spi.Integrator with com.blazebit.persistence.integration.hibernate.Hibernate62Integrator;
    provides org.hibernate.boot.spi.AdditionalMappingContributor with com.blazebit.persistence.integration.hibernate.Hibernate62AdditionalMappingContributor;
    provides org.hibernate.service.spi.ServiceContributor with com.blazebit.persistence.integration.hibernate.Hibernate62ServiceContributor;
    exports com.blazebit.persistence.integration.hibernate;
}