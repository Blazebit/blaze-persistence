package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.NodeInfo;
import com.blazebit.persistence.impl.SelectManager;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 27.09.2016.
 */
public class NodeInfoExpressionModifier implements ExpressionModifier<Expression> {

    private final NodeInfo target;

    public NodeInfoExpressionModifier(NodeInfo target) {
        this.target = target;
    }

    public NodeInfoExpressionModifier(NodeInfoExpressionModifier original) {
        this.target = original.target;
    }

    @Override
    public void set(Expression expression) {
        target.setExpression(expression);
    }

    @Override
    public Object clone() {
        return new NodeInfoExpressionModifier(this);
    }
}