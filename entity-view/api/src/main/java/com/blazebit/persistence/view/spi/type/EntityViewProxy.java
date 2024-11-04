/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
     * Returns the base JPA managed type for which this entity view object is a projection.
     * This is the base entity type if {@link #$$_getJpaManagedClass()} is an inheritance subtype.
     *
     * @return The base JPA managed type
     * @since 1.4.0
     */
    public Class<?> $$_getJpaManagedBaseClass();

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
     * Whether the instance was created via {@link com.blazebit.persistence.view.EntityViewManager#getReference(Class, Object)}.
     *
     * @return True if the instance is a reference, otherwise false
     * @since 1.5.0
     */
    public boolean $$_isReference();

    /**
     * Sets whether the object should be a reference.
     *
     * @param isReference Whether the object should be a reference
     * @since 1.6.0
     */
    public void $$_setIsReference(boolean isReference);

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
