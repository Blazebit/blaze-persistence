/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.blazebit.persistence.impl.predicate.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.blazebit.persistence.parser.JPQLSelectExpressionBaseVisitor;
import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ArrayExpressionArithmeticIndexContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ArrayExpressionStringIndexContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Functions_returning_datetimeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.IndexFunctionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_arithmeticContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_booleanContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_datetimeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_entityContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_entitytypeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_stringContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TrimFunctionContext;

/**
 *
 * @author Moritz Becker
 */
public class JPQLSelectExpressionVisitorImpl extends JPQLSelectExpressionBaseVisitor<Expression> {

    private final CommonTokenStream tokens;
    private final Set<String> aggregateFunctions;

    public JPQLSelectExpressionVisitorImpl(CommonTokenStream tokens, Set<String> aggregateFunctions) {
        this.tokens = tokens;
        this.aggregateFunctions = aggregateFunctions;
    }

    @Override
    public Expression visitFunctions_returning_numerics_default(JPQLSelectExpressionParser.Functions_returning_numerics_defaultContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitOuter_expression(JPQLSelectExpressionParser.Outer_expressionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitCoalesce_expression(JPQLSelectExpressionParser.Coalesce_expressionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitFunctions_returning_numerics_size(JPQLSelectExpressionParser.Functions_returning_numerics_sizeContext ctx) {
        FunctionExpression func = handleFunction(ctx.getStart().getText(), ctx);
        ((PathExpression) func.getExpressions().get(0)).setUsedInCollectionFunction(true);
        return func;
    }

    @Override
    public Expression visitFunctions_returning_datetime(Functions_returning_datetimeContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitStringFunction(JPQLSelectExpressionParser.StringFunctionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitTrimFunction(TrimFunctionContext ctx) {
        Trimspec trimspec;
        
        if (ctx.trim_specification() != null) {
            trimspec = Trimspec.valueOf(ctx.trim_specification().getText().toUpperCase());
        } else {
            trimspec = Trimspec.BOTH;
        }
        
        Expression trimCharacter = null;
        
        if (ctx.trim_character() != null) {
            trimCharacter = ctx.trim_character().accept(this);
        }
        
        return new TrimExpression(trimspec, trimCharacter, ctx.string_expression().accept(this));
    }

    @Override
    public Expression visitAggregateExpression(JPQLSelectExpressionParser.AggregateExpressionContext ctx) {
        return new AggregateExpression(ctx.distinct != null, ctx.funcname.getText(), Arrays.asList((Expression) ctx.aggregate_argument()
            .accept(this)));
    }

    @Override
    public Expression visitCountStar(JPQLSelectExpressionParser.CountStarContext ctx) {
        return new AggregateExpression();
    }

    @Override
    public Expression visitNullif_expression(JPQLSelectExpressionParser.Nullif_expressionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitNull_literal(JPQLSelectExpressionParser.Null_literalContext ctx) {
        return new NullExpression();
    }

    private FunctionExpression handleFunction(String name, ParseTree ctx) {
        List<Expression> funcArgs = new ArrayList<Expression>(ctx.getChildCount());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!(ctx.getChild(i) instanceof TerminalNode)) {
                funcArgs.add(ctx.getChild(i).accept(this));
            }
        }

        if ("FUNCTION".equalsIgnoreCase(name) && funcArgs.size() > 0
            && aggregateFunctions.contains(getLiteralString(funcArgs.get(0)).toLowerCase())) {
            return new AggregateExpression(false, name, funcArgs);
        } else {
            return new FunctionExpression(name, funcArgs);
        }
    }

    @Override
    public Expression visitFunction_invocation(JPQLSelectExpressionParser.Function_invocationContext ctx) {
        List<Expression> funcArgs = new ArrayList<Expression>(ctx.getChildCount());
        funcArgs.add(ctx.string_literal().accept(this));
        for (JPQLSelectExpressionParser.Function_argContext argCtx : ctx.args) {
            funcArgs.add(argCtx.accept(this));
        }

        String name = ctx.getStart().getText();
        if ("FUNCTION".equalsIgnoreCase(name) && funcArgs.size() > 0
            && aggregateFunctions.contains(getLiteralString(funcArgs.get(0)).toLowerCase())) {
            return new AggregateExpression(false, name, funcArgs);
        } else {
            return new FunctionExpression(name, funcArgs);
        }
    }

    private String getLiteralString(Expression expr) {
        String str = expr.toString();
        return str.substring(1, str.length() - 1);
    }

    @Override
    public Expression visitGeneral_subpath(JPQLSelectExpressionParser.General_subpathContext ctx) {
        List<PathElementExpression> pathElements = new ArrayList<PathElementExpression>();
        pathElements.add((PathElementExpression) ctx.general_path_start().accept(this));
        for (JPQLSelectExpressionParser.General_path_elementContext generalPathElem : ctx.general_path_element()) {
            pathElements.add((PathElementExpression) generalPathElem.accept(this));
        }
        return new PathExpression(pathElements);
    }

    @Override
    public Expression visitPath(JPQLSelectExpressionParser.PathContext ctx) {
        PathExpression result = (PathExpression) ctx.general_subpath().accept(this);
        result.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));
        return result;
    }

    @Override
    public Expression visitSingle_element_path_expression(JPQLSelectExpressionParser.Single_element_path_expressionContext ctx) {
        return new PathExpression(new ArrayList<PathElementExpression>(Arrays.asList((PathElementExpression) ctx.general_path_start().accept(this))));
    }

    @Override
    public Expression visitSimple_path_element(JPQLSelectExpressionParser.Simple_path_elementContext ctx) {
        return new PropertyExpression(ctx.identifier().getText());
    }

    @Override
    public Expression visitCollection_member_expression(JPQLSelectExpressionParser.Collection_member_expressionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setUsedInCollectionFunction(true);
        return new MemberOfPredicate(ctx.entity_or_value_expression().accept(this), collectionPath, ctx.not != null);
    }

    @Override
    public Expression visitEmpty_collection_comparison_expression(JPQLSelectExpressionParser.Empty_collection_comparison_expressionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setUsedInCollectionFunction(true);
        return new IsEmptyPredicate(collectionPath, ctx.not != null);
    }

    @Override
    public Expression visitType_discriminator(JPQLSelectExpressionParser.Type_discriminatorContext ctx) {
        return new FunctionExpression(ctx.getStart().getText(), Arrays.asList(ctx.type_discriminator_arg().accept(this)));
    }

    @Override
    public Expression visitEntryFunction(JPQLSelectExpressionParser.EntryFunctionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        return new FunctionExpression(ctx.name.getText(), Arrays.asList(collectionPath));
    }

    @Override
    public Expression visitKey_value_expression(JPQLSelectExpressionParser.Key_value_expressionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        return new FunctionExpression(ctx.name.getText(), Arrays.asList(collectionPath));
    }

    @Override
    public Expression visitIndexFunction(IndexFunctionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        return new FunctionExpression(ctx.getStart().getText(), Arrays.asList(collectionPath));
    }

    @Override
    public Expression visitArrayExpressionArithmeticIndex(ArrayExpressionArithmeticIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), unwrap(ctx.arithmetic_expression().accept(this)));
    }
    
