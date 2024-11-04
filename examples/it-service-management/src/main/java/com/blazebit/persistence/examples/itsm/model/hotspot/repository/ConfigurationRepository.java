/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.hotspot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;

import com.blazebit.persistence.examples.itsm.model.hotspot.entity.HotspotConfiguration;
import com.blazebit.persistence.examples.itsm.model.hotspot.view.HotspotConfigurationView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface ConfigurationRepository
        extends JpaRepository<HotspotConfiguration, Long>,
        EntityViewSpecificationExecutor<HotspotConfigurationView, HotspotConfiguration> {

}
