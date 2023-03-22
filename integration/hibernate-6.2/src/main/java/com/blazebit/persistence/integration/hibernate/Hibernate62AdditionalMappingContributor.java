/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(AdditionalMappingContributor.class)
public class Hibernate62AdditionalMappingContributor implements AdditionalMappingContributor {
    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        // Skip if already registered
        if (metadata.getEntityBinding("com.blazebit.persistence.impl.function.entity.ValuesEntity") != null) {
            return;
        }
        contributions.contributeEntity(buildingContext.getBootstrapContext().getClassLoaderAccess().classForName("com.blazebit.persistence.impl.function.entity.ValuesEntity"));
    }

    @Override
    public String getContributorName() {
        return "blaze-persistence";
    }
}