    @Override
    public Expression visitArrayExpressionStringIndex(ArrayExpressionStringIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), unwrap(ctx.string_expression().accept(this)));
    }
    
    @Override
    public Expression visitArithmeticExpressionPlusMinus(JPQLSelectExpressionParser.ArithmeticExpressionPlusMinusContext ctx) {
        CompositeExpression expr = accept(ctx.arithmetic_expression());
        expr.append(getTextWithSurroundingHiddenTokens(ctx.op));
        acceptAndCompose(expr, ctx.arithmetic_term());
        return expr;
    }

    @Override
    public Expression visitBetweenArithmetic(JPQLSelectExpressionParser.BetweenArithmeticContext ctx) {
        return new BetweenPredicate(ctx.expr.accept(this), ctx.bound1.accept(this), ctx.bound2.accept(this), ctx.not != null);
    }

    @Override
    public Expression visitBetweenDatetime(JPQLSelectExpressionParser.BetweenDatetimeContext ctx) {
        return new BetweenPredicate(ctx.expr.accept(this), ctx.bound1.accept(this), ctx.bound2.accept(this), ctx.not != null);
    }

    @Override
    public Expression visitBetweenString(JPQLSelectExpressionParser.BetweenStringContext ctx) {
        return new BetweenPredicate(ctx.expr.accept(this), ctx.bound1.accept(this), ctx.bound2.accept(this), ctx.not != null);
    }

    @Override
    public Expression visitArithmeticPrimaryParanthesis(JPQLSelectExpressionParser.ArithmeticPrimaryParanthesisContext ctx) {
        CompositeExpression expr = accept(ctx.arithmetic_expression());
        expr.prepend("(" + tokenListToString(tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex())).toString());
        expr.append(")");
        return expr;
    }

    @Override
    public Expression visitArithmeticMultDiv(JPQLSelectExpressionParser.ArithmeticMultDivContext ctx) {
        CompositeExpression expr = accept(ctx.term);
        expr.append(getTextWithSurroundingHiddenTokens(ctx.op));
        acceptAndCompose(expr, ctx.factor);

        return expr;
    }

    @Override
    public Expression visitConditionalTerm_and(JPQLSelectExpressionParser.ConditionalTerm_andContext ctx) {
        Predicate left = (Predicate) ctx.conditional_term().accept(this);
        if (left instanceof AndPredicate) {
            ((AndPredicate) left).getChildren().add((Predicate) ctx.conditional_factor().accept(this));
            return left;
        } else {
            return new AndPredicate(left, (Predicate) ctx.conditional_factor().accept(this));
        }
    }

    @Override
    public Expression visitConditionalPrimary(JPQLSelectExpressionParser.ConditionalPrimaryContext ctx) {
        return ctx.conditional_expression().accept(this);
    }

    @Override
    public Expression visitConditional_factor(JPQLSelectExpressionParser.Conditional_factorContext ctx) {
        Predicate p = (Predicate) ctx.conditional_primary().accept(this);

        if (ctx.not != null) {
            if (p instanceof Negatable) {
                Negatable n = (Negatable) p;
                n.setNegated(!n.isNegated());
            } else {
                p = new NotPredicate(p);
            }
        }
        return p;
    }

    @Override
    public Expression visitConditionalExpression_or(JPQLSelectExpressionParser.ConditionalExpression_orContext ctx) {
        Predicate left = (Predicate) ctx.conditional_expression().accept(this);
        if (left instanceof OrPredicate) {
            ((OrPredicate) left).getChildren().add((Predicate) ctx.conditional_term().accept(this));
            return left;
        } else {
            return new OrPredicate(left, (Predicate) ctx.conditional_term().accept(this));
        }
    }

    @Override
    public Expression visitArithmetic_factor(JPQLSelectExpressionParser.Arithmetic_factorContext ctx) {
        if (ctx.signum != null) {
            CompositeExpression expr = accept(ctx.arithmetic_primary());
            expr.prepend(ctx.signum.getText() + tokenListToString(tokens.getHiddenTokensToRight(ctx.signum.getTokenIndex())));
            return expr;
        } else {
            return ctx.arithmetic_primary().accept(this);
        }
    }

    @Override
    public Expression visitNull_comparison_expression(JPQLSelectExpressionParser.Null_comparison_expressionContext ctx) {
        return new IsNullPredicate(ctx.getChild(0).accept(this), ctx.not != null);
    }

    @Override
    public Expression visitLike_expression(JPQLSelectExpressionParser.Like_expressionContext ctx) {
        // @formatter:off
        return new LikePredicate(
                ctx.string_expression().accept(this),
                ctx.pattern_value().accept(this),
                true,
                ctx.escape_character() != null ? ctx.escape_character().accept(this).toString().charAt(1) : null,
                ctx.not != null);
        // @formatter:on
    }

    @Override
    public Expression visitGeneral_case_expression(JPQLSelectExpressionParser.General_case_expressionContext ctx) {
        List<WhenClauseExpression> whenClauses = new ArrayList<WhenClauseExpression>();
        for (JPQLSelectExpressionParser.When_clauseContext whenClause : ctx.when_clause()) {
            whenClauses.add((WhenClauseExpression) whenClause.accept(this));
        }
        return new GeneralCaseExpression(whenClauses, ctx.scalar_expression().accept(this));
    }

    @Override
    public Expression visitSimple_case_expression(JPQLSelectExpressionParser.Simple_case_expressionContext ctx) {
        List<WhenClauseExpression> whenClauses = new ArrayList<WhenClauseExpression>();
        for (JPQLSelectExpressionParser.Simple_when_clauseContext whenClause : ctx.simple_when_clause()) {
            whenClauses.add((WhenClauseExpression) whenClause.accept(this));
        }
        return new SimpleCaseExpression(ctx.case_operand().accept(this), whenClauses, ctx.scalar_expression().accept(this));
    }

    @Override
    public Expression visitWhen_clause(JPQLSelectExpressionParser.When_clauseContext ctx) {
        return handleWhenClause(ctx.conditional_expression(), ctx.scalar_expression());
    }

    @Override
    public Expression visitSimple_when_clause(JPQLSelectExpressionParser.Simple_when_clauseContext ctx) {
        return handleWhenClause(ctx.scalar_expression(0), ctx.scalar_expression(1));
    }

    private WhenClauseExpression handleWhenClause(ParserRuleContext condition, ParserRuleContext result) {
        return new WhenClauseExpression(condition.accept(this), result.accept(this));
    }

    @Override
    public Expression visitErrorNode(ErrorNode node) {
        throw new SyntaxErrorException("Parsing failed: " + node.getText());
    }

    @Override
    public Expression visitIn_expression(JPQLSelectExpressionParser.In_expressionContext ctx) {
        Expression inExpr;
        if (ctx.param == null && ctx.right == null) {
            CompositeExpression compositeInExpr = accept(ctx.in_item(0));
            compositeInExpr.prepend("(");
            for (int i = 1; i < ctx.in_item().size(); i++) {
                compositeInExpr.append(",");
                acceptAndCompose(compositeInExpr, ctx.in_item(i));
            }
            compositeInExpr.append(")");
            inExpr = unwrap(compositeInExpr);
        } else if (ctx.param != null) {
            inExpr = ctx.Input_parameter().accept(this);
        } else {
            inExpr = ctx.Identifier().get(ctx.Identifier().size() - 1).accept(this);
        }
        return new InPredicate(ctx.getChild(0).accept(this), inExpr, ctx.not != null);
    }

    @Override
    public Expression visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == JPQLSelectExpressionLexer.EOF) {
            return null;
        }
        switch (node.getSymbol().getType()) {
            case JPQLSelectExpressionLexer.Input_parameter:
                return new ParameterExpression(node.getText().substring(1));
            default:
                return new FooExpression(node.getText());
        }
    }

    @Override
    public Expression visitParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
        return unwrap(super.visitParseSimpleExpression(ctx));
    }

    @Override
    public Expression visitParseSimpleSubqueryExpression(JPQLSelectExpressionParser.ParseSimpleSubqueryExpressionContext ctx) {
        return unwrap(super.visitParseSimpleSubqueryExpression(ctx));
    }

    @Override
    public Expression visitParseOrderByClause(JPQLSelectExpressionParser.ParseOrderByClauseContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitEnum_literal(JPQLSelectExpressionParser.Enum_literalContext ctx) {
        return new LiteralExpression("ENUM", ctx.path().accept(this).toString());
    }

    @Override
    public Expression visitEntity_type_literal(JPQLSelectExpressionParser.Entity_type_literalContext ctx) {
        return new LiteralExpression("ENTITY", ctx.identifier().getText());
    }

    @Override
    public Expression visitComparisonExpression_string(JPQLSelectExpressionParser.ComparisonExpression_stringContext ctx) {
        return handleComparison(ctx.left, ctx.comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_string(QuantifiedComparisonExpression_stringContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_arithmetic(JPQLSelectExpressionParser.ComparisonExpression_arithmeticContext ctx) {
        return handleComparison(ctx.left, ctx.comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_arithmetic(QuantifiedComparisonExpression_arithmeticContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_entitytype(JPQLSelectExpressionParser.ComparisonExpression_entitytypeContext ctx) {
        return handleComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_entitytype(QuantifiedComparisonExpression_entitytypeContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_boolean(JPQLSelectExpressionParser.ComparisonExpression_booleanContext ctx) {
        return handleComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_boolean(QuantifiedComparisonExpression_booleanContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_datetime(JPQLSelectExpressionParser.ComparisonExpression_datetimeContext ctx) {
        return handleComparison(ctx.left, ctx.comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_datetime(QuantifiedComparisonExpression_datetimeContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_entity(JPQLSelectExpressionParser.ComparisonExpression_entityContext ctx) {
        return handleComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right);
    }

    @Override
    public Expression visitQuantifiedComparisonExpression_entity(QuantifiedComparisonExpression_entityContext ctx) {
        return handleQuantifiedComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right, toQuantifier(ctx.quantifier));
    }

    @Override
    public Expression visitComparisonExpression_enum(JPQLSelectExpressionParser.ComparisonExpression_enumContext ctx) {
        return handleComparison(ctx.left, ctx.equality_comparison_operator(), ctx.right);
    }

    BinaryExpressionPredicate handleComparison(ParseTree left, ParseTree comparisonOperator, ParseTree right) {
        BinaryExpressionPredicate pred = (BinaryExpressionPredicate) comparisonOperator.accept(this);
        pred.setLeft(left.accept(this));
        pred.setRight(right.accept(this));
        return pred;
    }

    BinaryExpressionPredicate handleQuantifiedComparison(ParseTree left, ParseTree comparisonOperator, ParseTree right, PredicateQuantifier quantifier) {
        QuantifiableBinaryExpressionPredicate pred = (QuantifiableBinaryExpressionPredicate) comparisonOperator.accept(this);
        pred.setLeft(left.accept(this));
        pred.setRight(right.accept(this));
        pred.setQuantifier(quantifier);
        return pred;
    }

    @Override
    public Expression visitEqPredicate(JPQLSelectExpressionParser.EqPredicateContext ctx) {
        return new EqPredicate(false);
    }

    @Override
    public Expression visitNeqPredicate(JPQLSelectExpressionParser.NeqPredicateContext ctx) {
        return new EqPredicate(true);
    }

    @Override
    public Expression visitGtPredicate(JPQLSelectExpressionParser.GtPredicateContext ctx) {
        return new GtPredicate();
    }

    @Override
    public Expression visitGePredicate(JPQLSelectExpressionParser.GePredicateContext ctx) {
        return new GePredicate();
    }

    @Override
    public Expression visitLtPredicate(JPQLSelectExpressionParser.LtPredicateContext ctx) {
        return new LtPredicate();
    }

    @Override
    public Expression visitLePredicate(JPQLSelectExpressionParser.LePredicateContext ctx) {
        return new LePredicate();
    }

    @Override
    public Expression visitExists_expression(JPQLSelectExpressionParser.Exists_expressionContext ctx) {
        return new ExistsPredicate(ctx.identifier().accept(this), ctx.not != null);
    }

    @Override
    public Expression visitChildren(RuleNode node) {
        CompositeExpression result = null;
        int n = node.getChildCount();

        if (shouldVisitNextChild(node, result)) {
            if (n > 0 && shouldVisitNextChild(node, result)) {
                if (n == 1) {
                    return node.getChild(0).accept(this);
                } else {
                    result = accept(node.getChild(0));
                    for (int i = 1; i < n; i++) {
                        if (!shouldVisitNextChild(node, result)) {
                            break;
                        }

                        ParseTree c = node.getChild(i);
                        acceptAndCompose(result, c);
                    }
                }
            }
        }

        return result;
    }

    private StringBuilder getTextWithSurroundingHiddenTokens(Token token) {
        StringBuilder sb = new StringBuilder();
        List<Token> hiddenTokens = tokens.getHiddenTokensToLeft(token.getTokenIndex());
        if (hiddenTokens != null) {
            for (Token t : hiddenTokens) {
                sb.append(t.getText());
            }
        }
        sb.append(token.getText());
        hiddenTokens = tokens.getHiddenTokensToRight(token.getTokenIndex());
        if (hiddenTokens != null) {
            for (Token t : hiddenTokens) {
                sb.append(t.getText());
            }
        }
        return sb;
    }

    private CompositeExpression acceptAndCompose(CompositeExpression composite, ParseTree ruleContext) {
        Expression expr = ruleContext.accept(this);
        if (expr != null) {
            composite.append(expr);
        }

        return composite;
    }

    private CompositeExpression accept(ParseTree ruleContext) {
        Expression expr = ruleContext.accept(this);
        CompositeExpression composite;
        if (expr instanceof CompositeExpression) {
            composite = (CompositeExpression) expr;
        } else {
            composite = new CompositeExpression(new ArrayList<Expression>(Arrays.asList(expr)));
        }
        return composite;
    }

    private StringBuilder tokenListToString(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        if (tokens != null) {
            for (Token t : tokens) {
                sb.append(t.getText());
            }
        }
        return sb;
    }

    private Expression unwrap(Expression expr) {
        if (expr instanceof CompositeExpression) {
            CompositeExpression composite = (CompositeExpression) expr;
            if (composite.getExpressions().size() == 1) {
                // recursion should not be necessary;
                return composite.getExpressions().get(0);
            }
        }
        return expr;
    }

    private PredicateQuantifier toQuantifier(Token token) {
        PredicateQuantifier quantifier;
        if (token == null) {
            quantifier = PredicateQuantifier.ONE;
        } else {
            switch(token.getType()) {
                case JPQLSelectExpressionLexer.ANY:
                case JPQLSelectExpressionLexer.SOME:
                    quantifier = PredicateQuantifier.ANY;
                    break;
                case JPQLSelectExpressionLexer.ALL:
                    quantifier = PredicateQuantifier.ALL;
                    break;
                default:
                    quantifier = PredicateQuantifier.ONE;
            }
        }
        return quantifier;
    }
}
