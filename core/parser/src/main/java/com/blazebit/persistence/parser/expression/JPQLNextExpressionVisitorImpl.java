/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.parser.FunctionKind;
import com.blazebit.persistence.parser.JPQLNextLexer;
import com.blazebit.persistence.parser.JPQLNextParser;
import com.blazebit.persistence.parser.JPQLNextParserBaseVisitor;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
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
import com.blazebit.persistence.parser.util.TypeUtils;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JPQLNextExpressionVisitorImpl extends JPQLNextParserBaseVisitor<Expression> {

    private final Map<String, FunctionKind> functions;
    private final Map<String, Class<Enum<?>>> enums;
    private final Map<String, Class<Enum<?>>> enumsForLiterals;
    private final Map<String, Class<?>> entities;
    private final int minEnumSegmentCount;
    private final int minEntitySegmentCount;
    private final Map<String, MacroFunction> macros;
    private final Set<String> usedMacros;
    private final boolean allowOuter;
    private final boolean allowQuantifiedPredicates;
    private final boolean allowObjectExpression;
    private final CharStream input;

    public JPQLNextExpressionVisitorImpl(Map<String, FunctionKind> functions, Map<String, Class<Enum<?>>> enums, Map<String, Class<Enum<?>>> enumsForLiterals, Map<String, Class<?>> entities,
                                         int minEnumSegmentCount, int minEntitySegmentCount, Map<String, MacroFunction> macros, Set<String> usedMacros, boolean allowOuter, boolean allowQuantifiedPredicates, boolean allowObjectExpression, CharStream input) {
        this.functions = functions;
        this.enums = enums;
        this.enumsForLiterals = enumsForLiterals;
        this.entities = entities;
        this.minEnumSegmentCount = minEnumSegmentCount;
        this.minEntitySegmentCount = minEntitySegmentCount;
        this.macros = macros;
        this.usedMacros = usedMacros;
        this.allowOuter = allowOuter;
        this.allowQuantifiedPredicates = allowQuantifiedPredicates;
        this.allowObjectExpression = allowObjectExpression;
        this.input = input;
    }

    @Override
    public Expression visitParseSelectExpression(JPQLNextParser.ParseSelectExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitParsePathExpression(JPQLNextParser.ParsePathExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitParseExpression(JPQLNextParser.ParseExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitParseInItemExpression(JPQLNextParser.ParseInItemExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Predicate visitParsePredicate(JPQLNextParser.ParsePredicateContext ctx) {
        return (Predicate) ctx.getChild(0).accept(this);
    }

    @Override
    public Expression visitGroupedExpression(JPQLNextParser.GroupedExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitConcatenationExpression(JPQLNextParser.ConcatenationExpressionContext ctx) {
        Expression left = ctx.lhs.accept(this);
        Expression right = ctx.rhs.accept(this);
        if (left instanceof FunctionExpression && "CONCAT".equalsIgnoreCase(((FunctionExpression) left).getFunctionName())) {
            ((FunctionExpression) left).getExpressions().add(right);
            return left;
        } else if (right instanceof FunctionExpression && "CONCAT".equalsIgnoreCase(((FunctionExpression) right).getFunctionName())) {
            ((FunctionExpression) right).getExpressions().add(0, left);
            return right;
        } else {
            List<Expression> args = new ArrayList<>(2);
            args.add(left);
            args.add(right);
            return new FunctionExpression("CONCAT", args);
        }
    }

    @Override
    public Expression visitAdditionExpression(JPQLNextParser.AdditionExpressionContext ctx) {
        return new ArithmeticExpression(ctx.lhs.accept(this), ctx.rhs.accept(this), ArithmeticOperator.ADDITION);
    }

    @Override
    public Expression visitSubtractionExpression(JPQLNextParser.SubtractionExpressionContext ctx) {
        return new ArithmeticExpression(ctx.lhs.accept(this), ctx.rhs.accept(this), ArithmeticOperator.SUBTRACTION);
    }

    @Override
    public Expression visitMultiplicationExpression(JPQLNextParser.MultiplicationExpressionContext ctx) {
        return new ArithmeticExpression(ctx.lhs.accept(this), ctx.rhs.accept(this), ArithmeticOperator.MULTIPLICATION);
    }

    @Override
    public Expression visitDivisionExpression(JPQLNextParser.DivisionExpressionContext ctx) {
        return new ArithmeticExpression(ctx.lhs.accept(this), ctx.rhs.accept(this), ArithmeticOperator.DIVISION);
    }

    @Override
    public Expression visitModuloExpression(JPQLNextParser.ModuloExpressionContext ctx) {
        List<Expression> args = new ArrayList<>(2);
        args.add(ctx.lhs.accept(this));
        args.add(ctx.rhs.accept(this));
        return new FunctionExpression("MOD", args);
    }

    @Override
    public Expression visitUnaryMinusExpression(JPQLNextParser.UnaryMinusExpressionContext ctx) {
        return new ArithmeticFactor(ctx.expression().accept(this), true);
    }

    @Override
    public Expression visitUnaryPlusExpression(JPQLNextParser.UnaryPlusExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitSimpleCaseExpression(JPQLNextParser.SimpleCaseExpressionContext ctx) {
        List<JPQLNextParser.SimpleCaseWhenContext> simpleCaseWhenContexts = ctx.simpleCaseWhen();
        List<WhenClauseExpression> whenClauses = new ArrayList<>(simpleCaseWhenContexts.size());
        for (int i = 0; i < simpleCaseWhenContexts.size(); i++) {
            whenClauses.add(visitSimpleCaseWhen(simpleCaseWhenContexts.get(i)));
        }
        return new SimpleCaseExpression(ctx.operand.accept(this), whenClauses, ctx.otherwise == null ? null : ctx.otherwise.accept(this));
    }

    @Override
    public Expression visitGeneralCaseExpression(JPQLNextParser.GeneralCaseExpressionContext ctx) {
        List<JPQLNextParser.SearchedCaseWhenContext> searchedCaseWhenContexts = ctx.searchedCaseWhen();
        List<WhenClauseExpression> whenClauses = new ArrayList<>(searchedCaseWhenContexts.size());
        for (int i = 0; i < searchedCaseWhenContexts.size(); i++) {
            whenClauses.add(visitSearchedCaseWhen(searchedCaseWhenContexts.get(i)));
        }
        JPQLNextParser.ExpressionContext elseExpressionCtx = ctx.expression();
        return new GeneralCaseExpression(whenClauses, elseExpressionCtx == null ? null : elseExpressionCtx.accept(this));
    }

    @Override
    public WhenClauseExpression visitSimpleCaseWhen(JPQLNextParser.SimpleCaseWhenContext ctx) {
        return handleWhenClause(ctx.when, ctx.then);
    }

    @Override
    public WhenClauseExpression visitSearchedCaseWhen(JPQLNextParser.SearchedCaseWhenContext ctx) {
        return handleWhenClause(ctx.predicate(), ctx.expression());
    }

    private WhenClauseExpression handleWhenClause(ParserRuleContext condition, ParserRuleContext result) {
        return new WhenClauseExpression(condition.accept(this), result.accept(this));
    }

    @Override
    public Expression visitTimestampLiteral(JPQLNextParser.TimestampLiteralContext ctx) {
        return new TimestampLiteral(TypeUtils.TIMESTAMP_CONVERTER.convert(ctx.dateTimeLiteralText().getText()));
    }

    @Override
    public Expression visitDateLiteral(JPQLNextParser.DateLiteralContext ctx) {
        return new DateLiteral(TypeUtils.DATE_CONVERTER.convert(ctx.dateTimeLiteralText().getText()));
    }

    @Override
    public Expression visitTimeLiteral(JPQLNextParser.TimeLiteralContext ctx) {
        return new TimeLiteral(TypeUtils.TIME_CONVERTER.convert(ctx.dateTimeLiteralText().getText()));
    }

    @Override
    public Expression visitNamedParameter(JPQLNextParser.NamedParameterContext ctx) {
        return new ParameterExpression(ctx.identifier().getText());
    }

    @Override
    public Expression visitPositionalParameter(JPQLNextParser.PositionalParameterContext ctx) {
        return new ParameterExpression(ctx.INTEGER_LITERAL().getText());
    }

    @Override
    public Expression visitEntityType(JPQLNextParser.EntityTypeContext ctx) {
        Expression expression;
        JPQLNextParser.ParameterContext parameter = ctx.parameter();
        if (parameter == null) {
            expression = visitPath(ctx.path());
        } else {
            expression = parameter.accept(this);
        }
        return new TypeFunctionExpression(expression);
    }

    @Override
    public Expression visitEntityTypeOrEnumLiteral(JPQLNextParser.EntityTypeOrEnumLiteralContext ctx) {
        String literalStr = ctx.getText();
        Expression literalExpression = createEnumLiteral(literalStr);
        if (literalExpression != null) {
            return literalExpression;
        }

        literalExpression = createEntityTypeLiteral(literalStr);
        if (literalExpression != null) {
            return literalExpression;
        }

        throw new SyntaxErrorException("Invalid literal: " + literalStr);
    }

    @Override
    public Expression visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == JPQLNextLexer.EOF) {
            return null;
        }
        switch (node.getSymbol().getType()) {
            case JPQLNextLexer.NULL:
                return NullExpression.INSTANCE;
            case JPQLNextLexer.STRING_LITERAL:
            case JPQLNextLexer.CHARACTER_LITERAL:
                return new StringLiteral(node.getText());
            case JPQLNextLexer.TRUE:
                return new BooleanLiteral(true);
            case JPQLNextLexer.FALSE:
                return new BooleanLiteral(false);
            case JPQLNextLexer.BIG_INTEGER_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.BIG_INTEGER);
            case JPQLNextLexer.INTEGER_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.INTEGER);
            case JPQLNextLexer.LONG_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.LONG);
            case JPQLNextLexer.FLOAT_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.FLOAT);
            case JPQLNextLexer.DOUBLE_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.DOUBLE);
            case JPQLNextLexer.BIG_DECIMAL_LITERAL:
                return new NumericLiteral(node.getText(), NumericType.BIG_DECIMAL);
            default:
                throw new IllegalStateException("Terminal node '" + node.getText() + "' not handled");
        }
    }

    @Override
    public Expression visitTrimFunction(JPQLNextParser.TrimFunctionContext ctx) {
        Trimspec trimspec;
        JPQLNextParser.TrimSpecificationContext trimSpecificationContext = ctx.trimSpecification();
        if (trimSpecificationContext != null) {
            trimspec = Trimspec.valueOf(trimSpecificationContext.getText().toUpperCase());
        } else {
            trimspec = Trimspec.BOTH;
        }

        Expression trimCharacter = null;
        JPQLNextParser.TrimCharacterContext trimCharacterContext = ctx.trimCharacter();
        if (trimCharacterContext != null) {
            trimCharacter = trimCharacterContext.accept(this);
        }

        return new TrimExpression(trimspec, trimCharacter, ctx.expression().accept(this));
    }

    @Override
    public Expression visitTemporalFunction(JPQLNextParser.TemporalFunctionContext ctx) {
        return new FunctionExpression(ctx.name.getText(), Collections.<Expression>emptyList());
    }

    @Override
    public Expression visitTemporalFunctionExpression(JPQLNextParser.TemporalFunctionExpressionContext ctx) {
        return new FunctionExpression(ctx.name.getText(), Collections.<Expression>emptyList());
    }

    @Override
    public Expression visitCountFunction(JPQLNextParser.CountFunctionContext ctx) {
        boolean distinct = ctx.DISTINCT() != null;
        List<Expression> arguments;
        if (ctx.ASTERISK() == null) {
            arguments = new ArrayList<>(1);
            arguments.add(ctx.expression().accept(this));
        } else {
            arguments = Collections.emptyList();
        }
        return handleFunction("COUNT", distinct, arguments, ctx, ctx.whereClause(), ctx.windowName, ctx.windowDefinition());
    }

    @Override
    public Expression visitGenericFunctionInvocation(JPQLNextParser.GenericFunctionInvocationContext ctx) {
        boolean distinct = ctx.DISTINCT() != null;
        List<JPQLNextParser.ExpressionContext> expressions = ctx.expression();
        int size = expressions.size();
        List<Expression> arguments = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arguments.add(expressions.get(i).accept(this));
        }
        return handleFunction(ctx.name.getText(), distinct, arguments, ctx, ctx.whereClause(), ctx.windowName, ctx.windowDefinition());
    }

    private Expression handleFunction(String name, boolean distinct, List<Expression> arguments, ParserRuleContext ctx, JPQLNextParser.WhereClauseContext whereClauseContext, JPQLNextParser.IdentifierContext windowName, JPQLNextParser.WindowDefinitionContext windowDefinitionContext) {
        String lowerName = name.toLowerCase();
        FunctionKind functionKind;
        // Builtin functions
        switch (lowerName) {
            //CHECKSTYLE:OFF: FallThrough
            case "current_date":
            case "current_time":
            case "current_timestamp":
                failDistinct(distinct, ctx);
                return new FunctionExpression(name, Collections.<Expression>emptyList());
            case "outer":
                if (!allowOuter) {
                    throw new SyntaxErrorException("Invalid disallowed use of OUTER in: " + getInputText(ctx));
                }
            case "concat":
            case "substring":
            case "lower":
            case "upper":
            case "length":
            case "locate":
            case "abs":
            case "sqrt":
            case "mod":
            case "coalesce":
            case "nullif":
                failDistinct(distinct, ctx);
                return new FunctionExpression(name, arguments);
            case "trim":
                failDistinct(distinct, ctx);
                return new TrimExpression(Trimspec.BOTH, null, arguments.get(0));
            case "size":
                failDistinct(distinct, ctx);
                ((PathExpression) arguments.get(0)).setUsedInCollectionFunction(true);
                return new FunctionExpression(name, arguments);
            case "index":
                failDistinct(distinct, ctx);
                PathExpression listIndexPath = (PathExpression) arguments.get(0);
                listIndexPath.setCollectionQualifiedPath(true);
                return new ListIndexExpression(listIndexPath);
            case "key":
                failDistinct(distinct, ctx);
                PathExpression mapKeyPath = (PathExpression) arguments.get(0);
                mapKeyPath.setCollectionQualifiedPath(true);
                return new MapKeyExpression(mapKeyPath);
            case "value":
                failDistinct(distinct, ctx);
                PathExpression mapValuePath = (PathExpression) arguments.get(0);
                mapValuePath.setCollectionQualifiedPath(true);
                return new MapValueExpression(mapValuePath);
            case "entry":
                failDistinct(distinct, ctx);
                PathExpression mapEntryPath = (PathExpression) arguments.get(0);
                mapEntryPath.setCollectionQualifiedPath(true);
                return new MapEntryExpression(mapEntryPath);
            case "type":
                failDistinct(distinct, ctx);
                return new TypeFunctionExpression(arguments.get(0));
            case "function":
                failDistinct(distinct, ctx);
                String functionName = ((StringLiteral) arguments.get(0)).getValue();
                functionKind = functions.get(functionName.toLowerCase());
                if (functionKind == null) {
                    // We pass through the function syntax to the JPA provider
                    functionKind = FunctionKind.DETERMINISTIC;
                }
                break;
            default:
                functionKind = functions.get(lowerName);
                break;
            //CHECKSTYLE:ON: FallThrough
        }
        if (functionKind == null) {
            if (whereClauseContext == null && windowName == null && windowDefinitionContext == null) {
                failDistinct(distinct, ctx);
                return handleMacro(name, arguments, ctx);
            }
            throw new SyntaxErrorException("No function with the name '" + name + "' exists!");
        }
        if (functionKind == FunctionKind.AGGREGATE) {
            // NOTE: We currently don't support JUST filtering for aggregate functions, but maybe in the future
            if (windowName == null && windowDefinitionContext == null) {
                if (whereClauseContext == null) {
                    return new AggregateExpression(distinct, name, arguments);
                } else {
                    return new AggregateExpression(distinct, "window_" + name, arguments, (Predicate) whereClauseContext.predicate().accept(this));
                }
            } else {
                failDistinct(distinct, ctx);
                return new FunctionExpression("window_" + name, arguments, createWindowDefinition(whereClauseContext, windowName, windowDefinitionContext, functionKind));
            }
        } else {
            failDistinct(distinct, ctx);
            return new FunctionExpression(name, arguments, createWindowDefinition(whereClauseContext, windowName, windowDefinitionContext, functionKind));
        }
    }

    private Expression handleMacro(String name, List<Expression> arguments, ParserRuleContext ctx) {
        String macroName = name.toUpperCase();
        MacroFunction macro = macros.get(macroName);
        if (macro == null) {
            throw new SyntaxErrorException("No function or macro with the name '" + name + "' could not be found!");
        }
        if (usedMacros != null) {
            usedMacros.add(macroName);
        }
        try {
            return macro.apply(arguments);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Could not apply the macro for the expression: " + getInputText(ctx), ex);
        }
    }

    private void failDistinct(boolean distinct, ParserRuleContext ctx) {
        if (distinct) {
            throw new SyntaxErrorException("Invalid use of DISTINCT for function: " + getInputText(ctx));
        }
    }

    private WindowDefinition createWindowDefinition(JPQLNextParser.WhereClauseContext whereClauseContext, JPQLNextParser.IdentifierContext windowNameIdentifier, JPQLNextParser.WindowDefinitionContext windowDefinitionContext, FunctionKind functionKind) {
        Predicate filterPredicate = null;
        if (whereClauseContext != null) {
            filterPredicate = (Predicate) whereClauseContext.predicate().accept(this);
        }

        String windowName = null;
        if (windowNameIdentifier != null) {
            windowName = windowNameIdentifier.getText();
        }

        if (windowDefinitionContext == null) {
            if (windowName != null || filterPredicate != null || functionKind == FunctionKind.WINDOW) {
                return new WindowDefinition(windowName, filterPredicate);
            }
            return null;
        } else {
            JPQLNextParser.IdentifierContext identifierContext = windowDefinitionContext.identifier();
            if (identifierContext != null) {
                windowName = identifierContext.getText();
            }
            List<Expression> partitionExpressions;
            JPQLNextParser.PartitionByClauseContext partitionByClauseContext = windowDefinitionContext.partitionByClause();
            if (partitionByClauseContext == null) {
                partitionExpressions = Collections.emptyList();
            } else {
                List<JPQLNextParser.GroupingValueContext> groupingValueContexts = partitionByClauseContext.groupingValue();
                int size = groupingValueContexts.size();
                partitionExpressions = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    partitionExpressions.add(groupingValueContexts.get(i).accept(this));
                }
            }

            List<OrderByItem> orderByExpressions = createOrderByItems(windowDefinitionContext.orderByClause());

            WindowFrameMode frameMode = null;
            WindowFramePositionType frameStartType = null;
            WindowFramePositionType frameEndType = null;
            Expression frameStartExpression = null;
            Expression frameEndExpression = null;
            WindowFrameExclusionType frameExclusionType = null;
            JPQLNextParser.FrameClauseContext frameClauseContext = windowDefinitionContext.frameClause();
            if (frameClauseContext != null) {
                frameMode = WindowFrameMode.valueOf(frameClauseContext.frameMode.getText().toUpperCase());
                JPQLNextParser.FrameStartContext frameStartContext = frameClauseContext.frameStart();
                JPQLNextParser.ParameterOrNumberLiteralContext parameterOrNumberLiteralContext = frameStartContext.parameterOrNumberLiteral();
                if (parameterOrNumberLiteralContext != null) {
                    frameStartExpression = parameterOrNumberLiteralContext.accept(this);
                    frameStartType = frameStartContext.PRECEDING() != null ? WindowFramePositionType.BOUNDED_PRECEDING : WindowFramePositionType.BOUNDED_FOLLOWING;
                } else if (frameStartContext.CURRENT() != null) {
                    frameStartType = WindowFramePositionType.CURRENT_ROW;
                } else if (frameStartContext.PRECEDING() != null) {
                    frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
                } else {
                    throw new IllegalStateException("Unexpected state!");
                }
                JPQLNextParser.FrameEndContext frameEndContext = frameClauseContext.frameEnd();
                if (frameEndContext != null) {
                    parameterOrNumberLiteralContext = frameEndContext.parameterOrNumberLiteral();
                    if (parameterOrNumberLiteralContext != null) {
                        frameEndExpression = parameterOrNumberLiteralContext.accept(this);
                        frameEndType = frameEndContext.PRECEDING() != null ? WindowFramePositionType.BOUNDED_PRECEDING : WindowFramePositionType.BOUNDED_FOLLOWING;
                    } else if (frameEndContext.CURRENT() != null) {
                        frameEndType = WindowFramePositionType.CURRENT_ROW;
                    } else if (frameEndContext.FOLLOWING() != null) {
                        frameEndType = WindowFramePositionType.UNBOUNDED_FOLLOWING;
                    } else {
                        throw new IllegalStateException("Unexpected state!");
                    }
                }
                JPQLNextParser.FrameExclusionClauseContext frameExclusionContext = frameClauseContext.frameExclusionClause();
                if (frameExclusionContext != null) {
                    if (frameExclusionContext.CURRENT() != null) {
                        frameExclusionType = WindowFrameExclusionType.EXCLUDE_CURRENT_ROW;
                    } else if (frameExclusionContext.GROUP() != null) {
                        frameExclusionType = WindowFrameExclusionType.EXCLUDE_GROUP;
                    } else if (frameExclusionContext.NO() != null) {
                        frameExclusionType = WindowFrameExclusionType.EXCLUDE_NO_OTHERS;
                    } else if (frameExclusionContext.TIES() != null) {
                        frameExclusionType = WindowFrameExclusionType.EXCLUDE_TIES;
                    } else {
                        throw new IllegalStateException("Unexpected state!");
                    }
                }
            }

            return new WindowDefinition(
                    windowName,
                    partitionExpressions,
                    orderByExpressions,
                    filterPredicate,
                    frameMode,
                    frameStartType,
                    frameStartExpression,
                    frameEndType,
                    frameEndExpression,
                    frameExclusionType
            );
        }
    }

    private List<OrderByItem> createOrderByItems(JPQLNextParser.OrderByClauseContext ctx) {
        List<OrderByItem> orderByExpressions;
        if (ctx == null) {
            orderByExpressions = Collections.emptyList();
        } else {
            List<JPQLNextParser.OrderByItemContext> orderByItemContexts = ctx.orderByItem();
            int size = orderByItemContexts.size();
            orderByExpressions = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                orderByExpressions.add(createOrderByItem(orderByItemContexts.get(i)));
            }
        }
        return orderByExpressions;
    }

    private OrderByItem createOrderByItem(JPQLNextParser.OrderByItemContext ctx) {
        Expression expression = ctx.expression().accept(this);
        boolean asc = true;
        boolean nullsFirst = true;
        if (ctx.STRING_LITERAL() != null) {
            throw new SyntaxErrorException("Collations are not yet supported: " + getInputText(ctx));
        }
        if (ctx.DESC() != null) {
            asc = false;
        }
        if (ctx.FIRST() == null) {
            nullsFirst = false;
        }
        return new OrderByItem(asc, nullsFirst, expression);
    }

    @Override
    public Expression visitPathExpression(JPQLNextParser.PathExpressionContext ctx) {
        Expression expression = visitPath(ctx.path());
        if (!allowObjectExpression) {
            if (expression instanceof PathExpression) {
                List<PathElementExpression> expressions = ((PathExpression) expression).getExpressions();
                if (expressions.size() == 1 && expressions.get(0) instanceof TreatExpression) {
                    throw new SyntaxErrorException("A top level treat expression is not allowed. Consider to further dereference the expression: " + getInputText(ctx));
                }
            } else if (expression instanceof TreatExpression) {
                throw new SyntaxErrorException("A top level treat expression is not allowed. Consider to further dereference the expression: " + getInputText(ctx));
            }
        }
        return expression;
    }

    @Override
    public Expression visitTreatPath(JPQLNextParser.TreatPathContext ctx) {
        Expression expression = visitPath(ctx.path());
        if (expression instanceof PathElementExpression) {
            List<PathElementExpression> pathElements = new ArrayList<>(1);
            pathElements.add((PathElementExpression) expression);
            expression = new PathExpression(pathElements);
        }
        return new TreatExpression(expression, ctx.entityName().getText());
    }

    @Override
    public Expression visitObjectSelectExpression(JPQLNextParser.ObjectSelectExpressionContext ctx) {
        return ctx.identifier().accept(this);
    }

    @Override
    public Expression visitValuePath(JPQLNextParser.ValuePathContext ctx) {
        return new MapValueExpression((PathExpression) visitPath(ctx.path()));
    }

    @Override
    public Expression visitMapKeyPath(JPQLNextParser.MapKeyPathContext ctx) {
        PathExpression collectionPath = (PathExpression) visitPath(ctx.path());
        collectionPath.setCollectionQualifiedPath(true);
        return new MapKeyExpression(collectionPath);
    }

    @Override
    public Expression visitMapEntrySelectExpression(JPQLNextParser.MapEntrySelectExpressionContext ctx) {
        return new MapEntryExpression((PathExpression) visitPath(ctx.path()));
    }

    @Override
    public Expression visitConstructorExpression(JPQLNextParser.ConstructorExpressionContext ctx) {
        List<JPQLNextParser.ConstructorItemContext> constructorItemContexts = ctx.constructorItem();
        int size = constructorItemContexts.size();
        List<Expression> arguments = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arguments.add(constructorItemContexts.get(i).accept(this));
        }

        return new FunctionExpression("NEW " + ctx.simpleSubpath().getText(), arguments);
    }

    @Override
    public Expression visitPath(JPQLNextParser.PathContext ctx) {
        JPQLNextParser.QualifiedPathContext qualifiedPathContext = ctx.qualifiedPath();
        JPQLNextParser.GeneralSubpathContext generalSubpathContext = ctx.generalSubpath();
        if (qualifiedPathContext == null) {
            return visitGeneralSubpath(generalSubpathContext, null);
        }
        if (generalSubpathContext == null) {
            return qualifiedPathContext.accept(this);
        }
        return visitGeneralSubpath(generalSubpathContext, (PathElementExpression) qualifiedPathContext.accept(this));
    }

    @Override
    public Expression visitMacroPath(JPQLNextParser.MacroPathContext ctx) {
        List<JPQLNextParser.ExpressionContext> expressions = ctx.expression();
        int size = expressions.size();
        List<Expression> arguments = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arguments.add(expressions.get(i).accept(this));
        }
        return handleMacro(ctx.identifier().getText(), arguments, ctx);
    }

    @Override
    public Expression visitOuterPath(JPQLNextParser.OuterPathContext ctx) {
        List<Expression> arguments = new ArrayList<>(1);
        JPQLNextParser.SimpleSubpathContext simpleSubpathContext = ctx.simpleSubpath();
        arguments.add(simpleSubpathContext == null ? ctx.macroPath().accept(this) : simpleSubpathContext.accept(this));
        return new FunctionExpression("OUTER", arguments);
    }

    @Override
    public Expression visitGeneralSubpath(JPQLNextParser.GeneralSubpathContext ctx) {
        return visitGeneralSubpath(ctx, null);
    }

    @Override
    public Expression visitSimpleSubpath(JPQLNextParser.SimpleSubpathContext ctx) {
        List<JPQLNextParser.IdentifierContext> identifierContexts = ctx.identifier();
        int size = identifierContexts.size();

        // handle entity and enum literals
        if (size >= minEnumSegmentCount) {
            String literalStr = ctx.getText();
            Expression literalExpression = createEnumLiteral(literalStr);
            if (literalExpression != null) {
                return literalExpression;
            }
        } else if (size >= minEntitySegmentCount || size == 1) {
            String literalStr = ctx.getText();
            Expression literalExpression = createEntityTypeLiteral(literalStr);
            if (literalExpression != null) {
                return literalExpression;
            }
        }

        ArrayList<PathElementExpression> pathElementExpressions = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            pathElementExpressions.add(new PropertyExpression(identifierContexts.get(i).getText()));
        }
        return new PathExpression(pathElementExpressions);
    }

    public Expression visitGeneralSubpath(JPQLNextParser.GeneralSubpathContext ctx, PathElementExpression initialPathElement) {
        List<JPQLNextParser.IdentifierContext> identifierContexts = ctx.simpleSubpath().identifier();
        JPQLNextParser.PredicateOrExpressionContext expression = ctx.predicateOrExpression();

        boolean literalPossible = true;
        int size = identifierContexts.size();
        int initialSize = size + (initialPathElement == null ? 0 : 1);
        ArrayList<PathElementExpression> pathElementExpressions = new ArrayList<>(initialSize);
        PathExpression pathExpression = new PathExpression(pathElementExpressions);
        if (initialPathElement != null) {
            pathElementExpressions.add(initialPathElement);
        }

        do {
            boolean processPathElements = true;
            // handle entity and enum literals
            if (literalPossible) {
                if (size >= minEnumSegmentCount) {
                    String literalStr = ctx.simpleSubpath().getText();
                    Expression literalExpression = createEnumLiteral(literalStr);
                    if (literalExpression != null) {
                        return literalExpression;
                    }
                } else if (size >= minEntitySegmentCount || size == 1) {
                    String literalStr = ctx.simpleSubpath().getText();
                    Expression literalExpression = createEntityTypeLiteral(literalStr);
                    if (literalExpression != null) {
                        if (expression == null) {
                            return literalExpression;
                        } else {
                            pathElementExpressions.add(new ArrayExpression(literalExpression, expression.accept(this)));
                            processPathElements = false;
                        }
                    }
                }
            }

            if (processPathElements) {
                for (int i = 0; i < size; i++) {
                    pathElementExpressions.add(new PropertyExpression(identifierContexts.get(i).getText()));
                }
                if (expression != null) {
                    int index = initialSize - 1;
                    pathElementExpressions.set(index, new ArrayExpression(pathElementExpressions.get(index), expression.accept(this)));
                }
            }
            ctx = ctx.generalSubpath();
            if (ctx == null) {
                break;
            }

            literalPossible = false;
            identifierContexts = ctx.simpleSubpath().identifier();
            expression = ctx.predicateOrExpression();

            size = identifierContexts.size();
            initialSize += size;
            pathElementExpressions.ensureCapacity(initialSize);
        } while (true);

        return pathExpression;
    }

    @Override
    public Expression visitGroupedPredicate(JPQLNextParser.GroupedPredicateContext ctx) {
        return ctx.predicate().accept(this);
    }

    @Override
    public Expression visitOrPredicate(JPQLNextParser.OrPredicateContext ctx) {
        List<JPQLNextParser.PredicateContext> predicate = ctx.predicate();
        Predicate left = (Predicate) predicate.get(0).accept(this);
        if (left instanceof CompoundPredicate && ((CompoundPredicate) left).getOperator() == CompoundPredicate.BooleanOperator.OR) {
            ((CompoundPredicate) left).getChildren().add((Predicate) predicate.get(1).accept(this));
            return left;
        } else {
            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(left);
            predicates.add((Predicate) predicate.get(1).accept(this));
            return new CompoundPredicate(CompoundPredicate.BooleanOperator.OR, predicates);
        }
    }

    @Override
    public Expression visitAndPredicate(JPQLNextParser.AndPredicateContext ctx) {
        List<JPQLNextParser.PredicateContext> predicate = ctx.predicate();
        Predicate left = (Predicate) predicate.get(0).accept(this);
        if (left instanceof CompoundPredicate && ((CompoundPredicate) left).getOperator() == CompoundPredicate.BooleanOperator.AND) {
            ((CompoundPredicate) left).getChildren().add((Predicate) predicate.get(1).accept(this));
            return left;
        } else {
            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(left);
            predicates.add((Predicate) predicate.get(1).accept(this));
            return new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, predicates);
        }
    }

    @Override
    public Expression visitNegatedPredicate(JPQLNextParser.NegatedPredicateContext ctx) {
        Predicate predicate = (Predicate) ctx.predicate().accept(this);
        if (ctx.NOT() != null) {
            if (predicate.isNegated()) {
                // wrap in this case to maintain negational structure
                predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, predicate);
            }
            predicate.negate();
        }
        return predicate;
    }

    @Override
    public Expression visitExistsSimplePredicate(JPQLNextParser.ExistsSimplePredicateContext ctx) {
        return new ExistsPredicate(ctx.identifier().accept(this), ctx.NOT() != null);
    }

    @Override
    public Expression visitIsNullPredicate(JPQLNextParser.IsNullPredicateContext ctx) {
        return new IsNullPredicate(ctx.expression().accept(this), ctx.NOT() != null);
    }

    @Override
    public Expression visitIsEmptyPredicate(JPQLNextParser.IsEmptyPredicateContext ctx) {
        PathExpression collectionPath = (PathExpression) ctx.expression().accept(this);
        collectionPath.setUsedInCollectionFunction(true);
        return new IsEmptyPredicate(collectionPath, ctx.NOT() != null);
    }

    @Override
    public Expression visitQuantifiedSimpleLessThanOrEqualPredicate(JPQLNextParser.QuantifiedSimpleLessThanOrEqualPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new LePredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), false);
    }

    @Override
    public Expression visitQuantifiedSimpleGreaterThanPredicate(JPQLNextParser.QuantifiedSimpleGreaterThanPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new GtPredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), false);
    }

    @Override
    public Expression visitQuantifiedSimpleInequalityPredicate(JPQLNextParser.QuantifiedSimpleInequalityPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new EqPredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), true);
    }

    @Override
    public Expression visitQuantifiedSimpleLessThanPredicate(JPQLNextParser.QuantifiedSimpleLessThanPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new LtPredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), false);
    }

    @Override
    public Expression visitQuantifiedSimpleEqualityPredicate(JPQLNextParser.QuantifiedSimpleEqualityPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new EqPredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), false);
    }

    @Override
    public Expression visitQuantifiedSimpleGreaterThanOrEqualPredicate(JPQLNextParser.QuantifiedSimpleGreaterThanOrEqualPredicateContext ctx) {
        failQuantified(ctx, ctx.quantifier);
        return new GePredicate(ctx.expression().accept(this), ctx.identifier().accept(this), toQuantifier(ctx.quantifier), false);
    }

    private void failQuantified(ParserRuleContext ctx, Token qualifier) {
        if (qualifier != null && !allowQuantifiedPredicates) {
            throw new SyntaxErrorException("The use of quantifiers is not allowed in the context of the expression: " + getInputText(ctx));
        }
    }

    @Override
    public Expression visitInequalityPredicate(JPQLNextParser.InequalityPredicateContext ctx) {
        return new EqPredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, true);
    }

    @Override
    public Expression visitLessThanOrEqualPredicate(JPQLNextParser.LessThanOrEqualPredicateContext ctx) {
        return new LePredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, false);
    }

    @Override
    public Expression visitEqualityPredicate(JPQLNextParser.EqualityPredicateContext ctx) {
        return new EqPredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, false);
    }

    @Override
    public Expression visitGreaterThanPredicate(JPQLNextParser.GreaterThanPredicateContext ctx) {
        return new GtPredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, false);
    }

    @Override
    public Expression visitLessThanPredicate(JPQLNextParser.LessThanPredicateContext ctx) {
        return new LtPredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, false);
    }

    @Override
    public Expression visitGreaterThanOrEqualPredicate(JPQLNextParser.GreaterThanOrEqualPredicateContext ctx) {
        return new GePredicate(ctx.lhs.accept(this), ctx.rhs.accept(this), PredicateQuantifier.ONE, false);
    }

    @Override
    public Expression visitInPredicate(JPQLNextParser.InPredicateContext ctx) {
        Expression left = ctx.expression().accept(this);
        JPQLNextParser.InListContext inListContext = ctx.inList();
        List<JPQLNextParser.ExpressionContext> expressions = inListContext.expression();
        int size = expressions.size();
        List<Expression> right = new ArrayList<>(size);
        if (size == 1 && inListContext.LP() == null) {
            JPQLNextParser.ExpressionContext expressionContext = expressions.get(0);
            Expression expression = expressionContext.accept(this);
            boolean collectionValuedAllowed = true;
            right.add(expression);

            if (expressionContext instanceof JPQLNextParser.FunctionExpressionContext && expressionContext.getChild(0) instanceof JPQLNextParser.GenericFunctionInvocationContext) {
                // Even if a macro expressions produces a parameter, we will never consider it being collection valued
                collectionValuedAllowed = false;
            }
            if (collectionValuedAllowed && expression instanceof ParameterExpression) {
                ((ParameterExpression) expression).setCollectionValued(true);
            }
        } else {
            for (int i = 0; i < size; i++) {
                right.add(expressions.get(i).accept(this));
            }
        }

        return new InPredicate(ctx.NOT() != null, left, right);
    }

    @Override
    public Expression visitBetweenPredicate(JPQLNextParser.BetweenPredicateContext ctx) {
        return new BetweenPredicate(ctx.lhs.accept(this), ctx.start.accept(this), ctx.end.accept(this), ctx.NOT() != null);
    }

    @Override
    public Expression visitLikePredicate(JPQLNextParser.LikePredicateContext ctx) {
        Character escapeCharacter;
        if (ctx.escape == null) {
            escapeCharacter = null;
        } else {
            Expression expression = ctx.escape.accept(this);
            if (expression instanceof LiteralExpression<?>) {
                escapeCharacter = ((LiteralExpression) expression).getValue().toString().charAt(0);
            } else {
                throw new SyntaxErrorException("Only a character literal is allowed as escape character in like predicate: " + getInputText(ctx));
            }
        }
        return new LikePredicate(
                ctx.lhs.accept(this),
                ctx.like.accept(this),
                true,
                escapeCharacter,
                ctx.NOT() != null
        );
    }

    @Override
    public Expression visitMemberOfPredicate(JPQLNextParser.MemberOfPredicateContext ctx) {
        PathExpression collectionPath = (PathExpression) visitPath(ctx.path());
        collectionPath.setUsedInCollectionFunction(true);
        return new MemberOfPredicate(ctx.expression().accept(this), collectionPath, ctx.NOT() != null);
    }

    @Override
    public Expression visitErrorNode(ErrorNode node) {
        throw new SyntaxErrorException("Parsing failed: " + node.getText());
    }

    @Override
    public Expression visitIdentifier(JPQLNextParser.IdentifierContext ctx) {
        String text = ctx.getText();
        Expression entityLiteral = createEntityTypeLiteral(text);
        if (entityLiteral != null) {
            return entityLiteral;
        }

        List<PathElementExpression> pathElems = new ArrayList<>(1);
        pathElems.add(new PropertyExpression(text));
        return new PathExpression(pathElems);
    }

    @Override
    protected Expression aggregateResult(Expression aggregate, Expression nextResult) {
        return aggregate == null ? nextResult : aggregate;
    }

    private PredicateQuantifier toQuantifier(Token token) {
        if (token == null) {
            return PredicateQuantifier.ONE;
        } else {
            switch (token.getType()) {
                case JPQLNextLexer.ANY:
                    return PredicateQuantifier.ANY;
                case JPQLNextLexer.SOME:
                    return PredicateQuantifier.ANY;
                case JPQLNextLexer.ALL:
                    return PredicateQuantifier.ALL;
                default:
                    return PredicateQuantifier.ONE;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Expression createEnumLiteral(String enumStr) {
        int lastDotIdx = enumStr.lastIndexOf('.');
        if (lastDotIdx == -1) {
            return null;
        }
        String enumTypeStr = enumStr.substring(0, lastDotIdx);
        String enumValueStr = enumStr.substring(lastDotIdx + 1);
        Class<Enum<?>> enumType = enums.get(enumTypeStr);
        if (enumType == null) {
            return null;
        }
        Enum enumValue = Enum.valueOf((Class) enumType, enumValueStr);
        if (enumsForLiterals.containsKey(enumTypeStr)) {
            return new EnumLiteral(enumValue, enumStr);
        } else {
            return new ParameterExpression(enumStr.replace('.', '_'), enumValue);
        }
    }

    private Expression createEntityTypeLiteral(String entityLiteralStr) {
        Class<?> entityType = entities.get(entityLiteralStr);
        if (entityType == null) {
            return null;
        }
        return new EntityLiteral(entityType, entityLiteralStr);
    }

    private String getInputText(ParserRuleContext ctx) {
        int from = ctx.start.getStartIndex();
        int to = ctx.stop.getStopIndex();
        Interval interval = new Interval(from, to);
        return input.getText(interval);
    }
}
