/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
