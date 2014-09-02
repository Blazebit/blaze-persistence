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

import com.blazebit.persistence.parser.JPQLSelectExpressionBaseVisitor;
import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author Moritz Becker
 */
public class JPQLSelectExpressionVisitorImpl extends JPQLSelectExpressionBaseVisitor<Expression> {

    private final BufferedTokenStream tokens;

    public JPQLSelectExpressionVisitorImpl(BufferedTokenStream tokens) {
        this.tokens = tokens;
        
        for(int i = 0; i < tokens.size(); i++){
            System.out.println(tokens.get(i).getText() + " - " + tokens.get(i).getChannel());
        }
    }

    @Override
    public Expression visitParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
        Expression expr = ctx.simple_expression().accept(this);
        if (!(expr instanceof CompositeExpression)) {
            return new CompositeExpression(new ArrayList<Expression>(Arrays.asList(expr)));
        }
        return expr;
    }

    @Override
    public Expression visitFunctions_returning_numerics(JPQLSelectExpressionParser.Functions_returning_numericsContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitStringFunction(JPQLSelectExpressionParser.StringFunctionContext ctx) {
        return handleFunction(ctx.getStart().getText(), ctx);
    }

    @Override
    public Expression visitAggregateExpression(JPQLSelectExpressionParser.AggregateExpressionContext ctx) {
        return new AggregateExpression(ctx.distinct != null, ctx.funcname.getText(), (PathExpression) ctx.aggregate_argument().accept(this));
    }

    @Override
    public Expression visitCountStar(JPQLSelectExpressionParser.CountStarContext ctx) {
        return new AggregateExpression();
    }

    private Expression handleFunction(String name, ParseTree ctx) {
        List<Expression> funcArgs = new ArrayList<Expression>(ctx.getChildCount());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (!(ctx.getChild(i) instanceof TerminalNode)) {
                funcArgs.add(ctx.getChild(i).accept(this));
            }
        }
        return new FunctionExpression(name, funcArgs);
    }

    @Override
    public Expression visitFunction_invocation(JPQLSelectExpressionParser.Function_invocationContext ctx) {
        List<Expression> funcArgs = new ArrayList<Expression>(ctx.getChildCount());
        for (JPQLSelectExpressionParser.Function_argContext argCtx : ctx.args) {
            funcArgs.add(argCtx.accept(this));
        }
        return new FunctionExpression(ctx.funcname.getText(), funcArgs);
    }

    @Override
    public Expression visitState_field_path_expression(JPQLSelectExpressionParser.State_field_path_expressionContext ctx) {
        PathExpression result = (PathExpression) ctx.general_subpath().accept(this);
        result.getExpressions().add((PathElementExpression) ctx.general_path_element().accept(this));
        return result;
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
    public Expression visitSingle_valued_object_path_expression(JPQLSelectExpressionParser.Single_valued_object_path_expressionContext ctx) {
        List<PathElementExpression> pathElements = new ArrayList<PathElementExpression>();
        pathElements.add((PathElementExpression) ctx.general_subpath().accept(this));
        pathElements.add((PathElementExpression) ctx.general_path_element().accept(this));
        return new PathExpression(pathElements);
    }

    @Override
    public Expression visitSingle_element_path_expression(JPQLSelectExpressionParser.Single_element_path_expressionContext ctx) {
        return new PathExpression(Arrays.asList(new PathElementExpression[]{(PathElementExpression) ctx.general_path_start().accept(this)}));
    }

    @Override
    public Expression visitSimple_path_element(JPQLSelectExpressionParser.Simple_path_elementContext ctx) {
        return new PropertyExpression(ctx.Identifier().getText());
    }

    @Override
    public Expression visitType_discriminator(JPQLSelectExpressionParser.Type_discriminatorContext ctx) {
        return new FunctionExpression("TYPE", Arrays.asList(new Expression[]{ctx.getChild(1).accept(this)}));
    }

    @Override
    public Expression visitEntry(JPQLSelectExpressionParser.EntryContext ctx) {
        return new FunctionExpression("ENTRY", Arrays.asList(new Expression[]{ctx.collection_valued_path_expression().accept(this)}));
    }

    @Override
    public Expression visitComposable_qualified_identification_variable(JPQLSelectExpressionParser.Composable_qualified_identification_variableContext ctx) {
        return new FunctionExpression(ctx.name.getText(), Arrays.asList(new Expression[]{ctx.collection_valued_path_expression().accept(this)}));
    }

    @Override
    public Expression visitArray_expression(JPQLSelectExpressionParser.Array_expressionContext ctx) {
        return new ArrayExpression((PropertyExpression) ctx.simple_path_element().accept(this), ctx.arithmetic_expression().accept(this));
    }

    @Override
    public Expression visitArithmeticExpressionPlusMinus(JPQLSelectExpressionParser.ArithmeticExpressionPlusMinusContext ctx) {
//        CompositeExpression expr = accept(ctx.expr);
//        expr.append(ctx.op.getText());
//        acceptAndCompose(expr, ctx.term);
//        return expr;
        return super.visit(ctx);
    }

    @Override
    public Expression visitArithmeticPrimaryParanthesis(JPQLSelectExpressionParser.ArithmeticPrimaryParanthesisContext ctx) {
        CompositeExpression expr = accept(ctx.arithmetic_expression());
        expr.prepend("(" + tokenListToString(tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex())).toString());
        expr.append(")");
        return expr;
    }

    private CompositeExpression acceptAndCompose(CompositeExpression composite, ParserRuleContext ruleContext) {
        Expression expr = ruleContext.accept(this);
        composite.append(expr);

        List<Token> rightHiddenTokens = tokens.getHiddenTokensToRight(ruleContext.getStop().getTokenIndex());
        composite.append(tokenListToString(rightHiddenTokens));
        return composite;
    }

    private CompositeExpression accept(ParserRuleContext ruleContext) {
        Expression expr = ruleContext.accept(this);
        CompositeExpression composite;
        if (expr instanceof CompositeExpression) {
            composite = (CompositeExpression) expr;
        } else {
            composite = new CompositeExpression(new ArrayList<Expression>(Arrays.asList(expr)));
        }
        List<Token> rightHiddenTokens = tokens.getHiddenTokensToRight(ruleContext.getStop().getTokenIndex());
        composite.append(tokenListToString(rightHiddenTokens));
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

    @Override
    public Expression visitArithmeticMultDiv(JPQLSelectExpressionParser.ArithmeticMultDivContext ctx) {
        CompositeExpression expr = accept(ctx.term);
        expr.append(ctx.op.getText());
        acceptAndCompose(expr, ctx.factor);

        return expr;
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
    public Expression visitErrorNode(ErrorNode node) {
        throw new SyntaxErrorException("Parsing failed: " + node.getText());
    }

    @Override
    public Expression visitTerminal(TerminalNode node) {
        switch (node.getSymbol().getType()) {
            case JPQLSelectExpressionLexer.Input_parameter:
                return new ParameterExpression(node.getText().substring(1));
            case JPQLSelectExpressionLexer.Numeric_literal:
            case JPQLSelectExpressionLexer.String_literal:
            case JPQLSelectExpressionLexer.Boolean_literal:
//            case JPQLSelectExpressionLexer.Enum_literal:
            case JPQLSelectExpressionLexer.Date_literal:
            case JPQLSelectExpressionLexer.Time_literal:
            case JPQLSelectExpressionLexer.Timestamp_literal:
            case JPQLSelectExpressionLexer.Character_literal:
                return new FooExpression(node.getText());
            default:
                return super.visitTerminal(node);
        }

    }

}
