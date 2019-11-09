/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view;

/**
 * Configuration properties that can be specified via {@link com.blazebit.persistence.view.spi.EntityViewConfiguration#setProperty(String, String)}.
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public final class ConfigurationProperties {
    
    /**
     * A boolean flag to make it possible to use the generated proxies with serialization.
     * When deserializing an instance the class might not have been loaded yet, so we can force loading
     * proxy classes on startup to avoid this problem. 
     * By default the eager loading of proxies is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * 
     * @since 1.0.6
     */
    public static final String PROXY_EAGER_LOADING = "com.blazebit.persistence.view.proxy.eager_loading";
    /**
     * A boolean flag to make it possible to prepare all view template caches on startup.
     * By default the eager loading of the view templates is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.2.0
     */
    public static final String TEMPLATE_EAGER_LOADING = "com.blazebit.persistence.view.eager_loading";
    /**
     * A boolean flag to make it possible to disable unsafe proxy generation.
     * By default the unsafe proxies are allowed to be able to make use of the features.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * 
     * @since 1.0.6
     */
    public static final String PROXY_UNSAFE_ALLOWED = "com.blazebit.persistence.view.proxy.unsafe_allowed";
    /**
     * A boolean flag to make it possible to disable the expression validation.
     * By default the expression validation is enabled, but since the validation is not bullet proof, it can be disabled.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * 
     * @since 1.1.0
     */
    public static final String EXPRESSION_VALIDATION_DISABLED = "com.blazebit.persistence.view.expression_validation_disabled";
    /**
     * A boolean flag to make it possible to disable the managed type validation.
     * By default the managed type validation is enabled, but since the validation is not bullet proof, it can be disabled.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.3.0
     */
    public static final String MANAGED_TYPE_VALIDATION_DISABLED = "com.blazebit.persistence.view.managed_type_validation_disabled";
    /**
     * An integer value that defines the default batch size for entity view attributes.
     * By default the value is 1 and can be overridden either via {@linkplain com.blazebit.persistence.view.BatchFetch#size()}
     * or by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     *
     * To specify the batch size of a specific attribute, append the attribute name after the "batch_size" like
     * e.g. <code>com.blazebit.persistence.view.batch_size.subProperty</code>
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_BATCH_SIZE = "com.blazebit.persistence.view.batch_size";
    /**
     * A boolean specifying if correlation value batching is expected or view root batching.
     * By default the value is true and can be overridden by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     *
     * To specify the batch expectation of a specific attribute, append the attribute name after the "batch_correlation_values" like
     * e.g. <code>com.blazebit.persistence.view.batch_correlation_values.subProperty</code>
     *
     * @since 1.2.0
     */
    public static final String EXPECT_BATCH_CORRELATION_VALUES = "com.blazebit.persistence.view.batch_correlation_values";
    /**
     * A mode specifying if correlation value, view root or embedded view batching is expected.
     * By default the value is <code>values</code> and can be overridden by setting this property via {@linkplain com.blazebit.persistence.view.EntityViewSetting#setProperty}.
     * Valid values are
     * <ul>
     *  <li><code>values</code></li>
     *  <li><code>view_roots</code></li>
     *  <li><code>embedding_views</code></li>
     * </ul>
     *
     * To specify the batch expectation of a specific attribute, append the attribute name after the "batch_mode" like
     * e.g. <code>com.blazebit.persistence.view.batch_mode.subProperty</code>
     *
     * @since 1.3.0
     */
    public static final String EXPECT_BATCH_MODE = "com.blazebit.persistence.view.batch_mode";
    /**
     * A boolean flag to make it possible to prepare the entity view updater cache on startup.
     * By default the eager loading of entity view updates is disabled to have a better startup performance.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.2.0
     */
    public static final String UPDATER_EAGER_LOADING = "com.blazebit.persistence.view.updater.eager_loading";
    /**
     * An override for the flush mode of updatable entity views.
     * By default, the property is not set. This has the effect, that the flush modes configured for the respective updatable entity views are used.
     * Valid values for this property are <code>partial</code>, <code>lazy</code> or <code>full</code>.
     *
     * To specify an override for a specific entity view, append the fully qualified entity view class name after the "flush_mode" like
     * e.g. <code>com.blazebit.persistence.view.updater.flush_mode.com.mypackage.views.MyView</code>
     *
     * @since 1.2.0
     * @see com.blazebit.persistence.view.FlushMode
     */
    public static final String UPDATER_FLUSH_MODE = "com.blazebit.persistence.view.updater.flush_mode";
    /**
     * An override for the flush strategy of updatable entity views.
     * By default, the property is not set. This has the effect, that the flush strategies configured for the respective updatable entity views are used.
     * Valid values for this property are <code>auto</code>, <code>entity</code> or <code>query</code>.
     *
     * To specify an override for a specific entity view, append the fully qualified entity view class name after the "flush_strategy" like
     * e.g. <code>com.blazebit.persistence.view.updater.flush_strategy.com.mypackage.views.MyView</code>
     *
     * @since 1.2.0
     * @see com.blazebit.persistence.view.FlushStrategy
     */
    public static final String UPDATER_FLUSH_STRATEGY = "com.blazebit.persistence.view.updater.flush_strategy";

    /**
     * A boolean flag to make it possible to disable the strict validation that disallows the use of an updatable entity view type for owned relationships.
     * By default the use is disallowed i.e. the default value is <code>true</code>, but since there might be strange models out there, it possible to allow this.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.3.0
     */
    public static final String UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW = "com.blazebit.persistence.view.updater.disallow_owned_updatable_subview";

    /**
     * A boolean flag to make it possible to disable the strict cascading check that disallows setting updatable or creatable entity views on non-cascading attributes
     * before being associated with a cascading attribute. When disabled, it is possible, like in JPA, that the changes done to an updatable entity view are not flushed
     * when it is not associated with an attribute that cascades updates.
     * By default the use is enabled i.e. the default value is <code>true</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.4.0
     */
    public static final String UPDATER_STRICT_CASCADING_CHECK = "com.blazebit.persistence.view.updater.strict_cascading_check";

    /**
     * A boolean flag that allows to switch from warnings to boot time validation errors when invalid plural attribute setters are encountered while the strict cascading check is enabled.
     * When <code>true</code>, a boot time validation error is thrown when encountering an invalid setter, otherwise just a warning.
     * This configuration has no effect when the strict cascading check is disabled.
     * By default the use is disabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.4.0
     */
    public static final String UPDATER_ERROR_ON_INVALID_PLURAL_SETTER = "com.blazebit.persistence.view.updater.error_on_invalid_plural_setter";

    /**
     * A boolean flag that allows to disable a count query for a paginated criteria builder.
     *
     * By default the count query is enabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.4.0
     */
    public static final String PAGINATION_DISABLE_COUNT_QUERY = "com.blazebit.persistence.view.pagination.disable_count_query";

    /**
     * A boolean flag that allows to enable the extraction of all keysets for a paginated criteria builder.
     *
     * By default the extraction of all keysets is disabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.4.0
     */
    public static final String PAGINATION_EXTRACT_ALL_KEYSETS = "com.blazebit.persistence.view.pagination.extract_all_keysets";

    /**
     * A boolean flag that allows to force the use of the keyset for a paginated criteria builder rather than relying on firstResult/maxResults.
     * This is useful if a strict keyset based pagination is necessary and the page size or the offset might vary.
     *
     * By default forcing keysets is disabled i.e. the default value is <code>false</code>.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     *
     * @since 1.4.0
     */
    public static final String PAGINATION_FORCE_USE_KEYSET = "com.blazebit.persistence.view.pagination.force_use_keyset";

    private ConfigurationProperties() {
    }
}
