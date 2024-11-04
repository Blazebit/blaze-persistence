/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.quarkus.runtime;

import io.quarkus.hibernate.orm.runtime.integration.HibernateOrmIntegrationStaticInitListener;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;

import java.util.function.BiConsumer;

/**
 * @author Christian Beikov
 * @since 1.6.5
 */
public class BlazePersistenceHibernateOrmIntegrationStaticInitListener implements HibernateOrmIntegrationStaticInitListener {
    @Override
    public void contributeBootProperties(BiConsumer<String, Object> propertyCollector) {
    }

    @Override
    public void onMetadataInitialized(Metadata metadata, BootstrapContext bootstrapContext, BiConsumer<String, Object> propertyCollector) {
        // Carry over the configured bulk-id strategy from Hibernate56MetadataContributor
        propertyCollector.accept(AvailableSettings.HQL_BULK_ID_STRATEGY, bootstrapContext.getServiceRegistry().getService(ConfigurationService.class).getSettings().get(AvailableSettings.HQL_BULK_ID_STRATEGY));
    }
}
