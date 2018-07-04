/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl;

/**
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

    private ConfigurationProperties() {
    }
}
