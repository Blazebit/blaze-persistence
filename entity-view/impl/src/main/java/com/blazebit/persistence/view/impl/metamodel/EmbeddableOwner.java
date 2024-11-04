/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EmbeddableOwner {
    private final Class<?> entityClass;
    private final String embeddableMapping;

    public EmbeddableOwner(Class<?> entityClass, String embeddableMapping) {
        this.entityClass = entityClass;
        this.embeddableMapping = embeddableMapping;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getEmbeddableMapping() {
        return embeddableMapping;
    }

    public EmbeddableOwner withSubMapping(String mapping) {
        return new EmbeddableOwner(entityClass, embeddableMapping + "." + mapping);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmbeddableOwner)) {
            return false;
        }

        EmbeddableOwner that = (EmbeddableOwner) o;

        if (!getEntityClass().equals(that.getEntityClass())) {
            return false;
        }
        return getEmbeddableMapping().equals(that.getEmbeddableMapping());
    }

    @Override
    public int hashCode() {
        int result = getEntityClass().hashCode();
        result = 31 * result + getEmbeddableMapping().hashCode();
        return result;
    }
}
