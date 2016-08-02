package com.blazebit.persistence.criteria.impl.expression;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Selection;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractSelection<X> extends AbstractTupleElement<X> implements Selection<X> {

    private static final long serialVersionUID = 1L;

    public AbstractSelection(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType) {
        super(criteriaBuilder, javaType);
    }

    @Override
    public Selection<X> alias(String alias) {
        setAlias(alias);
        return this;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException("Not a compound selection");
    }

    public void visitParameters(ParameterVisitor visitor) {
    }
    
    public abstract void render(RenderContext context);
    
}
