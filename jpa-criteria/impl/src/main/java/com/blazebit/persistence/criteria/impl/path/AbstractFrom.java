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

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.BlazeCollectionJoin;
import com.blazebit.persistence.criteria.BlazeFetchParent;
import com.blazebit.persistence.criteria.BlazeFrom;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.BlazeListJoin;
import com.blazebit.persistence.criteria.BlazeMapJoin;
import com.blazebit.persistence.criteria.BlazeSetJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.FromSelection;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractFrom<Z, X> extends AbstractPath<X> implements BlazeFrom<Z, X> {

    public static final JoinType DEFAULT_JOIN_TYPE = JoinType.INNER;

    private static final long serialVersionUID = 1L;

    private AbstractFrom<Z, X> correlationParent;
    private JoinScope<X> joinScope = new BasicJoinScope();

    private Set<AbstractJoin<X, ?>> joins;
    private Set<AbstractJoin<X, ?>> fetches;
    private Map<EntityType<? extends X>, TreatedPath<? extends X>> treatedPaths;

    public AbstractFrom(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType) {
        this(criteriaBuilder, javaType, null);
    }

    public AbstractFrom(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<?> pathSource) {
        super(criteriaBuilder, javaType, pathSource);
    }

    @Override
    public Selection<X> alias(String alias) {
        return new FromSelection<X>(this.criteriaBuilder, this, alias);
    }

    @Override
    public String getPathExpression() {
        return getAlias();
    }

    @Override
    public void renderPathExpression(RenderContext context) {
        prepareAlias(context);
        context.getBuffer().append(getAlias());
    }

    @Override
    protected boolean isDereferencable() {
        return true;
    }

    public void visit(ParameterVisitor visitor) {
        for (AbstractJoin<?, ?> j : joins) {
            j.visit(visitor);
        }
    }

    @Override
    public void prepareAlias(RenderContext context) {
        if (getAlias() == null) {
            if (isCorrelated()) {
                setAlias(getCorrelationParent().getAlias());
            } else {
                setAlias(context.generateAlias(getJavaType()));
            }
        }
    }

    @Override
    public void render(RenderContext context) {
        prepareAlias(context);
        context.getBuffer().append(getAlias());
    }

    @Override
    public Attribute<?, ?> getAttribute() {
        return null;
    }

    public BlazeFetchParent<?, Z> getParent() {
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Attribute<X, ?> findAttribute(String name) {
        return (Attribute<X, ?>) getManagedType().getAttribute(name);
    }

    @SuppressWarnings({"unchecked"})
    protected ManagedType<? super X> getManagedType() {
        return (ManagedType<? super X>) getModel();
    }

    @Override
    public boolean isCorrelated() {
        return correlationParent != null;
    }

    @Override
    public AbstractFrom<Z, X> getCorrelationParent() {
        if (correlationParent == null) {
            throw new IllegalStateException("This from node has no correlation parent!");
        }
        return correlationParent;
    }

    @SuppressWarnings({"unchecked"})
    public AbstractFrom<Z, X> correlateTo(SubqueryExpression<?> subquery) {
        final AbstractFrom<Z, X> correlationDelegate = createCorrelationDelegate();
        correlationDelegate.prepareCorrelationDelegate(this);
        return correlationDelegate;
    }

    protected abstract AbstractFrom<Z, X> createCorrelationDelegate();

    public void prepareCorrelationDelegate(AbstractFrom<Z, X> parent) {
        this.joinScope = new CorrelationJoinScope();
        this.correlationParent = parent;
    }

    @Override
    public String getAlias() {
        return isCorrelated() ? getCorrelationParent().getAlias() : super.getAlias();
    }

    @Override
    public abstract <T extends X> AbstractFrom<?, T> treatAs(Class<T> treatAsType);

    @SuppressWarnings("unchecked")
    protected final <N extends X, T extends TreatedPath<N>> T addTreatedPath(T treatedPath) {
        if (treatedPaths == null) {
            treatedPaths = new LinkedHashMap<>();
        }
        T realJoin = (T) treatedPaths.get(treatedPath.getTreatType());
        if (realJoin == null) {
            realJoin = treatedPath;
            treatedPaths.put(treatedPath.getTreatType(), treatedPath);
        }
        return realJoin;
    }

    public Collection<TreatedPath<? extends X>> getTreatedPaths() {
        if (treatedPaths == null) {
            return Collections.emptyList();
        }
        return treatedPaths.values();
    }

    /***************************************************
     * Joins
     ****************************************************/

    protected abstract boolean isJoinAllowed();

    protected JoinScope<X> getJoinScope() {
        return joinScope;
    }

    protected void checkJoinAllowed() {
        if (!isJoinAllowed()) {
            throw new IllegalArgumentException("Joins on '" + getPathExpression() + "' are not allowed");
        }
    }

    private void checkJoin(Attribute<?, ?> attribute, JoinType jt) {
        checkJoinAllowed();
        if (jt == JoinType.RIGHT) {
            throw new UnsupportedOperationException("RIGHT JOIN not supported");
        }

        ManagedType<?> t = getManagedType();
        if (t == null) {
            throw new IllegalArgumentException("Joins on '" + getPathExpression() + "' are not allowed");
        }
        // getAttribute will throw an exception if the attribute does not exist
        if (t.getAttribute(attribute.getName()) == null) {
            // Some old hibernate versions don't throw an exception but return null
            throw new IllegalArgumentException("Could not resolve attribute named: " + attribute.getName());
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Set<Join<X, ?>> getJoins() {
        return joins == null ? Collections.EMPTY_SET : (Set<Join<X, ?>>) (Set) joins;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Set<BlazeJoin<X, ?>> getBlazeJoins() {
        return joins == null ? Collections.EMPTY_SET : (Set<BlazeJoin<X, ?>>) (Set<?>) joins;
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> singularAttribute, String alias) {
        return join(singularAttribute, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt) {
        AbstractJoin<X, Y> join = constructJoin(attribute, alias, jt);
        joinScope.addJoin(join);
        return join;
    }

    private <Y> AbstractJoin<X, Y> constructJoin(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt) {
        if (Type.PersistenceType.BASIC.equals(attribute.getType().getPersistenceType())) {
            throw new IllegalArgumentException("Cannot join to attribute of basic type: " + attribute.getJavaType().getName());
        }

        checkJoin(attribute, jt);
        final Class<Y> attributeType = attribute.getBindableJavaType();
        SingularAttributeJoin<X, Y> join = new SingularAttributeJoin<X, Y>(criteriaBuilder, attributeType, this, attribute, jt);
        join.setAlias(alias);
        return join;
    }

    @Override
    public <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias) {
        return join(collection, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, String alias, JoinType jt) {
        final CollectionAttributeJoin<X, Y> join = constructJoin(collection, alias, jt);
        joinScope.addJoin(join);
        return join;
    }

    private <Y> CollectionAttributeJoin<X, Y> constructJoin(CollectionAttribute<? super X, Y> collection, String alias, JoinType jt) {
        checkJoin(collection, jt);
        final Class<Y> attributeType = collection.getBindableJavaType();
        CollectionAttributeJoin<X, Y> join = new CollectionAttributeJoin<X, Y>(criteriaBuilder, attributeType, this, collection, jt);
        join.setAlias(alias);
        return join;
    }

    @Override
    public <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias) {
        return join(set, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, String alias, JoinType jt) {
        final SetAttributeJoin<X, Y> join = constructJoin(set, alias, jt);
        joinScope.addJoin(join);
        return join;
    }

    private <Y> SetAttributeJoin<X, Y> constructJoin(SetAttribute<? super X, Y> set, String alias, JoinType jt) {
        checkJoin(set, jt);
        final Class<Y> attributeType = set.getBindableJavaType();
        SetAttributeJoin<X, Y> join = new SetAttributeJoin<X, Y>(criteriaBuilder, attributeType, this, set, jt);
        join.setAlias(alias);
        return join;
    }

    @Override
    public <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias) {
        return join(list, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, String alias, JoinType jt) {
        final ListAttributeJoin<X, Y> join = constructJoin(list, alias, jt);
        joinScope.addJoin(join);
        return join;
    }

    private <Y> ListAttributeJoin<X, Y> constructJoin(ListAttribute<? super X, Y> list, String alias, JoinType jt) {
        checkJoin(list, jt);
        final Class<Y> attributeType = list.getBindableJavaType();
        ListAttributeJoin<X, Y> join = new ListAttributeJoin<X, Y>(criteriaBuilder, attributeType, this, list, jt);
        join.setAlias(alias);
        return join;
    }

    @Override
    public <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias) {
        return join(map, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, String alias, JoinType jt) {
        final MapAttributeJoin<X, K, V> join = constructJoin(map, alias, jt);
        joinScope.addJoin(join);
        return join;
    }

    private <K, V> MapAttributeJoin<X, K, V> constructJoin(MapAttribute<? super X, K, V> map, String alias, JoinType jt) {
        checkJoin(map, jt);
        final Class<V> attributeType = map.getBindableJavaType();
        MapAttributeJoin<X, K, V> join = new MapAttributeJoin<X, K, V>(criteriaBuilder, attributeType, this, map, jt);
        join.setAlias(alias);
        return join;
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias) {
        return join(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, Y> BlazeJoin<X, Y> join(String attributeName, String alias, JoinType jt) {
        checkJoinAllowed();
        if (jt == JoinType.RIGHT) {
            throw new UnsupportedOperationException("RIGHT JOIN not supported");
        }

        final Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (attribute.isCollection()) {
            final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
            if (PluralAttribute.CollectionType.COLLECTION.equals(pluralAttribute.getCollectionType())) {
                return (BlazeJoin<X, Y>) join((CollectionAttribute) attribute, alias, jt);
            } else if (PluralAttribute.CollectionType.LIST.equals(pluralAttribute.getCollectionType())) {
                return (BlazeJoin<X, Y>) join((ListAttribute) attribute, alias, jt);
            } else if (PluralAttribute.CollectionType.SET.equals(pluralAttribute.getCollectionType())) {
                return (BlazeJoin<X, Y>) join((SetAttribute) attribute, alias, jt);
            } else {
                return (BlazeJoin<X, Y>) join((MapAttribute) attribute, alias, jt);
            }
        } else {
            return (BlazeJoin<X, Y>) join((SingularAttribute) attribute, alias, jt);
        }
    }

    @Override
    public <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias) {
        return joinCollection(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, String alias, JoinType jt) {
        final Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (!attribute.isCollection()) {
            throw new IllegalArgumentException("Requested attribute was not a collection");
        }

        final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
        if (!PluralAttribute.CollectionType.COLLECTION.equals(pluralAttribute.getCollectionType())) {
            throw new IllegalArgumentException("Requested attribute was not a collection");
        }

        return (BlazeCollectionJoin<X, Y>) join((CollectionAttribute) attribute, alias, jt);
    }

    @Override
    public <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias) {
        return joinSet(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, String alias, JoinType jt) {
        final Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (!attribute.isCollection()) {
            throw new IllegalArgumentException("Requested attribute was not a set");
        }

        final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
        if (!PluralAttribute.CollectionType.SET.equals(pluralAttribute.getCollectionType())) {
            throw new IllegalArgumentException("Requested attribute was not a set");
        }

        return (BlazeSetJoin<X, Y>) join((SetAttribute) attribute, alias, jt);
    }

    @Override
    public <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias) {
        return joinList(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, String alias, JoinType jt) {
        final Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (!attribute.isCollection()) {
            throw new IllegalArgumentException("Requested attribute was not a list");
        }

        final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
        if (!PluralAttribute.CollectionType.LIST.equals(pluralAttribute.getCollectionType())) {
            throw new IllegalArgumentException("Requested attribute was not a list");
        }

        return (BlazeListJoin<X, Y>) join((ListAttribute) attribute, alias, jt);
    }

    @Override
    public <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias) {
        return joinMap(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, String alias, JoinType jt) {
        final Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (!attribute.isCollection()) {
            throw new IllegalArgumentException("Requested attribute was not a map");
        }

        final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
        if (!PluralAttribute.CollectionType.MAP.equals(pluralAttribute.getCollectionType())) {
            throw new IllegalArgumentException("Requested attribute was not a map");
        }

        return (BlazeMapJoin<X, K, V>) join((MapAttribute) attribute, alias, jt);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, String alias) {
        return join(entityType, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, String alias) {
        return join(criteriaBuilder.getEntityMetamodel().entity(entityTypeClass), alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType) {
        return join(entityType, null, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass) {
        return join(criteriaBuilder.getEntityMetamodel().entity(entityTypeClass), null, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, JoinType joinType) {
        return join(entityType, null, joinType);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, JoinType joinType) {
        return join(criteriaBuilder.getEntityMetamodel().entity(entityTypeClass), null, joinType);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(Class<Y> entityTypeClass, String alias, JoinType joinType) {
        return join(criteriaBuilder.getEntityMetamodel().entity(entityTypeClass), alias, joinType);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(EntityType<Y> entityType, String alias, JoinType joinType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Null entity type");
        }
        if (joinType == null) {
            throw new IllegalArgumentException("Null join type");
        }
        EntityJoin<X, Y> join = new EntityJoin<>(criteriaBuilder,this, entityType, joinType);
        join.setAlias(alias);
        joinScope.addJoin(join);
        return join;
    }

    /***************************************************
     * Fetches
     ****************************************************/

    protected boolean isFetchAllowed() {
        return isJoinAllowed();
    }

    protected void checkFetchAllowed() {
        if (!isFetchAllowed()) {
            throw new IllegalArgumentException("Join fetches on '" + getPathExpression() + "' are not allowed");
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Set<Fetch<X, ?>> getFetches() {
        return fetches == null ? Collections.EMPTY_SET : (Set<Fetch<X, ?>>) (Set) fetches;
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> singularAttribute, String alias) {
        return fetch(singularAttribute, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt) {
        checkFetchAllowed();
        AbstractJoin<X, Y> fetch = constructJoin(attribute, alias, jt);
        joinScope.addFetch(fetch);
        return fetch;
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute, String alias) {
        return fetch(pluralAttribute, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute, String alias, JoinType jt) {
        checkFetchAllowed();

        final AbstractJoin<X, Y> fetch;
        if (PluralAttribute.CollectionType.COLLECTION.equals(pluralAttribute.getCollectionType())) {
            fetch = constructJoin((CollectionAttribute<X, Y>) pluralAttribute, alias, jt);
        } else if (PluralAttribute.CollectionType.LIST.equals(pluralAttribute.getCollectionType())) {
            fetch = constructJoin((ListAttribute<X, Y>) pluralAttribute, alias, jt);
        } else if (PluralAttribute.CollectionType.SET.equals(pluralAttribute.getCollectionType())) {
            fetch = constructJoin((SetAttribute<X, Y>) pluralAttribute, alias, jt);
        } else {
            fetch = constructJoin((MapAttribute<X, ?, Y>) pluralAttribute, alias, jt);
        }
        joinScope.addFetch(fetch);
        return fetch;
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias) {
        return fetch(attributeName, alias, DEFAULT_JOIN_TYPE);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias, JoinType jt) {
        checkFetchAllowed();

        Attribute<X, ?> attribute = (Attribute<X, ?>) getAttribute(attributeName);
        if (attribute.isCollection()) {
            return (BlazeJoin<X, Y>) fetch((PluralAttribute) attribute, alias, jt);
        } else {
            return (BlazeJoin<X, Y>) fetch((SingularAttribute) attribute, alias, jt);
        }
    }
    
    /* Non-aliased joins and fetches */

    @Override
    public <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
        return fetch(attribute, (String) null);
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
        return fetch(attribute, null, jt);
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
        return fetch(attribute, (String) null);
    }

    @Override
    public <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) {
        return fetch(attribute, null, jt);
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> fetch(String attributeName) {
        return fetch(attributeName, (String) null);
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> fetch(String attributeName, JoinType jt) {
        return fetch(attributeName, (String) null, jt);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute) {
        return join(attribute, (String) null);
    }

    @Override
    public <Y> BlazeJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {
        return join(attribute, (String) null, jt);
    }

    @Override
    public <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {
        return join(collection, (String) null);
    }

    @Override
    public <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
        return join(set, (String) null);
    }

    @Override
    public <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
        return join(list, (String) null);
    }

    @Override
    public <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
        return join(map, (String) null);
    }

    @Override
    public <Y> BlazeCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
        return join(collection, (String) null, jt);
    }

    @Override
    public <Y> BlazeSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
        return join(set, (String) null, jt);
    }

    @Override
    public <Y> BlazeListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
        return join(list, (String) null, jt);
    }

    @Override
    public <K, V> BlazeMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
        return join(map, (String) null, jt);
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> join(String attributeName) {
        return join(attributeName, (String) null);
    }

    @Override
    public <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName) {
        return joinCollection(attributeName, (String) null);
    }

    @Override
    public <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName) {
        return joinSet(attributeName, (String) null);
    }

    @Override
    public <X, Y> BlazeListJoin<X, Y> joinList(String attributeName) {
        return joinList(attributeName, (String) null);
    }

    @Override
    public <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName) {
        return joinMap(attributeName, (String) null);
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> join(String attributeName, JoinType jt) {
        return join(attributeName, (String) null, jt);
    }

    @Override
    public <X, Y> BlazeCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
        return joinCollection(attributeName, (String) null, jt);
    }

    @Override
    public <X, Y> BlazeSetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
        return joinSet(attributeName, (String) null, jt);
    }

    @Override
    public <X, Y> BlazeListJoin<X, Y> joinList(String attributeName, JoinType jt) {
        return joinList(attributeName, (String) null, jt);
    }

    @Override
    public <X, K, V> BlazeMapJoin<X, K, V> joinMap(String attributeName, JoinType joinType) {
        return joinMap(attributeName, (String) null, joinType);
    }

    /* Join scope implementations */

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static interface JoinScope<X> extends Serializable {

        public void addJoin(AbstractJoin<X, ?> join);

        public void addFetch(AbstractJoin<X, ?> fetch);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected class BasicJoinScope implements JoinScope<X> {

        @Override
        public void addJoin(AbstractJoin<X, ?> join) {
            if (joins == null) {
                joins = new LinkedHashSet<>();
            }
            joins.add(join);
        }

        @Override
        public void addFetch(AbstractJoin<X, ?> fetch) {
            fetch.setFetch(true);
            addJoin(fetch);
            if (fetches == null) {
                fetches = new LinkedHashSet<>();
            }
            fetches.add(fetch);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected class CorrelationJoinScope implements JoinScope<X> {

        @Override
        public void addJoin(AbstractJoin<X, ?> join) {
            if (joins == null) {
                joins = new LinkedHashSet<>();
            }
            joins.add(join);
        }

        @Override
        public void addFetch(AbstractJoin<X, ?> fetch) {
            throw new UnsupportedOperationException("Cannot define fetch from a subquery correlation");
        }
    }
}
