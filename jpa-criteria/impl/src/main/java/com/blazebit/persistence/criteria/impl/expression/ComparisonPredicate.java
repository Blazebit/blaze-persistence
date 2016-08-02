package com.blazebit.persistence.criteria.impl.expression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.TypeUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ComparisonPredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;
    
    private final ComparisonOperator comparisonOperator;
    private final Expression<?> leftHandSide;
    private final Expression<?> rightHandSide;

    public ComparisonPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, ComparisonOperator comparisonOperator, Expression<?> leftHandSide, Expression<?> rightHandSide) {
        super(criteriaBuilder, false);
        this.comparisonOperator = comparisonOperator;
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ComparisonPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, ComparisonOperator comparisonOperator, Expression<?> leftHandSide, Object rightHandSide) {
        super(criteriaBuilder, false);
        this.comparisonOperator = comparisonOperator;
        this.leftHandSide = leftHandSide;
        if (TypeUtils.isNumeric(leftHandSide.getJavaType())) {
            this.rightHandSide = new LiteralExpression(criteriaBuilder, TypeUtils.convert(rightHandSide, (Class<Number>) leftHandSide.getJavaType()));
        } else {
            this.rightHandSide = new LiteralExpression(criteriaBuilder, rightHandSide);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <N extends Number> ComparisonPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, ComparisonOperator comparisonOperator, Expression<N> leftHandSide, Number rightHandSide) {
        super(criteriaBuilder, false);
        this.comparisonOperator = comparisonOperator;
        this.leftHandSide = leftHandSide;
        Class type = leftHandSide.getJavaType();
        if (Number.class.equals(type)) {
            this.rightHandSide = new LiteralExpression(criteriaBuilder, rightHandSide);
        } else {
            N converted = (N) TypeUtils.convert(rightHandSide, type);
            this.rightHandSide = new LiteralExpression<N>(criteriaBuilder, converted);
        }
    }

    public static enum ComparisonOperator {
        EQUAL {
            public String getOperator() {
                return "=";
            }
            public ComparisonOperator getNegated() {
                return NOT_EQUAL;
            }
        },
        NOT_EQUAL {
            public String getOperator() {
                return "<>";
            }
            public ComparisonOperator getNegated() {
                return EQUAL;
            }
        },
        LESS_THAN {
            public String getOperator() {
                return "<";
            }
            public ComparisonOperator getNegated() {
                return GREATER_THAN_OR_EQUAL;
            }
        },
        LESS_THAN_OR_EQUAL {
            public String getOperator() {
                return "<=";
            }
            public ComparisonOperator getNegated() {
                return GREATER_THAN;
            }
        },
        GREATER_THAN {
            public String getOperator() {
                return ">";
            }
            public ComparisonOperator getNegated() {
                return LESS_THAN_OR_EQUAL;
            }
        },
        GREATER_THAN_OR_EQUAL {
            public String getOperator() {
                return ">=";
            }
            public ComparisonOperator getNegated() {
                return LESS_THAN;
            }
        };

        public abstract String getOperator();

        public abstract ComparisonOperator getNegated();
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new ComparisonPredicate(criteriaBuilder, comparisonOperator.getNegated(), leftHandSide, rightHandSide);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(leftHandSide);
        visitor.visit(rightHandSide);
    }

    @Override
    public void render(RenderContext context) {
        context.apply(leftHandSide);
        context.getBuffer().append(comparisonOperator.getOperator());
        context.apply(rightHandSide);
    }

}
