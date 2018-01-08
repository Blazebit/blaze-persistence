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

package com.blazebit.persistence.view.spi.type;

/**
 * Every entity view object implements this interface to give access to known attributes and metamodel information.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface EntityViewProxy {

    /**
     * Returns the JPA managed type for which this entity view object is a projection.
     *
     * @return The JPA managed type
     */
    public Class<?> $$_getJpaManagedClass();

    /**
     * Returns the entity view type of this object.
     *
     * @return The entity view type
     */
    public Class<?> $$_getEntityViewClass();

    /**
     * Whether the instance was created via {@link com.blazebit.persistence.view.EntityViewManager#create(Class)} and will cause an entity to be persisted during an <em>update</em>.
     *
     * @return True if will cause persist, otherwise false
     */
    public boolean $$_isNew();

    /**
     * Returns the identifier object of this entity view if it has one, otherwise <code>null</code>.
     *
     * @return The identifier or <code>null</code>
     */
    public Object $$_getId();

    /**
     * Returns the version object of this entity view if it has one, otherwise <code>null</code>.
     *
     * @return The version or <code>null</code>
     */
    public Object $$_getVersion();
}
