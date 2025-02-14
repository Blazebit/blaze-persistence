/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.runtime;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
@ConfigMapping(prefix = "quarkus.blaze-persistence")
public interface BlazePersistenceConfiguration {

    /**
     * Additional named Blaze-Persistence instances.
     */
    @ConfigDocSection
    @ConfigDocMapKey("blaze-persistence-instance-name")
    @WithParentName
    @WithUnnamedKey(BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME)
    Map<String, BlazePersistenceInstanceConfiguration> blazePersistenceInstances();

    default BlazePersistenceInstanceConfiguration defaultBlazePersistenceInstance() {
        return blazePersistenceInstances().get(BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME);
    }

    default Map<String, BlazePersistenceInstanceConfiguration> namedBlazePersistenceInstances() {
        Map<String, BlazePersistenceInstanceConfiguration> map = new TreeMap<>(blazePersistenceInstances());
        map.remove(BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME);
        return map;
    }
}
