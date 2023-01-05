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

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the annotated attribute is allowed to use an updatable entity view type even if it is an owned *ToOne relation.
 *
 * By default, it is disallowed to use an updatable entity view type for owned *ToOne relationships because it is rarely necessary and often results in more problems than it solves.
 * It is vital for a user to understand the implications of allowing updatable entity view types for such relationships.
 *
 * Updatable entity views only allow to model updates of tree-like structures, but not graphs.
 * It is currently a technical limitation because dirty tracking currently only works with a single parent object,
 * but it is also believed that in a domain driven approach, with proper bounded contexts, it is not needed.
 *
 * The technical limitation manifests itself at runtime, when it encounters a graph like updatable object structure.
 * When loading an updatable entity view that contains an owned *ToOne relationship, it is possible for two different root objects to refer to the same sub-object via the *ToOne relationship.
 * If the entity view type used for the *ToOne relationship is updatable, it could end up having multiple parent objects, which currently isn't allowed and results in an exception.
 *
 * There may be reasons to why the entity model was done this way, but at runtime, the objects conform to a tree-like structure.
 * There are a few cases that are safe. If the relationship is reached from the query root via OneToOne relationships,
 * or the owners of the ManyToOne relationships are guaranteed to have distinct objects, allowing updatable entity view types is safe.
 *
 * At some point, the underlying technical limitation will be removed, yet it will probably not be allowed by default to use updatable entity view types for owned *ToOne relationships.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowUpdatableEntityViews {
}
