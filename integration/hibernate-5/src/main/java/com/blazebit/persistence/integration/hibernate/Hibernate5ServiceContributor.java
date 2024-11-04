/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.hibernate.base.Database;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.spi.ServiceContributor;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(ServiceContributor.class)
public class Hibernate5ServiceContributor implements ServiceContributor {

    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(new StandardServiceInitiator<Database>() {
            @Override
            public Database initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
                return null;
            }

            @Override
            public Class<Database> getServiceInitiated() {
                return Database.class;
            }
        });
    }
}
