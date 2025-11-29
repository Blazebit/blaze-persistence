/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingularAttributePath<X> extends AbstractPath<X> {

    private static final long serialVersionUID = 1L;

    private final SingularAttribute<?, X> attribute;
    private final ManagedType<X> managedType;

    @SuppressWarnings("unchecked")
    private SingularAttributePath(BlazeCriteriaBuilderImpl criteriaBuilder, SingularAttributePath<? super X> original, EntityType<X> treatType) {
        super(criteriaBuilder, treatType.getJavaType(), original.getBasePath());
        this.attribute = (SingularAttribute<?, X>) original.getAttribute();
        this.managedType = treatType;
    }

    public SingularAttributePath(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<?> pathSource, SingularAttribute<?, X> attribute) {
        super(criteriaBuilder, javaType, pathSource);
        this.attribute = attribute;
        this.managedType = getManagedType(attribute);
    }

    private SingularAttributePath(SingularAttributePath<X> origin, String alias) {
        super(origin.criteriaBuilder, origin.getJavaType(), origin.getBasePath());
        this.attribute = origin.attribute;
        this.managedType = origin.managedType;
        this.setAlias(alias);
    }

    @Override
    public Selection<X> alias(String alias) {
        return new SingularAttributePath<X>(this, alias);
    }

    private ManagedType<X> getManagedType(SingularAttribute<?, X> attribute) {
        if (Attribute.PersistentAttributeType.BASIC == attribute.getPersistentAttributeType()) {
            return null;
        } else if (Attribute.PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType()) {
            return (EmbeddableType<X>) attribute.getType();
        } else {
            return (IdentifiableType<X>) attribute.getType();
        }
    }

    @Override
    public SingularAttribute<?, X> getAttribute() {
        return attribute;
    }

    @Override
    public Bindable<X> getModel() {
        return getAttribute();
    }

    @Override
    protected boolean isDereferencable() {
        return managedType != null;
    }

    @Override
    protected Attribute<?, ?> findAttribute(String attributeName) {
        final Attribute<?, ?> attribute = managedType.getAttribute(attributeName);
        // Some old hibernate versions don't throw an exception but return null
        if (attribute == null) {
            throw new IllegalArgumentException("Could not resolve attribute named: " + attributeName);
        }
        return attribute;
    }

    @Override
    public <T extends X> SingularAttributePath<T> treatAs(Class<T> treatAsType) {
        return new TreatedSingularAttributePath<T>(criteriaBuilder, this, getTreatType(treatAsType));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedSingularAttributePath<T> extends SingularAttributePath<T> implements TreatedPath<T> {

        private static final long serialVersionUID = 1L;

        private final SingularAttributePath<? super T> treatedPath;
        private final EntityType<T> treatType;

        public TreatedSingularAttributePath(BlazeCriteriaBuilderImpl criteriaBuilder, SingularAttributePath<? super T> treatedPath, EntityType<T> treatType) {
            super(criteriaBuilder, treatedPath, treatType);
            this.treatedPath = treatedPath;
            this.treatType = treatType;
        }

        @Override
        public EntityType<T> getTreatType() {
            return treatType;
        }

        @Override
        public AbstractPath<? super T> getTreatedPath() {
            return treatedPath;
        }

        @Override
        public String getAlias() {
            return treatedPath.getAlias();
        }

        @Override
        public String getPathExpression() {
            return "TREAT(" + treatedPath.getPathExpression() + " AS " + getTreatType().getName() + ')';
        }

        @Override
        public void renderPathExpression(RenderContext context) {
            render(context);
        }

        @Override
        public void render(RenderContext context) {
            final StringBuilder buffer = context.getBuffer();
            buffer.append("TREAT(");
            treatedPath.renderPathExpression(context);
            buffer.append(" AS ")
                    .append(getTreatType().getName())
                    .append(')');
        }
    }
}
