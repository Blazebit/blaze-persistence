package com.blazebit.persistence.criteria.impl.expression;

import java.util.Collection;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.path.PluralAttributePath;

import javax.persistence.criteria.Predicate;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IsEmptyPredicate<C extends Collection> extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;
    
    private final PluralAttributePath<C> collectionPath;

    public IsEmptyPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, PluralAttributePath<C> collectionPath) {
        super(criteriaBuilder, negated);
        this.collectionPath = collectionPath;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new IsEmptyPredicate<C>(criteriaBuilder, !isNegated(), collectionPath);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        context.apply(collectionPath);
        
        if (isNegated()) {
            context.getBuffer().append(" IS NOT EMPTY");
        } else {
            context.getBuffer().append(" IS EMPTY");
        }
    }

}
