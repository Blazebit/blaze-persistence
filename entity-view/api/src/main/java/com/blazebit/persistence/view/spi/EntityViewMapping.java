/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.ViewTransition;

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
     * Returns whether the entity view should be updatable i.e. support updates via {@link com.blazebit.persistence.view.EntityViewManager#save(EntityManager, Object)}.
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
     * Returns the post convert method or <code>null</code> if there is none.
     *
     * @return The post convert method
     * @since 1.4.0
     */
    public Method getPostConvertMethod();

    /**
     * Sets the post convert method.
     *
     * @param postConvertMethod The method
     * @since 1.4.0
     */
    public void setPostConvertMethod(Method postConvertMethod);

    /**
     * Returns the post load method or <code>null</code> if there is none.
     *
     * @return The post load method
     * @since 1.5.0
     */
    public Method getPostLoadMethod();

    /**
     * Sets the post load method.
     *
     * @param postLoadMethod The method
     * @since 1.5.0
     */
    public void setPostLoadMethod(Method postLoadMethod);

    /**
     * Returns the pre persist method or <code>null</code> if there is none.
     *
     * @return The pre persist method
     * @since 1.4.0
     */
    public Method getPrePersistMethod();

    /**
     * Sets the pre persist method.
     *
     * @param prePersistMethod The method
     * @since 1.4.0
     */
    public void setPrePersistMethod(Method prePersistMethod);

    /**
     * Returns the post persist method or <code>null</code> if there is none.
     *
     * @return The post persist method
     * @since 1.4.0
     */
    public Method getPostPersistMethod();

    /**
     * Sets the post persist method.
     *
     * @param postPersistMethod The method
     * @since 1.4.0
     */
    public void setPostPersistMethod(Method postPersistMethod);

    /**
     * Returns the pre update method or <code>null</code> if there is none.
     *
     * @return The pre update method
     * @since 1.4.0
     */
    public Method getPreUpdateMethod();

    /**
     * Sets the pre update method.
     *
     * @param preUpdateMethod The method
     * @since 1.4.0
     */
    public void setPreUpdateMethod(Method preUpdateMethod);

    /**
     * Returns the post update method or <code>null</code> if there is none.
     *
     * @return The post update method
     * @since 1.4.0
     */
    public Method getPostUpdateMethod();

    /**
     * Sets the post update method.
     *
     * @param postUpdateMethod The method
     * @since 1.4.0
     */
    public void setPostUpdateMethod(Method postUpdateMethod);

    /**
     * Returns the pre remove method or <code>null</code> if there is none.
     *
     * @return The pre remove method
     * @since 1.4.0
     */
    public Method getPreRemoveMethod();

    /**
     * Sets the pre remove method.
     *
     * @param preRemoveMethod The method
     * @since 1.4.0
     */
    public void setPreRemoveMethod(Method preRemoveMethod);

    /**
     * Returns the post remove method or <code>null</code> if there is none.
     *
     * @return The post remove method
     * @since 1.4.0
     */
    public Method getPostRemoveMethod();

    /**
     * Sets the post remove method.
     *
     * @param postRemoveMethod The method
     * @since 1.4.0
     */
    public void setPostRemoveMethod(Method postRemoveMethod);

    /**
     * Returns the post rollback method or <code>null</code> if there is none.
     *
     * @return The post rollback method
     * @since 1.4.0
     */
    public Method getPostRollbackMethod();

    /**
     * Sets the post rollback method.
     *
     * @param postRollbackMethod The method
     * @since 1.4.0
     */
    public void setPostRollbackMethod(Method postRollbackMethod);

    /**
     * Returns the post commit method or <code>null</code> if there is none.
     *
     * @return The post commit method
     * @since 1.4.0
     */
    public Method getPostCommitMethod();

    /**
     * Sets the post commit method.
     *
     * @param postCommitMethod The method
     * @since 1.4.0
     */
    public void setPostCommitMethod(Method postCommitMethod);

    /**
     * Returns the post rollback view transitions or <code>null</code> if there is none.
     *
     * @return The post rollback view transitions
     * @since 1.4.0
     */
    public ViewTransition[] getPostRollbackTransitions();

    /**
     * Sets the post rollback view transitions.
     *
     * @param viewTransitions The view transitions
     * @since 1.4.0
     */
    public void setPostRollbackTransitions(ViewTransition[] viewTransitions);

    /**
     * Returns the post commit view transitions or <code>null</code> if there is none.
     *
     * @return The post commit view transitions
     * @since 1.4.0
     */
    public ViewTransition[] getPostCommitTransitions();

    /**
     * Sets the post commit view transitions.
     *
     * @param viewTransitions The view transitions
     * @since 1.4.0
     */
    public void setPostCommitTransitions(ViewTransition[] viewTransitions);

    /**
     * Returns whether the entity view should be creatable i.e. support persist via {@link com.blazebit.persistence.view.EntityViewManager#save(EntityManager, Object)}.
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
