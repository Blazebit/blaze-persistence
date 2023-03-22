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
    @ConfigItem(defaultValue = "false")
    public boolean templateEagerLoading;

    /**
     * A boolean flag to make it possible to disable the managed type validation.
     * By default the managed type validation is enabled, but since the validation is not bullet proof, it can be disabled.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(defaultValue = "false")
    public boolean managedTypeValidationDisabled;

    /**
     * An integer value that defines the default batch size for entity view attributes.
     * By default the value is 1 and can be overridden either via {@linkplain com.blazebit.persistence.view.BatchFetch#size()}
     * or by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     */
    @ConfigItem(defaultValue = "1")
    public int defaultBatchSize;

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
    @ConfigItem(defaultValue = "values")
    public String expectBatchMode;

    /**
     * A boolean flag to make it possible to prepare the entity view updater cache on startup.
     * By default the eager loading of entity view updates is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.eager-loading", defaultValue = "false")
    public boolean updaterEagerLoading;

    /**
     * A boolean flag to make it possible to disable the strict validation that disallows the use of an updatable entity view type for owned relationships.
     * By default the use is disallowed i.e. the default value is <code>true</code>, but since there might be strange models out there, it possible to allow this.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.disallow-owned-updatable-subview", defaultValue = "true")
    public boolean updaterDisallowOwnedUpdatableSubview;

    /**
     * A boolean flag to make it possible to disable the strict cascading check that disallows setting updatable or creatable entity views on non-cascading attributes
     * before being associated with a cascading attribute. When disabled, it is possible, like in JPA, that the changes done to an updatable entity view are not flushed
     * when it is not associated with an attribute that cascades updates.
     * By default the use is enabled i.e. the default value is <code>true</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.strict-cascading-check", defaultValue = "true")
    public boolean updaterStrictCascadingCheck;

    /**
     * A boolean flag that allows to switch from warnings to boot time validation errors when invalid plural attribute setters are encountered while the strict cascading check is enabled.
     * When <code>true</code>, a boot time validation error is thrown when encountering an invalid setter, otherwise just a warning.
     * This configuration has no effect when the strict cascading check is disabled.
     * By default the use is disabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(name = "updater.error-on-invalid-plural-setter", defaultValue = "false")
    public boolean updaterErrorOnInvalidPluralSetter;

    /**
     * A boolean flag that allows to specify if empty flat views should be created by default if not specified via {@link EmptyFlatViewCreation}.
     * By default the creation of empty flat views is enabled i.e. the default value is <code>true</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     */
    @ConfigItem(defaultValue = "true")
    public boolean createEmptyFlatViews;

    /**
     * The fully qualified expression cache implementation class name.
     */
    @ConfigItem(defaultValue = "com.blazebit.persistence.parser.expression.ConcurrentHashMapExpressionCache")
    public String expressionCacheClass;

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
    @ConfigItem(defaultValue = "true")
    public boolean inlineCtes;

    /**
     * If set to true, the query plans are cached and reused.
     * Valid values for this property are <code>true</code> and <code>false</code>.
     * Default is <code>true</code>.
     * This configuration option currently only takes effect when Hibernate is used as JPA provider.
     *
     * The property can be changed for a criteria builder before constructing a query.
     */
    @ConfigItem(defaultValue = "true")
    public boolean queryPlanCacheEnabled;

    public void apply(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        criteriaBuilderConfiguration.setProperty(ConfigurationProperties.EXPRESSION_CACHE_CLASS, expressionCacheClass);
        criteriaBuilderConfiguration.setProperty(ConfigurationProperties.INLINE_CTES, Boolean.toString(inlineCtes));
        criteriaBuilderConfiguration.setProperty(ConfigurationProperties.QUERY_PLAN_CACHE_ENABLED, Boolean.toString(queryPlanCacheEnabled));
    }

    public void apply(EntityViewConfiguration entityViewConfiguration) {
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.TEMPLATE_EAGER_LOADING, Boolean.toString(templateEagerLoading));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.MANAGED_TYPE_VALIDATION_DISABLED, Boolean.toString(managedTypeValidationDisabled));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.DEFAULT_BATCH_SIZE, Integer.toString(defaultBatchSize));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.EXPECT_BATCH_MODE, expectBatchMode);
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_EAGER_LOADING, Boolean.toString(updaterEagerLoading));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, Boolean.toString(updaterDisallowOwnedUpdatableSubview));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK, Boolean.toString(updaterStrictCascadingCheck));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.UPDATER_ERROR_ON_INVALID_PLURAL_SETTER, Boolean.toString(updaterErrorOnInvalidPluralSetter));
        entityViewConfiguration.setProperty(com.blazebit.persistence.view.ConfigurationProperties.CREATE_EMPTY_FLAT_VIEWS, Boolean.toString(createEmptyFlatViews));
    }

    public boolean isAnyPropertySet() {
        return !createEmptyFlatViews ||
                defaultBatchSize != 1 ||
                !"values".equals(expectBatchMode) ||
                !"com.blazebit.persistence.parser.expression.ConcurrentHashMapExpressionCache".equals(expressionCacheClass) ||
                !queryPlanCacheEnabled ||
                !inlineCtes ||
                persistenceUnit.isPresent() ||
                packages.isPresent() ||
                templateEagerLoading ||
                managedTypeValidationDisabled ||
                !updaterDisallowOwnedUpdatableSubview ||
                updaterEagerLoading ||
                updaterErrorOnInvalidPluralSetter ||
                !updaterStrictCascadingCheck;
    }
}
