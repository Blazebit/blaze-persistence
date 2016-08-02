package com.blazebit.persistence.criteria.impl.path;

import java.io.Serializable;

import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RootImpl<X> extends AbstractFrom<X, X> implements BlazeRoot<X>, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final EntityType<X> entityType;
    private final boolean joinsAllowed;

    public RootImpl(BlazeCriteriaBuilderImpl criteriaBuilder, EntityType<X> entityType, String alias, boolean joinsAllowed) {
        super(criteriaBuilder, entityType.getJavaType());
        this.entityType = entityType;
        this.setAlias(alias);
        this.joinsAllowed = joinsAllowed;
    }

    public EntityType<X> getEntityType() {
        return entityType;
    }

    public EntityType<X> getModel() {
        return getEntityType();
    }

    @Override
    protected AbstractFrom<X, X> createCorrelationDelegate() {
        return new RootImpl<X>(criteriaBuilder, getEntityType(), getAlias(), true);
    }

    @Override
    public RootImpl<X> correlateTo(SubqueryExpression<?> subquery) {
        return (RootImpl<X>) super.correlateTo(subquery);
    }

    @Override
    protected boolean isJoinAllowed() {
        return joinsAllowed;
    }

    @Override
    protected void checkJoinAllowed() {
        if (!joinsAllowed) {
            throw new IllegalArgumentException("Update and delete criteria queries cannot have joins");
        }
        super.checkJoinAllowed();
    }

    @Override
    protected void checkFetchAllowed() {
        if (!joinsAllowed) {
            throw new IllegalArgumentException("Update and delete criteria queries cannot have join fetches");
        }
        super.checkFetchAllowed();
    }

    @Override
    public String getPathExpression() {
        return getAlias();
    }

    @Override
    public void render(RenderContext context) {
        prepareAlias(context);
        context.getBuffer().append(getAlias());
    }
}
