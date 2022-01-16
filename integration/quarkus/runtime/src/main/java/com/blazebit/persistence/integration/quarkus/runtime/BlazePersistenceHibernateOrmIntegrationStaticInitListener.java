/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
