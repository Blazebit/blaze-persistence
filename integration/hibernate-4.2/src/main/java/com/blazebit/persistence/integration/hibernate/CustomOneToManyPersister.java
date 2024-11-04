/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.CustomCollectionPersister;
import com.blazebit.persistence.integration.hibernate.base.SubselectLoaderUtils;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.persister.collection.OneToManyPersister;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomOneToManyPersister extends OneToManyPersister implements CustomCollectionPersister {

    private final String mappedByProperty;

    public CustomOneToManyPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
        super(collection, cacheAccessStrategy, cfg, factory);
        String referencedPropertyName = collection.getReferencedPropertyName();
        if (referencedPropertyName == null && collection.isInverse()) {
            referencedPropertyName = findMappedByProperty(collection);
        }
        this.mappedByProperty = referencedPropertyName;
    }

    @SuppressWarnings("unchecked")
    private String findMappedByProperty(Collection collection) {
        String ownerEntityName = collection.getOwnerEntityName();
        Iterator<Column> columnIterator = collection.getKey().getColumnIterator();
        List<String> columnNames = new ArrayList<>();
        while (columnIterator.hasNext()) {
            Column column = columnIterator.next();
            columnNames.add(column.getName());
        }

        OneToMany oneToMany = (OneToMany) collection.getElement();
        return findMappedByProperty(collection, ownerEntityName, columnNames, oneToMany.getAssociatedClass().getPropertyIterator());
    }

    private String findMappedByProperty(Collection collection, String ownerEntityName, List<String> columnNames, Iterator propertyIterator) {
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            if (property.getValue() instanceof Component) {
                String name = findMappedByProperty(collection, ownerEntityName, columnNames, ((Component) property.getValue()).getPropertyIterator());
                if (name != null) {
                    return property.getName() + "." + name;
                }
            } else if (property.getValue() instanceof ToOne) {
                ToOne toOne = (ToOne) property.getValue();
                if (ownerEntityName.equals(toOne.getReferencedEntityName())
                        && matches(columnNames, collection.getKey().getColumnIterator())) {
                    return property.getName();
                }
            }
        }

        return null;
    }

    private boolean matches(List<String> columns, Iterator<Column> iter) {
        for (int i = 0; iter.hasNext(); i++) {
            Column column = iter.next();
            if (i == columns.size() || !columns.get(i).equals(column.getName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getMappedByProperty() {
        return mappedByProperty;
    }

    @Override
    protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
        return new CustomSubselectOneToManyLoader(
                this,
                SubselectLoaderUtils.getSubselectQueryForHibernatePre5(subselect.toSubselectString(getCollectionType().getLHSPropertyName())),
                subselect.getResult(),
                subselect.getQueryParameters(),
                subselect.getNamedParameterLocMap(),
                session.getFactory(),
                session.getLoadQueryInfluencers()
        );
    }
}