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

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the update handling of values assigned to the annotated attribute.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdatableMapping {

    /**
     * Specifies whether the relation for the attribute is updatable.
     *
     * If the attribute maps a *ToOne relation and {@linkplain #updatable()} is <code>false</code>, the relation isn't updated
     * and the referenced entity unless overridden by {@linkplain #cascade()} isn't updated either. If it is set to <code>true</code>, at least the relation updated.
     * If the attribute maps a *ToMany relation and {@linkplain #updatable()} is <code>false</code>, changes to the collection
     * aren't applied to the collection of the backing entity. If it is set to <code>true</code>, at least the collection changes are applied.
     *
     * Unless overridden by this attribute, every attribute that also has a setter is updatable.
     *
     * @return Whether attribute updates are enabled
     */
    public boolean updatable() default true;

    /**
     * Specifies whether an element should be deleted when it is removed/replaced in the attribute value.
     *
     * If the backing entity of the updatable entity view defines orphan removal, this setting must be set to true, otherwise it is a configuration error.
     * The rationale behind this, is that it should be apparent by looking at the attribute definition that orphan removal happens.
     * If this weren't a configuration error, the #{@link FlushStrategy#ENTITY} flush strategy would behave differently than other strategies,
     * as the JPA provider would do the orphan removal in case of an update whereas other strategies would adhere to the orphan removal defined on this attribute.
     *
     * @return Whether removed/replaced elements on the attribute should be deleted
     */
    public boolean orphanRemoval() default false;

    /**
     * The actions that should cascade for the runtime type of objects assigned to the annotated attribute.
     * Allows to override the default cascading strategy for a method attribute.
     * <p>
     * The default strategy {@link CascadeType#AUTO} causes that attributes that are mutable to cascade all changes.
     * Updatable and non-updatable attributes that have a non-mutable/immutable type do not cascade changes by default.
     * <p>
     * An updatable attribute with a non-mutable type can define cascading so that objects of a subtype are persisted or updated.
     * A mutable attribute can be excluded from persisting or updating by annotating an empty array of cascade types.
     *
     * @return The events that should further cascade
     * @since 1.2.0
     */
    public CascadeType[] cascade() default { CascadeType.AUTO };

    /**
     * Subtypes of the attribute element type that are allowed to be receive {@link CascadeType#PERSIST} and {@link CascadeType#UPDATE} cascade events.
     *
     * @return Allowed subtypes for cascading
     * @since 1.2.0
     */
    public Class<?>[] subtypes() default { };

    /**
     * Subtypes of the attribute element type that are allowed to receive {@link CascadeType#PERSIST} cascade events.
     *
     * @return Allowed subtypes for persist cascading
     * @since 1.2.0
     */
    public Class<?>[] persistSubtypes() default { };

    /**
     * Subtypes of the attribute element type that are allowed to receive {@link CascadeType#UPDATE} cascade events.
     *
     * @return Allowed subtypes for update cascading
     * @since 1.2.0
     */
    public Class<?>[] updateSubtypes() default { };
}
