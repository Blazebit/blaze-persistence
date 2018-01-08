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

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.InverseRemoveStrategy;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Mapping of an entity view method attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewMethodAttributeMapping extends EntityViewAttributeMapping {

    /**
     * Returns the name of this attribute.
     *
     * @return The attribute name
     */
    public String getName();

    /**
     * Returns the getter method represented by this attribute mapping.
     *
     * @return The getter method represented by this attribute mapping
     */
    public Method getMethod();

    /**
     * Returns whether the attribute is updatable i.e. the JPA attribute to which the attribute is mapped
     * via the mapping is updatable. If <code>null</code>(the default), whether the attribute is updatable is determined
     * during the building phase({@link EntityViewConfiguration#createEntityViewManager(CriteriaBuilderFactory)}).
     *
     * @return Whether the attribute is updatable or <code>null</code> if updatability should be determined during building phase
     */
    public Boolean getUpdatable();

    /**
     * Returns whether the elements that are removed from the attribute should be deleted.
     * If <code>null</code>(the default), whether the attribute is updatable is determined
     * during the building phase({@link EntityViewConfiguration#createEntityViewManager(CriteriaBuilderFactory)}).
     *
     * @return Whether the attribute should do orphan removal or <code>null</code> if that should be determined during building phase
     */
    public Boolean getOrphanRemoval();

    /**
     * Returns the cascade types that are configured for this attribute.
     *
     * @return The cascade types
     */
    public Set<CascadeType> getCascadeTypes();

    /**
     * Set whether the attribute is updatable along with cascading configuration and the allowed subtypes.
     *
     * @param updatable Whether the attribute should be updatable
     * @param orphanRemoval Whether orphaned objects should be deleted
     * @param cascadeTypes The enabled cascade types
     * @param subtypes The allowed subtypes for both, persist and update cascades
     * @param persistSubtypes The allowed subtypes for persist cascades
     * @param updateSubtypes The allowed subtypes for update cascades
     */
    public void setUpdatable(boolean updatable, boolean orphanRemoval, CascadeType[] cascadeTypes, Class<?>[] subtypes, Class<?>[] persistSubtypes, Class<?>[] updateSubtypes);

    /**
     * Returns the mapping to the inverse attribute relative to the element type or <code>null</code> if there is none.
     *
     * @return The mapping to the inverse attribute
     */
    public String getMappedBy();

    /**
     * Set the mapping to the inverse attribute.
     *
     * @param mappedBy The mapping
     */
    public void setMappedBy(String mappedBy);

    /**
     * Returns the inverse remove strategy to use if this is an inverse mapping. Returns {@link InverseRemoveStrategy#SET_NULL} by default.
     *
     * @return the inverse remove strategy
     */
    public InverseRemoveStrategy getInverseRemoveStrategy();

    /**
     * Sets the inverse remove strategy.
     *
     * @param inverseRemoveStrategy The strategy
     */
    public void setInverseRemoveStrategy(InverseRemoveStrategy inverseRemoveStrategy);
}
