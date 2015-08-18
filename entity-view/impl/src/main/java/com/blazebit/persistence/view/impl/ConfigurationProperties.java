/*
 * Copyright 2014 Blazebit.
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
	 * We added a flag to make it possible to use the generated proxies with serialization.
	 * When deserializing an instance the class might not have been loaded yet, so we can force loading
	 * proxy classes on startup to avoid this problem. 
	 * By default the eager loading of proxies is disabled to have a better startup performance.
	 * Valid values for this property are <code>true</code> or <code>false</code>.
	 * 
	 * @since 1.0.6
	 */
	public static final String PROXY_EAGER_LOADING = "com.blazebit.persistence.view.proxy.eager_loading";
	/**
	 * We added a flag to make it possible to disable unsafe proxy generation.
	 * By default the unsafe proxies are allowed to be able to make use of the features.
	 * Valid values for this property are <code>true</code> or <code>false</code>.
	 * 
	 * @since 1.0.6
	 */
	public static final String PROXY_UNSAFE_ALLOWED = "com.blazebit.persistence.view.proxy.unsafe_allowed";

	private ConfigurationProperties() {
	}
}
