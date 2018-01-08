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
 * A contract for defining a custom basic type to use with entity views.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BasicUserType<X> {

    /**
     * The object to return from {@link #getDirtyProperties(Object)} when unsure what properties are dirty.
     */
    public static final String[] DIRTY_MARKER = new String[0];

    /**
     * Returns <code>true</code> if the type is mutable, <code>false</code> otherwise.
     *
     * @return true if mutable, false otherwise
     */
    public boolean isMutable();

    /**
     * Returns <code>true</code> if the type supports dirty checking, <code>false</code> otherwise.
     * This is only relevant when the type is also mutable.
     *
     * @return true if dirty checking is supported, false otherwise
     */
    public boolean supportsDirtyChecking();

    /**
     * Returns <code>true</code> if the type supports dirty tracking, <code>false</code> otherwise.
     * Support for dirty tracking implies that the type implements the {@link BasicDirtyTracker} interface.
     * This is only relevant when the type is also mutable. Note that if this method returns true, the {@link #supportsDirtyChecking()} method should also return true.
     *
     * @return true if dirty tracking is supported, false otherwise
     */
    public boolean supportsDirtyTracking();

    /**
     * Returns <code>true</code> if the type supports checking deep equality, <code>false</code> otherwise.
     * Deep equality checking, in contrast to normal equality checking, compares objects by their actual values rather than maybe just their identity.
     * For value types, deep equality checking is the same as normal equality checking.
     *
     * @return true if deep equality checking is supported, false otherwise
     */
    public boolean supportsDeepEqualChecking();

    /**
     * Returns <code>true</code> if the type supports creating deep clones, <code>false</code> otherwise.
     * Deep clones are only necessary for mutable types that don't support dirty checking. Immutable types can simply return <code>true</code>.
     *
     * @return true if deep cloning is supported, false otherwise
     */
    public boolean supportsDeepCloning();

    /**
     * Returns <code>true</code> if the given objects are equal regarding their identity.
     * For value types, this is the same as deep equality checking.
     *
     * @param object1 The first object
     * @param object2 The second object
     * @return true if the objects are equal, false otherwise
     */
    public boolean isEqual(X object1, X object2);

    /**
     * Returns <code>true</code> if the given objects are equal regarding their values.
     * If deep equality is not supported, returns false.
     *
     * @param object1 The first object
     * @param object2 The second object
     * @return true if the objects are equal, false otherwise
     */
    public boolean isDeepEqual(X object1, X object2);

    /**
     * Returns the hash code of the object for lookups in hash based collections.
     *
     * @param object The object
     * @return the hash code of the object
     */
    public int hashCode(X object);

    /**
     * Returns <code>true</code> if the given entity object should be persisted.
     * This is invoked for user types where the type <code>T</code> is an entity type.
     * If the type is not an entity type, returns false.
     *
     * @param entity The entity for which to determine whether it should be persisted
     * @return true if the entity should be persisted, false otherwise
     */
    public boolean shouldPersist(X entity);

    /**
     * Returns the nested properties of the object that are known to be dirty.
     * If the object isn't dirty i.e. doesn't need flushing, <code>null</code> is returned.
     * If the properties that are dirty aren't known or the type doesn't have nested properties,
     * an empty array or {@linkplain #DIRTY_MARKER} is returned.
     *
     * @param object The object for which to determine the dirty properties
     * @return the dirty properties of the object
     */
    public String[] getDirtyProperties(X object);

    /**
     * Clones the given object if the type is mutable to be able to detect mutations.
     * Immutable types may return the object itself. Types that can't be cloned easily can return the object too,
     * but should make sure, that the deep equality check always returns <code>false</code> or dirty checking is properly supported.
     *
     * @param object The object to clone
     * @return The cloned object
     */
    public X deepClone(X object);
}
