/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies whether an entity should be cached, if caching is enabled,
 * and when the value of the {@code persistence.xml} caching element is
 * {@link SharedCacheMode#ENABLE_SELECTIVE} or
 * {@link SharedCacheMode#DISABLE_SELECTIVE}.
 *
 * <p>The value of the {@code Cacheable} annotation is inherited by
 * subclasses; it can be overridden by specifying {@code Cacheable} on
 * a subclass.
 * 
 * <p>{@code Cacheable(false)} means that the entity and its state must
 * not be cached by the provider.
 * 
 * @since 2.0
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Cacheable {

    /**
     * (Optional) Whether or not the entity should be cached.
     */
    boolean value() default true;
}
