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

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A mapping for an entity view type.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewMapping {

    /**
     * The type represented by this entity view mapping.
     *
     * @return The entity view type
     */
    public Class<?> getEntityViewClass();

    /**
     * The JPA managed type class for which this entity view mapping is defined.
     *
     * @return The JPA managed type class
     */
    public Class<?> getEntityClass();

    /**
     * Set the JPA managed type class for this entity view mapping.
     *
     * @param entityClass The JPA managed type class
     */
    public void setEntityClass(Class<?> entityClass);

    /**
     * Returns the name of the entity view, typically the simple type name.
     *
     * @return The entity view name
     */
    public String getName();

    /**
     * Set the entity view name.
     *
     * @param name The name
     */
    public void setName(String name);

    /**
     * Returns whether the entity view should be updatable i.e. support updates via {@link com.blazebit.persistence.view.EntityViewManager#update(EntityManager, Object)}.
     *
     * @return Whether the entity view should be updatable
     */
    public boolean isUpdatable();

    /**
     * Set whether the entity view should be updatable.
     *
     * @param updatable Whether the entity view should be updatable
     */
    public void setUpdatable(boolean updatable);

    /**
     * Returns the lock mode to use for doing updates or <code>null</code> if the entity view should not be updatable.
     *
     * @return The lock mode for updates
     */
    public LockMode getLockMode();

    /**
     * Set the lock mode to use for updates.
     *
     * @param lockMode The lock mode
     */
    public void setLockMode(LockMode lockMode);

    /**
     * Returns the lock owner mapping relative to the JPA managed type of this entity view to use for updates or <code>null</code> if the entity view should not be updatable.
     *
     * @return The lock owner for updates
     */
    public String getLockOwner();

    /**
     * Set the lock owner mapping relative to the JPA managed type of this entity view.
     *
     * @param lockOwner The lock owner mapping
     */
    public void setLockOwner(String lockOwner);

    /**
     * Returns the flush mode to use for updates or <code>null</code> if the entity view should not be updatable.
     *
     * @return The flush mode for updates
     */
    public FlushMode getFlushMode();

    /**
     * Set the flush mode to use for updates.
     *
     * @param flushMode The flush mode
     */
    public void setFlushMode(FlushMode flushMode);

    /**
     * Returns the flush strategy to use for updates or <code>null</code> if the entity view should not be updatable.
     *
     * @return The flush strategy for updates
     */
    public FlushStrategy getFlushStrategy();

    /**
     * Set the flush strategy to use for updates.
     *
     * @param flushStrategy The flush strategy
     */
    public void setFlushStrategy(FlushStrategy flushStrategy);

    /**
     * Returns the post create method or <code>null</code> if there is none.
     *
     * @return The post create method
     */
    public Method getPostCreateMethod();

    /**
     * Sets the post create method.
     *
     * @param postCreateMethod The method
     */
    public void setPostCreateMethod(Method postCreateMethod);

    /**
     * Returns whether the entity view should be creatable i.e. support persist via {@link com.blazebit.persistence.view.EntityViewManager#update(EntityManager, Object)}.
     *
     * @return Whether the entity view should be creatable
     */
    public boolean isCreatable();

    /**
     * Set whether the entity view should be creatable.
     *
     * @param creatable Whether the entity view should be creatable
     */
    public void setCreatable(boolean creatable);

    /**
     * Returns whether the persistability of an entity view should be validated i.e. check if an entity could be successfully persisted based on the settable attributes.
     *
     * @return Whether the persistability of an entity view should be validated
     */
    public boolean isValidatePersistability();

    /**
     * Set whether the entity view should be validated regarding it's persistability.
     *
     * @param validatePersistability Whether the entity view should be validated regarding it's persistability
     */
    public void setValidatePersistability(boolean validatePersistability);

    /**
     * Returns the id attribute mapping of this entity view mapping or <code>null</code> if there is none.
     *
     * @return The id attribute mapping or <code>null</code> if there is none
     */
    public EntityViewAttributeMapping getIdAttribute();

    /**
     * Set the id attribute mapping of this entity view mapping.
     * Note that the attribute must be one of the attributes as given by {@link #getAttributes()} or <code>null</code>.
     *
     * @param idAttribute The id attribute mapping
     */
    public void setIdAttribute(EntityViewAttributeMapping idAttribute);

    /**
     * Returns the version attribute mapping of this entity view mapping or <code>null</code> if ther is none.
     *
     * @return The version attribute mapping or <code>null</code> if there is none
     */
    public EntityViewAttributeMapping getVersionAttribute();

    /**
     * Set the version attribute mapping of this entity view mapping.
     * Note that the attribute must be one of the attributes as given by {@link #getAttributes()} or <code>null</code>.
     *
     * @param versionAttribute The version attribute mapping
     */
    public void setVersionAttribute(EntityViewAttributeMapping versionAttribute);

    /**
     * Returns the attribute mappings defined for this entity view mapping.
     *
     * @return The defined attribute mappings
     */
    public Map<String, EntityViewAttributeMapping> getAttributes();

    /**
     * Returns the constructor mappings defined for this entity view mapping.
     *
     * @return The defined constructor mappings
     */
    public Map<String, EntityViewConstructorMapping> getConstructors();
}
