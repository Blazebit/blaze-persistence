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
package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EmptyFlatViewCreation;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

import java.util.Optional;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@ConfigGroup
public class BlazePersistenceInstanceConfiguration {

    /**
     * The name of the persistence unit which this instance of Blaze-Persistence uses.
     * <p>
     * If undefined, it will use the default persistence unit.
     */
    public Optional<String> persistenceUnit;

    /**
     * The packages in which the entity views and entity view listeners assigned to this Blaze-Persistence instance are located.
     */
    public Optional<Set<String>> packages;

    /**
     * A boolean flag to make it possible to prepare all view template caches on startup.
     * By default the eager loading of the view templates is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem
    public Optional<Boolean> templateEagerLoading;

    /**
     * An integer value that defines the default batch size for entity view attributes.
     * By default the value is 1 and can be overridden either via {@linkplain com.blazebit.persistence.view.BatchFetch#size()}
     * or by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     */
    @ConfigItem
    public Optional<Integer> defaultBatchSize;

    /**
     * A mode specifying if correlation value, view root or embedded view batching is expected.
     * By default the value is <code>values</code> and can be overridden by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     * Valid values are
     * <ul>
     *  <li><code>values</code></li>
     *  <li><code>view_roots</code></li>
     *  <li><code>embedding_views</code></li>
     * </ul>
     */
    @ConfigItem
    public Optional<String> expectBatchMode;

    /**
     * A boolean flag to make it possible to prepare the entity view updater cache on startup.
     * By default the eager loading of entity view updates is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.eager-loading")
    public Optional<Boolean> updaterEagerLoading;

    /**
     * A boolean flag to make it possible to disable the strict validation that disallows the use of an updatable entity view type for owned relationships.
     * By default the use is disallowed i.e. the default value is <code>true</code>, but since there might be strange models out there, it possible to allow this.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.disallow-owned-updatable-subview")
    public Optional<Boolean> updaterDisallowOwnedUpdatableSubview;

    /**
     * A boolean flag to make it possible to disable the strict cascading check that disallows setting updatable or creatable entity views on non-cascading attributes
     * before being associated with a cascading attribute. When disabled, it is possible, like in JPA, that the changes done to an updatable entity view are not flushed
     * when it is not associated with an attribute that cascades updates.
     * By default the use is enabled i.e. the default value is <code>true</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.strict-cascading-check")
    public Optional<Boolean> updaterStrictCascadingCheck;

    /**
     * A boolean flag that allows to switch from warnings to boot time validation errors when invalid plural attribute setters are encountered while the strict cascading check is enabled.
     * When <code>true</code>, a boot time validation error is thrown when encountering an invalid setter, otherwise just a warning.
     * This configuration has no effect when the strict cascading check is disabled.
     * By default the use is disabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.error-on-invalid-plural-setter")
    public Optional<Boolean> updaterErrorOnInvalidPluralSetter;

    /**
     * A boolean flag that allows to specify if empty flat views should be created by default if not specified via {@link EmptyFlatViewCreation}.
     * By default the creation of empty flat views is enabled i.e. the default value is <code>true</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem
    public Optional<Boolean> createEmptyFlatViews;

    /**
     * The full qualified expression cache implementation class name.
     */
    @ConfigItem
    public Optional<String> expressionCacheClass;

    /**
     * If set to true, the CTE queries are inlined by default.
     * Valid values for this property are <code>true</code>, <code>false</code> or <code>auto</code>.
     * Default is <code>true</code> which will always inline non-recursive CTEs.
     * The <code>auto</code> configuration will only make use of inlining if the JPA provider and DBMS dialect support/require it.
     *
     * The property can be changed for a criteria builder before constructing a query.
     *
     * @see CTEBuilder#with(Class, boolean)
     * @see CTEBuilder#with(Class, CriteriaBuilder, boolean)
     */
    @ConfigItem
    public Optional<Boolean> inlineCtes;

    public void apply(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        expressionCacheClass.ifPresent(value ->
                criteriaBuilderConfiguration.setProperty(ConfigurationProperties.EXPRESSION_CACHE_CLASS, value)
        );
        inlineCtes.ifPresent(value ->
                criteriaBuilderConfiguration.setProperty(ConfigurationProperties.INLINE_CTES, value.toString())
        );
    }

    public void apply(EntityViewConfiguration entityViewConfiguration) {
        templateEagerLoading.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.TEMPLATE_EAGER_LOADING, value.toString())
        );
        defaultBatchSize.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.DEFAULT_BATCH_SIZE, value.toString())
        );
        expectBatchMode.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.EXPECT_BATCH_MODE, value)
        );
        updaterEagerLoading.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_EAGER_LOADING, value.toString())
        );
        updaterDisallowOwnedUpdatableSubview.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, value.toString())
        );
        updaterStrictCascadingCheck.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK, value.toString())
        );
        updaterErrorOnInvalidPluralSetter.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_ERROR_ON_INVALID_PLURAL_SETTER, value.toString())
        );
        createEmptyFlatViews.ifPresent(value ->
                entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.CREATE_EMPTY_FLAT_VIEWS, value.toString())
        );
    }

    public boolean isAnyPropertySet() {
        return createEmptyFlatViews.isPresent() ||
                defaultBatchSize.isPresent() ||
                expectBatchMode.isPresent() ||
                expressionCacheClass.isPresent() ||
                inlineCtes.isPresent() ||
                persistenceUnit.isPresent() ||
                packages.isPresent() ||
                templateEagerLoading.isPresent() ||
                updaterDisallowOwnedUpdatableSubview.isPresent() ||
                updaterEagerLoading.isPresent() ||
                updaterErrorOnInvalidPluralSetter.isPresent() ||
                updaterStrictCascadingCheck.isPresent();
    }
}
