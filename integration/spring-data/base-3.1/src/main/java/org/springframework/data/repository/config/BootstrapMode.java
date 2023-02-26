/*
 * Copyright 2018-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.repository.config;

/**
 * Enumeration to define in which way repositories are bootstrapped.
 *
 * @author Oliver Gierke
 * @see RepositoryConfigurationSource#getBootstrapMode()
 * @since 2.1
 * @soundtrack Dave Matthews Band - She (Come Tomorrow)
 */
public enum BootstrapMode {

	/**
	 * Repository proxies are instantiated eagerly, just like any other Spring bean, except explicitly marked as lazy.
	 * Thus, injection into repository clients will trigger initialization.
	 */
	DEFAULT,

	/**
	 * Repository bean definitions are considered lazy and clients will get repository proxies injected that will
	 * initialize on first access. Repository initialization is triggered on application context bootstrap completion.
	 */
	DEFERRED,

	/**
	 * Repository bean definitions are considered lazy, lazily inject and only initialized on first use, i.e. the
	 * application might have fully started without the repositories initialized.
	 */
	LAZY;
}
