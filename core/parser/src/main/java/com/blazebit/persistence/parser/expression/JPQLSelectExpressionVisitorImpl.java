/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.JPQLSelectExpressionBaseVisitor;
import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ArrayExpressionIntegerLiteralIndexContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ArrayExpressionSingleElementPathIndexContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ArrayExpressionStringLiteralIndexContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Boolean_literalContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ComparisonExpression_path_typeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ComparisonExpression_type_pathContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.DateLiteralContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Escape_characterContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.ExtendedJoinPathExpressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Functions_returning_datetimeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.General_path_elementContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.IdentifierContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.In_itemContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.IndexFunctionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Macro_expressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_arithmeticContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_booleanContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_datetimeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_entityContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_entitytypeContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.QuantifiedComparisonExpression_stringContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.SimpleJoinPathExpressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.SimplePathContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.SingleJoinElementExpressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.String_literalContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TimeLiteralContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TimestampLiteralContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TreatJoinPathExpressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TreatedRootPathContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Treated_key_value_expressionContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.Treated_subpathContext;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser.TrimFunctionContext;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.predicate.QuantifiableBinaryExpressionPredicate;
import com.blazebit.persistence.parser.util.TypeUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JPQLSelectExpressionVisitorImpl extends JPQLSelectExpressionBaseVisitor<Expression> {

    private final Set<String> aggregateFunctions;
    private final Map<String, Class<Enum<?>>> enums;
    private final Map<String, Class<?>> entities;
    private final int minEnumSegmentCount;
    private final int minEntitySegmentCount;
    private final Map<String, MacroFunction> macros;
    private final Set<String> usedMacros;

    public JPQLSelectExpressionVisitorImpl(Set<String> aggregateFunctions, Map<String, Class<Enum<?>>> enums, Map<String, Class<?>> entities, int minEnumSegmentCount, int minEntitySegmentCount, Map<String, MacroFunction> macros, Set<String> usedMacros) {
        this.aggregateFunctions = aggregateFunctions;
        this.enums = enums;
        this.entities = entities;
        this.minEnumSegmentCount = minEnumSegmentCount;
        this.minEntitySegmentCount = minEntitySegmentCount;
        this.macros = macros;
        this.usedMacros = usedMacros;
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
    public Expression visitMacro_expression(Macro_expressionContext ctx) {
        final String macroName = ctx.macroName.getText().toUpperCase();
        return visitMacroExpression(macroName, ctx);
    }

    public Expression visitMacroExpression(String macroName, ParserRuleContext ctx) {
        List<Expression> funcArgs = new ArrayList<Expression>(ctx.getChildCount());
        // Special handling of empty invocation, the position 2 contains an empty child node
        if (ctx.getChildCount() != 4 || !ctx.getChild(2).getText().isEmpty()) {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                if (!(ctx.getChild(i) instanceof TerminalNode)) {
                    funcArgs.add(ctx.getChild(i).accept(this));
                }
            }
        }

        MacroFunction macro = macros.get(macroName);
        if (macro == null) {
            throw new SyntaxErrorException("The macro '" + macroName + "' could not be found in the macro map!");
        }
        if (usedMacros != null) {
            usedMacros.add(macroName);
        }
        try {
            return macro.apply(funcArgs);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Could not apply the macro for the expression: " + ctx.getText(), ex);
        }
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
    public Expression visitSimple_subpath(JPQLSelectExpressionParser.Simple_subpathContext ctx) {
        List<PathElementExpression> pathElements = new ArrayList<PathElementExpression>();
        pathElements.add((PathElementExpression) ctx.general_path_start().accept(this));
        for (JPQLSelectExpressionParser.General_path_elementContext generalPathElem : ctx.general_path_element()) {
            pathElements.add((PathElementExpression) generalPathElem.accept(this));
        }
        return new PathExpression(pathElements);
    }

    @Override
    public Expression visitTreated_subpath(Treated_subpathContext ctx) {
        TreatExpression treatExpression = new TreatExpression(ctx.general_subpath().accept(this), ctx.subtype().getText());
        List<General_path_elementContext> followingPaths = ctx.general_path_element();
        Expression finalExpression = treatExpression;

        if (followingPaths.size() > 0) {
            List<PathElementExpression> pathProperties = new ArrayList<PathElementExpression>(followingPaths.size() + 1);
            PathExpression path = new PathExpression(pathProperties);
            pathProperties.add(treatExpression);

            for (int i = 0; i < followingPaths.size(); i++) {
                // TODO: Can here be arrays or is it just path elements?
                pathProperties.add((PathElementExpression) followingPaths.get(i).accept(this));
            }

            finalExpression = path;
        }

        return finalExpression;
    }

    @Override
    public Expression visitPath(JPQLSelectExpressionParser.PathContext ctx) {
        PathExpression result = wrapPath(ctx.general_subpath().accept(this));
        result.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));

        if (result.getExpressions().size() >= minEnumSegmentCount) {
            for (PathElementExpression element : result.getExpressions()) {
                if (!(element instanceof PropertyExpression)) {
                    return result;
                }
            }

            String literalStr = ctx.getText();
            Expression literalExpression = createEnumLiteral(literalStr);
            if (literalExpression != null) {
                return literalExpression;
            }
        } else if (result.getExpressions().size() >= minEntitySegmentCount || result.getExpressions().size() == 1) {
            for (PathElementExpression element : result.getExpressions()) {
                if (!(element instanceof PropertyExpression)) {
                    return result;
                }
            }

            String literalStr = ctx.getText();
            Expression literalExpression = createEntityTypeLiteral(literalStr);
            if (literalExpression != null) {
                return literalExpression;
            }
        }

        return result;
    }

    @Override
    public Expression visitLiteral(JPQLSelectExpressionParser.LiteralContext ctx) {
        JPQLSelectExpressionParser.Simple_literalContext literalContext = ctx.simple_literal();
        if (literalContext != null) {
            return literalContext.accept(this);
        }
        String literalStr = ctx.getText();
        if (ctx.pathElem.size() > minEnumSegmentCount) {
            Expression literalExpression = createEnumLiteral(literalStr);
            if (literalExpression != null) {
                return literalExpression;
            }
        }

        Expression literalExpression = createEntityTypeLiteral(literalStr);
        if (literalExpression != null) {
            return literalExpression;
        }

        throw new SyntaxErrorException("Could not interpret '" + literalStr + "' as enum or entity literal!");
    }

    @Override
    public Expression visitEntity_type_or_literal_expression(JPQLSelectExpressionParser.Entity_type_or_literal_expressionContext ctx) {
        JPQLSelectExpressionParser.Entity_type_expressionContext context = ctx.entity_type_expression();
        if (context != null) {
            return context.accept(this);
        }

        String literalStr = ctx.getText();
        Expression literalExpression = createEntityTypeLiteral(literalStr);
        if (literalExpression != null) {
            return literalExpression;
        }

        throw new SyntaxErrorException("Could not interpret '" + literalStr + "' as entity literal!");
    }

    @Override
    public Expression visitSingle_element_path_expression(JPQLSelectExpressionParser.Single_element_path_expressionContext ctx) {
        Expression entityLiteral = createEntityTypeLiteral(ctx.general_path_start().getText());
        if (entityLiteral != null) {
            return entityLiteral;
        }

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
        return new TypeFunctionExpression(ctx.type_discriminator_arg().accept(this));
    }

    @Override
    public Expression visitEntryFunction(JPQLSelectExpressionParser.EntryFunctionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        return new MapEntryExpression(collectionPath);
    }

    @Override
    public Expression visitKey_value_expression(JPQLSelectExpressionParser.Key_value_expressionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        if ("VALUE".equalsIgnoreCase(ctx.name.getText())) {
            return new MapValueExpression(collectionPath);
        } else {
            return new MapKeyExpression(collectionPath);
        }
    }

    @Override
    public Expression visitTreated_key_value_expression(Treated_key_value_expressionContext ctx) {
        return new TreatExpression(ctx.key_value_expression().accept(this), ctx.subtype().getText());
    }

    @Override
    public Expression visitOuterJoinPathExpression(JPQLSelectExpressionParser.OuterJoinPathExpressionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitMacroJoinPathExpression(JPQLSelectExpressionParser.MacroJoinPathExpressionContext ctx) {
        final String macroName = ctx.macroName.getText().toUpperCase();
        return visitMacroExpression(macroName, ctx);
    }

    @Override
    public Expression visitSimpleJoinPathExpression(SimpleJoinPathExpressionContext ctx) {
        PathExpression path = (PathExpression) ctx.simple_subpath().accept(this);
        path.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));
        return path;
    }

    @Override
    public Expression visitExtendedJoinPathExpression(ExtendedJoinPathExpressionContext ctx) {
        PathExpression path = wrapPath(ctx.treated_subpath().accept(this));
        path.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));
        return path;
    }

    @Override
    public Expression visitSingleJoinElementExpression(SingleJoinElementExpressionContext ctx) {
        return ctx.single_element_path_expression().accept(this);
    }

    @Override
    public Expression visitTreatJoinPathExpression(TreatJoinPathExpressionContext ctx) {
        return new TreatExpression(ctx.join_path_expression().accept(this), ctx.subtype().getText());
    }

    @Override
    public Expression visitSimplePath(SimplePathContext ctx) {
        PathExpression path = (PathExpression) ctx.simple_subpath().accept(this);
        path.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));
        return path;
    }

    @Override
    public Expression visitTreatedRootPath(TreatedRootPathContext ctx) {
        TreatExpression treatExpression = new TreatExpression(wrapPath(new PropertyExpression(ctx.identifier().getText())), ctx.subtype().getText());
        PathExpression path = (PathExpression) ctx.simple_subpath().accept(this);
        path.getExpressions().add(0, treatExpression);
        return path;
    }

    @Override
    public Expression visitIndexFunction(IndexFunctionContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.collection_valued_path_expression().accept(this);
        collectionPath.setCollectionKeyPath(true);
        return new ListIndexExpression(collectionPath);
    }

    @Override
    public Expression visitArrayExpressionParameterIndex(JPQLSelectExpressionParser.ArrayExpressionParameterIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), ctx.Input_parameter().accept(this));
    }

    @Override
    public Expression visitArrayExpressionPathIndex(JPQLSelectExpressionParser.ArrayExpressionPathIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), ctx.state_field_path_expression().accept(this));
    }

    @Override
    public Expression visitArrayExpressionSingleElementPathIndex(ArrayExpressionSingleElementPathIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), ctx.single_element_path_expression().accept(this));
    }

    @Override
    public Expression visitArrayExpressionIntegerLiteralIndex(ArrayExpressionIntegerLiteralIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), new NumericLiteral(ctx.Integer_literal().getText(), NumericType.INTEGER));
    }

    @Override
    public Expression visitArrayExpressionStringLiteralIndex(ArrayExpressionStringLiteralIndexContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), ctx.string_literal().accept(this));
    }

    @Override
    public Expression visitArithmeticExpressionPlusMinus(JPQLSelectExpressionParser.ArithmeticExpressionPlusMinusContext ctx) {
        ArithmeticOperator op = ArithmeticOperator.fromSymbol(ctx.op.getText());
        if (op == null) {
            throw new IllegalStateException("Unexpected arithmetic operator symbol [" + ctx.op.getText() + "]");
        }
        return new ArithmeticExpression(
                ctx.arithmetic_expression().accept(this),
                ctx.arithmetic_term().accept(this),
                op);
    }

    @Override
    public Expression visitArithmeticPrimaryParanthesis(JPQLSelectExpressionParser.ArithmeticPrimaryParanthesisContext ctx) {
        return ctx.arithmetic_expression().accept(this);
    }

    @Override
    public Expression visitArithmeticMultDiv(JPQLSelectExpressionParser.ArithmeticMultDivContext ctx) {
        ArithmeticOperator op = ArithmeticOperator.fromSymbol(ctx.op.getText());
        if (op == null) {
            throw new IllegalStateException("Unexpected arithmetic operator symbol [" + ctx.op.getText() + "]");
        }
        return new ArithmeticExpression(
                ctx.arithmetic_term().accept(this),
                ctx.arithmetic_factor().accept(this),
                op);
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
    public Expression visitConditionalTerm_and(JPQLSelectExpressionParser.ConditionalTerm_andContext ctx) {
        Predicate left = (Predicate) ctx.conditional_term().accept(this);
        if (left instanceof CompoundPredicate && ((CompoundPredicate) left).getOperator() == CompoundPredicate.BooleanOperator.AND) {
            ((CompoundPredicate) left).getChildren().add((Predicate) ctx.conditional_factor().accept(this));
            return left;
        } else {
            return new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, left, (Predicate) ctx.conditional_factor().accept(this));
        }
    }

    @Override
    public Expression visitConditionalExpression_or(JPQLSelectExpressionParser.ConditionalExpression_orContext ctx) {
        Predicate left = (Predicate) ctx.conditional_expression().accept(this);
        if (left instanceof CompoundPredicate && ((CompoundPredicate) left).getOperator() == CompoundPredicate.BooleanOperator.OR) {
            ((CompoundPredicate) left).getChildren().add((Predicate) ctx.conditional_term().accept(this));
            return left;
        } else {
            return new CompoundPredicate(CompoundPredicate.BooleanOperator.OR, left, (Predicate) ctx.conditional_term().accept(this));
        }
    }

    @Override
    public Expression visitConditionalPrimary(JPQLSelectExpressionParser.ConditionalPrimaryContext ctx) {
        return ctx.conditional_expression().accept(this);
    }

    @Override
    public Expression visitConditional_factor(JPQLSelectExpressionParser.Conditional_factorContext ctx) {
        Predicate predicate = (Predicate) ctx.conditional_primary().accept(this);

        if (ctx.not != null) {
            if (predicate.isNegated()) {
                // wrap in this case to maintain negational structure
                predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, predicate);
            }
            predicate.negate();
        }
        return predicate;
    }

    @Override
    public Expression visitArithmetic_factor(JPQLSelectExpressionParser.Arithmetic_factorContext ctx) {
        if (ctx.signum != null) {
            Expression expression = ctx.arithmetic_primary().accept(this);

            boolean invertSignum = "-".equals(ctx.signum.getText());
            return new ArithmeticFactor(expression, invertSignum);
        } else {
            return ctx.arithmetic_primary().accept(this);
        }
    }

    @Override
    public Expression visitNumeric_literal(JPQLSelectExpressionParser.Numeric_literalContext ctx) {
        NumericType numericType;
        String value;
        if (ctx.BigInteger_literal() != null) {
            numericType = NumericType.BIG_INTEGER;
            value = ctx.BigInteger_literal().getText();
        } else if (ctx.Integer_literal() != null) {
            numericType = NumericType.INTEGER;
            value = ctx.Integer_literal().getText();
        } else if (ctx.Long_literal() != null) {
            numericType = NumericType.LONG;
            value = ctx.Long_literal().getText();
        } else if (ctx.Float_literal() != null) {
            numericType = NumericType.FLOAT;
            value = ctx.Float_literal().getText();
        } else if (ctx.Double_literal() != null) {
            numericType = NumericType.DOUBLE;
            value = ctx.Double_literal().getText();
        } else if (ctx.BigDecimal_literal() != null) {
            numericType = NumericType.BIG_DECIMAL;
            value = ctx.BigDecimal_literal().getText();
        } else {
            throw new IllegalStateException("Could not find literal in context [" + ctx.getText() + "]");
        }
        return new NumericLiteral(value, numericType);
    }

    @Override
    public Expression visitBoolean_literal(Boolean_literalContext ctx) {
        return new BooleanLiteral(Boolean.parseBoolean(ctx.Boolean_literal().getText()));
    }

    @Override
    public Expression visitString_literal(String_literalContext ctx) {
        String literalValue = ctx.String_literal() == null ? ctx.Character_literal().getText() : ctx.String_literal().getText();
        // strip quotes
        return new StringLiteral(unquote(literalValue));
    }

    @Override
    public Expression visitDateLiteral(DateLiteralContext ctx) {
        return new DateLiteral(TypeUtils.DATE_CONVERTER.convert(extractTemporalValueString(ctx.Date_literal().getText())));
    }

    @Override
    public Expression visitTimeLiteral(TimeLiteralContext ctx) {
        return new TimeLiteral(TypeUtils.TIME_CONVERTER.convert(extractTemporalValueString(ctx.Time_literal().getText())));
    }

    @Override
    public Expression visitTimestampLiteral(TimestampLiteralContext ctx) {
        return new TimestampLiteral(TypeUtils.TIMESTAMP_CONVERTER.convert(extractTemporalValueString(ctx.Timestamp_literal().getText())));
    }

    private String extractTemporalValueString(String input) {
        int start = input.indexOf('\'') + 1;
        int end = input.lastIndexOf('\'');
        return input.substring(start, end);
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
    public Expression visitEscape_character(Escape_characterContext ctx) {
        if (ctx.Character_literal() != null) {
            return new StringLiteral(unquote(ctx.Character_literal().getText()));
        } else {
            return super.visitEscape_character(ctx);
        }
    }

    @Override
    public Expression visitGeneral_case_expression(JPQLSelectExpressionParser.General_case_expressionContext ctx) {
        List<WhenClauseExpression> whenClauses = new ArrayList<WhenClauseExpression>();
        for (JPQLSelectExpressionParser.When_clauseContext whenClause : ctx.when_clause()) {
            whenClauses.add((WhenClauseExpression) whenClause.accept(this));
        }
        JPQLSelectExpressionParser.Scalar_expressionContext elseExpressionCtx = ctx.scalar_expression();
        return new GeneralCaseExpression(whenClauses, elseExpressionCtx == null ? null : elseExpressionCtx.accept(this));
    }

    @Override
    public Expression visitSimple_case_expression(JPQLSelectExpressionParser.Simple_case_expressionContext ctx) {
        List<WhenClauseExpression> whenClauses = new ArrayList<WhenClauseExpression>();
        for (JPQLSelectExpressionParser.Simple_when_clauseContext whenClause : ctx.simple_when_clause()) {
            whenClauses.add((WhenClauseExpression) whenClause.accept(this));
        }
        JPQLSelectExpressionParser.Scalar_expressionContext elseExpressionCtx = ctx.scalar_expression();
        return new SimpleCaseExpression(ctx.case_operand().accept(this), whenClauses, elseExpressionCtx == null ? null : elseExpressionCtx.accept(this));
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
        InPredicate inPredicate;
        Expression left;
        if (ctx.left == null) {
            left = ctx.getChild(0).accept(this);
        } else {
            List<PathElementExpression> pathElems = new ArrayList<PathElementExpression>();
            pathElems.add(new PropertyExpression(ctx.left.getText()));
            left = new PathExpression(pathElems);
        }
        if (ctx.param == null && !ctx.in_item().isEmpty()) {
            List<Expression> inItems = new ArrayList<Expression>();
            for (In_itemContext inItemCtx : ctx.in_item()) {
                inItems.add(inItemCtx.accept(this));
            }
            inPredicate = new InPredicate(left, inItems);
        } else if (ctx.param != null) {
            ParameterExpression collectionParam = (ParameterExpression) new TerminalNodeImpl(ctx.param).accept(this);
            collectionParam.setCollectionValued(true);
            inPredicate = new InPredicate(left, collectionParam);
        } else {
//            List<PathElementExpression> pathElems = new ArrayList<PathElementExpression>();
//            pathElems.add(new PropertyExpression(ctx.right.getText()));
//            Expression inExpr = new PathExpression(pathElems);
            Expression inExpr = ctx.getChild(ctx.not == null ? 2 : 3).accept(this);
            inPredicate = new InPredicate(left, inExpr);
        }
        if (ctx.not != null) {
            inPredicate.setNegated(true);
        }
        return inPredicate;
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
                throw new IllegalStateException("Terminal node '" + node.getText() + "' not handled");
        }
    }

    @Override
    public Expression visitParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
        return super.visitParseSimpleExpression(ctx);
    }

    @Override
    public Expression visitParseSimpleSubqueryExpression(JPQLSelectExpressionParser.ParseSimpleSubqueryExpressionContext ctx) {
        return super.visitParseSimpleSubqueryExpression(ctx);
    }

    @Override
    public Expression visitParseOrderByClause(JPQLSelectExpressionParser.ParseOrderByClauseContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitComparisonExpression_path_type(ComparisonExpression_path_typeContext ctx) {
        BinaryExpressionPredicate pred = (EqPredicate) ctx.equality_comparison_operator().accept(this);
        String literalStr = ctx.left.getText();
        Expression expression = createEntityTypeLiteral(literalStr);
        if (expression == null) {
            throw new SyntaxErrorException("Could not interpret '" + literalStr + "' as entity literal!");
        }
        pred.setLeft(expression);
        pred.setRight(ctx.right.accept(this));
        return pred;
    }

    @Override
    public Expression visitComparisonExpression_type_path(ComparisonExpression_type_pathContext ctx) {
        BinaryExpressionPredicate pred = (EqPredicate) ctx.equality_comparison_operator().accept(this);
        pred.setLeft(ctx.left.accept(this));
        String literalStr = ctx.right.getText();
        Expression expression = createEntityTypeLiteral(literalStr);
        if (expression == null) {
            throw new SyntaxErrorException("Could not interpret '" + literalStr + "' as entity literal!");
        }
        pred.setRight(expression);
        return pred;
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
    public Expression visitIdentifier(IdentifierContext ctx) {
        Expression entityLiteral = createEntityTypeLiteral(ctx.Identifier().getText());
        if (entityLiteral != null) {
            return entityLiteral;
        }

        List<PathElementExpression> pathElems = new ArrayList<PathElementExpression>();
        pathElems.add(new PropertyExpression(ctx.Identifier().getText()));
        return new PathExpression(pathElems);
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
    protected Expression aggregateResult(Expression aggregate, Expression nextResult) {
        return aggregate == null ? nextResult : aggregate;
    }

    private String unquote(String literal) {
        return literal.substring(1, literal.length() - 1);
    }

    private PathExpression wrapPath(Expression expression) {
        if (expression instanceof PathExpression) {
            return (PathExpression) expression;
        }

        PathExpression p = new PathExpression();
        p.getExpressions().add((PathElementExpression) expression);
        return p;
    }

    private PredicateQuantifier toQuantifier(Token token) {
        PredicateQuantifier quantifier;
        if (token == null) {
            quantifier = PredicateQuantifier.ONE;
        } else {
            switch (token.getType()) {
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Expression createEnumLiteral(String enumStr) {
        int lastDotIdx = enumStr.lastIndexOf('.');
        String enumTypeStr = enumStr.substring(0, lastDotIdx);
        String enumValueStr = enumStr.substring(lastDotIdx + 1);
        Class<Enum<?>> enumType = enums.get(enumTypeStr);
        if (enumType == null) {
            return null;
        }
        return new EnumLiteral(Enum.valueOf((Class) enumType, enumValueStr), enumStr);
    }

    private Expression createEntityTypeLiteral(String entityLiteralStr) {
        Class<?> entityType = entities.get(entityLiteralStr);
        if (entityType == null) {
            return null;
        }
        return new EntityLiteral(entityType, entityLiteralStr);
    }
}
