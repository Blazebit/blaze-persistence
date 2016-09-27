package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.UnaryExpressionPredicate;

import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class ExpressionModifiers {

    private final ArithmeticExpressionModifier.ArithmeticLeftExpressionModifier arithmeticLeftExpressionModifier = new ArithmeticExpressionModifier.ArithmeticLeftExpressionModifier();
    private final ArithmeticExpressionModifier.ArithmeticRightExpressionModifier arithmeticRightExpressionModifier = new ArithmeticExpressionModifier.ArithmeticRightExpressionModifier();
    private final BinaryExpressionPredicateModifier.BinaryExpressionPredicateLeftModifier binaryExpressionPredicateLeftModifier = new BinaryExpressionPredicateModifier.BinaryExpressionPredicateLeftModifier();
    private final BinaryExpressionPredicateModifier.BinaryExpressionPredicateRightModifier binaryExpressionPredicateRightModifier = new BinaryExpressionPredicateModifier.BinaryExpressionPredicateRightModifier();
    private final UnaryExpressionPredicateModifier unaryExpressionPredicateModifier = new UnaryExpressionPredicateModifier();
    private final GeneralCaseExpressionModifier generalCaseExpressionModifier = new GeneralCaseExpressionModifier();
    private final WhenClauseExpressionModifier whenClauseExpressionModifier = new WhenClauseExpressionModifier();
    private final BetweenPredicateModifier.BetweenPredicateLeftModifier betweenPredicateLeftModifier = new BetweenPredicateModifier.BetweenPredicateLeftModifier();
    private final BetweenPredicateModifier.BetweenPredicateStartModifier betweenPredicateStartModifier = new BetweenPredicateModifier.BetweenPredicateStartModifier();
    private final BetweenPredicateModifier.BetweenPredicateEndModifier betweenPredicateEndModifier = new BetweenPredicateModifier.BetweenPredicateEndModifier();
    private final InPredicateLeftModifier inPredicateLeftModifier = new InPredicateLeftModifier();
    private final ExpressionListModifier<Expression> expressionListModifier = new ExpressionListModifier<Expression>();

    public ArithmeticExpressionModifier.ArithmeticLeftExpressionModifier getArithmeticLeftExpressionModifier(ArithmeticExpression target) {
        arithmeticLeftExpressionModifier.setTarget(target);
        return arithmeticLeftExpressionModifier;
    }

    public ArithmeticExpressionModifier.ArithmeticRightExpressionModifier getArithmeticRightExpressionModifier(ArithmeticExpression target) {
        arithmeticRightExpressionModifier.setTarget(target);
        return arithmeticRightExpressionModifier;
    }

    public BinaryExpressionPredicateModifier.BinaryExpressionPredicateLeftModifier getBinaryExpressionPredicateLeftModifier(BinaryExpressionPredicate target) {
        binaryExpressionPredicateLeftModifier.setTarget(target);
        return binaryExpressionPredicateLeftModifier;
    }

    public BinaryExpressionPredicateModifier.BinaryExpressionPredicateRightModifier getBinaryExpressionPredicateRightModifier(BinaryExpressionPredicate target) {
        binaryExpressionPredicateRightModifier.setTarget(target);
        return binaryExpressionPredicateRightModifier;
    }

    public UnaryExpressionPredicateModifier getUnaryExpressionPredicateModifier(UnaryExpressionPredicate target) {
        unaryExpressionPredicateModifier.setTarget(target);
        return unaryExpressionPredicateModifier;
    }

    public GeneralCaseExpressionModifier getGeneralCaseExpressionModifier(GeneralCaseExpression target) {
        generalCaseExpressionModifier.setTarget(target);
        return generalCaseExpressionModifier;
    }

    public WhenClauseExpressionModifier getWhenClauseExpressionModifier(WhenClauseExpression target) {
        whenClauseExpressionModifier.setTarget(target);
        return whenClauseExpressionModifier;
    }

    public BetweenPredicateModifier.BetweenPredicateLeftModifier getBetweenPredicateLeftModifier(BetweenPredicate target) {
        betweenPredicateLeftModifier.setTarget(target);
        return betweenPredicateLeftModifier;
    }

    public BetweenPredicateModifier.BetweenPredicateStartModifier getBetweenPredicateStartModifier(BetweenPredicate target) {
        betweenPredicateStartModifier.setTarget(target);
        return betweenPredicateStartModifier;
    }

    public BetweenPredicateModifier.BetweenPredicateEndModifier getBetweenPredicateEndModifier(BetweenPredicate target) {
        betweenPredicateEndModifier.setTarget(target);
        return betweenPredicateEndModifier;
    }

    public InPredicateLeftModifier getInPredicateLeftModifier(InPredicate target) {
        inPredicateLeftModifier.setTarget(target);
        return inPredicateLeftModifier;
    }

    public <E extends Expression> ExpressionListModifier<E> getExpressionListModifier(List<E> target) {
        expressionListModifier.setTarget((List<Expression>) target);
        return (ExpressionListModifier<E>) expressionListModifier;
    }
}
