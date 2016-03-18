package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.PredicateQuantifier;


public class QuantifierExpression implements Expression {

    private final PredicateQuantifier quantifier;
    private final FooExpression expression;
    
    public QuantifierExpression(PredicateQuantifier quantifier, FooExpression expression) {
        this.quantifier = quantifier;
        this.expression = expression;
    }
    
    public PredicateQuantifier getQuantifier() {
        return quantifier;
    }
    
    public FooExpression getExpression() {
        return expression;
    }

    @Override
    public Expression clone() {
        return new QuantifierExpression(quantifier, expression);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
        
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((quantifier == null) ? 0 : quantifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuantifierExpression other = (QuantifierExpression) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        if (quantifier != other.quantifier)
            return false;
        return true;
    }

}
