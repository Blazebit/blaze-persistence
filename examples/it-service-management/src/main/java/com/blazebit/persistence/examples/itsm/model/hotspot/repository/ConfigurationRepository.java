/*
 * Copyright 2014 - 2020 Blazebit.
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
