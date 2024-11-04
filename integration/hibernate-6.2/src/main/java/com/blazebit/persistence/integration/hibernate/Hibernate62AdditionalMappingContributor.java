/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
