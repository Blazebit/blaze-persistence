/*
 * Copyright 2014 - 2024 Blazebit.
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

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ConfigRoot
public class BlazePersistenceConfiguration {

    /**
     * Configuration for the default Blaze-Persistence instance.
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public BlazePersistenceInstanceConfiguration defaultBlazePersistence;

    /**
     * Additional named Blaze-Persistence instances.
     */
    @ConfigDocSection
    @ConfigDocMapKey("blaze-persistence-instance-name")
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, BlazePersistenceInstanceConfiguration> blazePersistenceInstances;
}
