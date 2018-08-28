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

package com.blazebit.persistence.spi;

import com.blazebit.persistence.JoinType;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link javax.persistence.metamodel.Attribute} that allows additionally efficient access to properties of the metamodel.
 *
 * @param <X> The Java type represented by the managed type owning the attribute
 * @param <Y> The Java element type of the attribute
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExtendedAttribute<X, Y> {

    /**
     * Returns the underlying attribute.
     *
     * @return The attribute
     */
    public Attribute<X, ?> getAttribute();

    /**
     * Returns the path from the owning entity type to this attribute.
     *
     * @return The path to the attribute
     */
    public List<Attribute<?, ?>> getAttributePath();

    /**
     * Returns the element type of the attribute.
     *
     * @return The element type
     */
    public Class<Y> getElementClass();

    /**
     * Returns whether the type of the attribute causes a cascading delete cycle.
     *
     * @return True if it has a cascading delete cycle, false otherwise
     */
    public boolean hasCascadingDeleteCycle();

    /**
     * Whether the join columns for the attribute are in a foreign table.
     *
     * @return True if join columns are in a foreign table, false otherwise
     */
    public boolean isForeignJoinColumn();

    /**
     * Whether columns for the attribute are shared between multiple subtypes
     * or shared by occupying the same slot in the resulting SQL.
     *
     * @return True if columns of the attribute are shared, false otherwise
     */
    public boolean isColumnShared();

    /**
     * Whether the attribute is a non-indexed and non-ordered collection a.k.a. a bag.
     *
     * @return True if it is a bag, false otherwise
     */
    public boolean isBag();

    /**
     * Whether orphan removal is activated for the attribute.
     *
     * @return True if orphan removal is activated, else false
     */
    public boolean isOrphanRemoval();

    /**
     * Whether delete cascading is activated for the attribute.
     *
     * @return True if delete cascading is activated, else false
     */
    public boolean isDeleteCascaded();

    /**
     * Returns where to put treat filters for a treat joined association of this attribute.
     *
     * @param joinType The join type used for the treat join
     * @return The constraint type for the treat filter
     */
    public JpaProvider.ConstraintType getJoinTypeIndexedRequiresTreatFilter(JoinType joinType);

    /**
     * If the attribute is <em>insertable = false</em> and <em>updatable = false</em> it returns the writable mappings for the inverse type.
     * Otherwise returns null.
     *
     * @param inverseType The type containing the inverse relation
     * @return The writable mappings for the inverse type if the attribute is not insertable or updatable, null otherwise
     */
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType);

    /**
     * If the attribute is an inverse collection, the mapped by attribute name is returned.
     * Otherwise returns null.
     *
     * @return The mapped by attribute name if the attribute is an inverse collection, null otherwise
     */
    public String getMappedBy();

    /**
     * If the attribute is a collection that uses a join table, returns it's descriptor.
     * Otherwise returns null.
     *
     * @return The join table information if the attribute has one, null otherwise
     */
    public JoinTable getJoinTable();

    /**
     * Returns the column names of the attribute.
     *
     * @return The column names of the attribute
     */
    public String[] getColumnNames();

    /**
     * Returns the SQL column type names of the attribute.
     *
     * @return The SQL column type names for the attribute
     */
    public String[] getColumnTypes();

    /**
     * Returns the attributes that have equivalent SQL column names.
     *
     * @return The attributes that have equivalent SQL column names
     * @since 1.3.0
     */
    public Set<ExtendedAttribute<X, ?>> getColumnEquivalentAttributes();
}

