package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.AbstractExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 03.08.2016.
 */
public class CompoundPredicate extends AbstractPredicate {

    private final List<Predicate> children;
    private final BooleanOperator operator;

    public CompoundPredicate(BooleanOperator operator) {
        this(operator, new ArrayList<Predicate>());
    }

    public CompoundPredicate(BooleanOperator operator, Predicate... children) {
        super(false);
        this.operator = operator;
        this.children = new ArrayList<Predicate>(Arrays.asList(children));
    }

    public CompoundPredicate(BooleanOperator operator, List<Predicate> children) {
        super(false);
        this.operator = operator;
        this.children = children;
    }

    public List<Predicate> getChildren() {
        return children;
    }

    public BooleanOperator getOperator() {
        return operator;
    }

    @Override
    public CompoundPredicate clone() {
        return new CompoundPredicate(operator, new ArrayList<Predicate>(children));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompoundPredicate)) return false;
        if (!super.equals(o)) return false;

        CompoundPredicate that = (CompoundPredicate) o;

        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        return operator == that.operator;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }

    public enum BooleanOperator {
        AND,
        OR
    }

}
