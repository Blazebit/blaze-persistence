package com.blazebit.persistence.impl.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.Service;
import org.hibernate.service.spi.ServiceContributor;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

@ServiceProvider(ServiceContributor.class)
public class Hibernate50ServiceContributor implements ServiceContributor {

    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(new StandardServiceInitiator() {
            @Override
            public Service initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
                return null;
            }

            @Override
            public Class getServiceInitiated() {
                return Database.class;
            }
        });
    }
}
