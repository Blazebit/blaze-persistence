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

import com.blazebit.persistence.parser.JPQLSelectExpressionBaseListener;
import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionListener;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author Moritz Becker
 */
class JPQLSelectExpressionListenerImpl extends JPQLSelectExpressionBaseListener {

    public JPQLSelectExpressionListenerImpl() {
    }

    public JPQLSelectExpressionListenerImpl(JPQLSelectExpressionListenerImpl parent) {
        this.parent = parent;
    }

    enum ContextType {

        FOO, PATH, ARRAY, PARAM
    }

    private CompositeExpression root = new CompositeExpression(new ArrayList<Expression>());
    private PathExpression path;

    private PropertyExpression arrayExprBase;
    private Expression arrayExprIndex;
    private ParameterExpression arrayIndexParam;

    private StringBuilder fooBuilder = new StringBuilder();

    private ContextType ctx = ContextType.FOO;

    private JPQLSelectExpressionListenerImpl parent;
    private JPQLSelectExpressionListenerImpl subexpressionDelegate;

    public CompositeExpression getCompositeExpression() {
        return root;
    }

    @Override
    public void enterState_field_path_expression(JPQLSelectExpressionParser.State_field_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        if (this.ctx != ContextType.ARRAY) {
            pathContext();
        }
    }

    @Override
    public void exitState_field_path_expression(JPQLSelectExpressionParser.State_field_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        fooContext();
    }

    @Override
    public void enterSingle_valued_object_path_expression(JPQLSelectExpressionParser.Single_valued_object_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        if (this.ctx != ContextType.ARRAY) {
            pathContext();
        }

    }

    @Override
    public void exitSingle_valued_object_path_expression(JPQLSelectExpressionParser.Single_valued_object_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        fooContext();
    }

    @Override
    public void enterCollection_valued_path_expression(JPQLSelectExpressionParser.Collection_valued_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        if (this.ctx != ContextType.ARRAY) {
            pathContext();
        }
    }

    @Override
    public void exitCollection_valued_path_expression(JPQLSelectExpressionParser.Collection_valued_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        fooContext();
    }

    @Override
    public void enterSingle_element_path_expression(JPQLSelectExpressionParser.Single_element_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        if (this.ctx != ContextType.ARRAY) {
            pathContext();
        }
    }

    @Override
    public void exitSingle_element_path_expression(JPQLSelectExpressionParser.Single_element_path_expressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }
        fooContext();
    }

    private void pathContext() {
        // take action depending on current context
        if (ctx == ContextType.FOO) {
            if (fooBuilder.length() > 0) {
                root.getExpressions().add(new FooExpression(fooBuilder.toString()));
                fooBuilder.setLength(0);
            }
            ctx = ContextType.PATH;
            path = new PathExpression(new ArrayList<PathElementExpression>());
        } else if (ctx == ContextType.ARRAY) {
            ArrayExpression arrayExpr;
            if (arrayExprIndex != null) {
                // the current array expression has an expression index
                arrayExpr = new ArrayExpression(arrayExprBase, arrayExprIndex);
                arrayExprIndex = null;
            } else {
                // the current array expression has an parameter index
                arrayExpr = new ArrayExpression(arrayExprBase, arrayIndexParam);
                arrayIndexParam = null;
            }
            path.getExpressions().add(arrayExpr);

            arrayExprBase = null;
            ctx = ContextType.PATH;
        }
    }

    private void paramContext() {
        if (this.ctx == ContextType.FOO) {
            ctx = ContextType.PARAM;
        }
    }
    
    private void fooContext() {
        if (this.ctx == ContextType.PATH) {
            ctx = ContextType.FOO;
            root.getExpressions().add(path);
        }else if(this.ctx == ContextType.PARAM){
            ctx = ContextType.FOO;
        }
    }

    @Override
    public void exitArray_expression(JPQLSelectExpressionParser.Array_expressionContext ctx) {
        pathContext();
    }

    @Override
    public void enterGeneral_path_start(JPQLSelectExpressionParser.General_path_startContext ctx) {
    }

    @Override
    public void exitGeneral_path_start(JPQLSelectExpressionParser.General_path_startContext ctx) {
    }

    @Override
    public void enterGeneral_path_element(JPQLSelectExpressionParser.General_path_elementContext ctx) {

    }

    @Override
    public void exitGeneral_path_element(JPQLSelectExpressionParser.General_path_elementContext ctx) {
    }

    private void applyPathElement(String property) {
        if (ctx == ContextType.ARRAY) {
            arrayExprBase = new PropertyExpression(property);
        } else if (ctx == ContextType.PATH) {
            path.getExpressions().add(new PropertyExpression(property));
        }
    }

    @Override
    public void enterSimple_path_element(JPQLSelectExpressionParser.Simple_path_elementContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }

        applyPathElement(ctx.getText());
    }

    @Override
    public void exitSimple_path_element(JPQLSelectExpressionParser.Simple_path_elementContext ctx) {
    }

    @Override
    public void enterGeneral_subpath(JPQLSelectExpressionParser.General_subpathContext ctx) {
//        root.getExpressions().add(new PropertyExpression(ctx.getText()));
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exitGeneral_subpath(JPQLSelectExpressionParser.General_subpathContext ctx) {
    }

    @Override
    public void enterArithmetic_factor(JPQLSelectExpressionParser.Arithmetic_factorContext ctx) {
    }

    @Override
    public void exitArithmetic_factor(JPQLSelectExpressionParser.Arithmetic_factorContext ctx) {
    }

    @Override
    public void enterCase_operand(JPQLSelectExpressionParser.Case_operandContext ctx) {
    }

    @Override
    public void exitCase_operand(JPQLSelectExpressionParser.Case_operandContext ctx) {
    }

    @Override
    public void enterEnum_expression(JPQLSelectExpressionParser.Enum_expressionContext ctx) {
    }

    @Override
    public void exitEnum_expression(JPQLSelectExpressionParser.Enum_expressionContext ctx) {
    }

    @Override
    public void enterString_expression(JPQLSelectExpressionParser.String_expressionContext ctx) {
    }

    @Override
    public void exitString_expression(JPQLSelectExpressionParser.String_expressionContext ctx) {
    }

    @Override
    public void enterParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
    }

    @Override
    public void exitParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
        if (subexpressionDelegate != null) {
            return;
        }

        if (fooBuilder.length() > 0) {
            root.getExpressions().add(new FooExpression(fooBuilder.toString()));
        }
    }

    private void arrayContext() {
        ctx = ContextType.ARRAY;
    }

    @Override
    public void enterArray_expression(JPQLSelectExpressionParser.Array_expressionContext ctx) {
        arrayContext();
    }

    @Override
    public void enterSimple_entity_expression(JPQLSelectExpressionParser.Simple_entity_expressionContext ctx) {
    }

    @Override
    public void exitSimple_entity_expression(JPQLSelectExpressionParser.Simple_entity_expressionContext ctx) {
    }

    @Override
    public void enterDatetime_expression(JPQLSelectExpressionParser.Datetime_expressionContext ctx) {
    }

    @Override
    public void exitDatetime_expression(JPQLSelectExpressionParser.Datetime_expressionContext ctx) {
    }

    @Override
    public void enterParseCaseOperandExpression(JPQLSelectExpressionParser.ParseCaseOperandExpressionContext ctx) {
    }

    @Override
    public void exitParseCaseOperandExpression(JPQLSelectExpressionParser.ParseCaseOperandExpressionContext ctx) {
    }

    @Override
    public void enterEntity_expression(JPQLSelectExpressionParser.Entity_expressionContext ctx) {
    }

    @Override
    public void exitEntity_expression(JPQLSelectExpressionParser.Entity_expressionContext ctx) {
    }

    @Override
    public void enterSimple_expression(JPQLSelectExpressionParser.Simple_expressionContext ctx) {
    }

    @Override
    public void exitSimple_expression(JPQLSelectExpressionParser.Simple_expressionContext ctx) {
    }

    @Override
    public void enterFunctions_returning_strings(JPQLSelectExpressionParser.Functions_returning_stringsContext ctx) {
    }

    @Override
    public void exitFunctions_returning_strings(JPQLSelectExpressionParser.Functions_returning_stringsContext ctx) {
    }

    @Override
    public void enterFunctions_returning_numerics(JPQLSelectExpressionParser.Functions_returning_numericsContext ctx) {
    }

    @Override
    public void exitFunctions_returning_numerics(JPQLSelectExpressionParser.Functions_returning_numericsContext ctx) {
    }

    @Override
    public void enterParseScalarExpression(JPQLSelectExpressionParser.ParseScalarExpressionContext ctx) {
    }

    @Override
    public void exitParseScalarExpression(JPQLSelectExpressionParser.ParseScalarExpressionContext ctx) {
    }

    @Override
    public void enterSingle_valued_path_expression(JPQLSelectExpressionParser.Single_valued_path_expressionContext ctx) {
    }

    @Override
    public void exitSingle_valued_path_expression(JPQLSelectExpressionParser.Single_valued_path_expressionContext ctx) {
    }

    @Override
    public void enterQualified_identification_variable(JPQLSelectExpressionParser.Qualified_identification_variableContext ctx) {
    }

    @Override
    public void exitQualified_identification_variable(JPQLSelectExpressionParser.Qualified_identification_variableContext ctx) {
    }

    @Override
    public void enterArithmetic_expression(JPQLSelectExpressionParser.Arithmetic_expressionContext ctx) {
    }

    @Override
    public void exitArithmetic_expression(JPQLSelectExpressionParser.Arithmetic_expressionContext ctx) {
    }

    @Override
    public void enterEntity_type_expression(JPQLSelectExpressionParser.Entity_type_expressionContext ctx) {
    }

    @Override
    public void exitEntity_type_expression(JPQLSelectExpressionParser.Entity_type_expressionContext ctx) {
    }

    @Override
    public void enterNullif_expression(JPQLSelectExpressionParser.Nullif_expressionContext ctx) {
    }

    @Override
    public void exitNullif_expression(JPQLSelectExpressionParser.Nullif_expressionContext ctx) {
    }

    @Override
    public void enterFunction_arg(JPQLSelectExpressionParser.Function_argContext ctx) {
    }

    @Override
    public void exitFunction_arg(JPQLSelectExpressionParser.Function_argContext ctx) {
    }

    @Override
    public void enterFunctions_returning_datetime(JPQLSelectExpressionParser.Functions_returning_datetimeContext ctx) {
    }

    @Override
    public void exitFunctions_returning_datetime(JPQLSelectExpressionParser.Functions_returning_datetimeContext ctx) {
    }

    @Override
    public void enterTrim_specification(JPQLSelectExpressionParser.Trim_specificationContext ctx) {
    }

    @Override
    public void exitTrim_specification(JPQLSelectExpressionParser.Trim_specificationContext ctx) {
    }

    @Override
    public void enterScalar_expression(JPQLSelectExpressionParser.Scalar_expressionContext ctx) {
    }

    @Override
    public void exitScalar_expression(JPQLSelectExpressionParser.Scalar_expressionContext ctx) {
    }

    @Override
    public void enterArithmetic_term(JPQLSelectExpressionParser.Arithmetic_termContext ctx) {
    }

    @Override
    public void exitArithmetic_term(JPQLSelectExpressionParser.Arithmetic_termContext ctx) {
    }

    @Override
    public void enterComposable_qualified_identification_variable(JPQLSelectExpressionParser.Composable_qualified_identification_variableContext ctx) {
    }

    @Override
    public void exitComposable_qualified_identification_variable(JPQLSelectExpressionParser.Composable_qualified_identification_variableContext ctx) {
    }

    @Override
    public void enterBoolean_expression(JPQLSelectExpressionParser.Boolean_expressionContext ctx) {
    }

    @Override
    public void exitBoolean_expression(JPQLSelectExpressionParser.Boolean_expressionContext ctx) {
    }

    @Override
    public void enterAggregate_expression(JPQLSelectExpressionParser.Aggregate_expressionContext ctx) {
    }

    @Override
    public void exitAggregate_expression(JPQLSelectExpressionParser.Aggregate_expressionContext ctx) {
    }

    @Override
    public void enterType_discriminator(JPQLSelectExpressionParser.Type_discriminatorContext ctx) {
    }

    @Override
    public void exitType_discriminator(JPQLSelectExpressionParser.Type_discriminatorContext ctx) {
    }

    @Override
    public void enterFunction_invocation(JPQLSelectExpressionParser.Function_invocationContext ctx) {
    }

    @Override
    public void exitFunction_invocation(JPQLSelectExpressionParser.Function_invocationContext ctx) {
    }

    @Override
    public void enterCoalesce_expression(JPQLSelectExpressionParser.Coalesce_expressionContext ctx) {
    }

    @Override
    public void exitCoalesce_expression(JPQLSelectExpressionParser.Coalesce_expressionContext ctx) {
    }

    @Override
    public void enterLiteral_temporal(JPQLSelectExpressionParser.Literal_temporalContext ctx) {
    }

    @Override
    public void exitLiteral_temporal(JPQLSelectExpressionParser.Literal_temporalContext ctx) {
    }

    @Override
    public void enterArithmetic_primary(JPQLSelectExpressionParser.Arithmetic_primaryContext ctx) {
    }

    @Override
    public void exitArithmetic_primary(JPQLSelectExpressionParser.Arithmetic_primaryContext ctx) {
    }

    @Override
    public void enterCase_expression(JPQLSelectExpressionParser.Case_expressionContext ctx) {
    }

    @Override
    public void exitCase_expression(JPQLSelectExpressionParser.Case_expressionContext ctx) {
    }

    @Override
    public void enterLiteral(JPQLSelectExpressionParser.LiteralContext ctx) {
    }

    @Override
    public void exitLiteral(JPQLSelectExpressionParser.LiteralContext ctx) {
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        if (subexpressionDelegate != null) {
            subexpressionDelegate.visitTerminal(node);
        } else {
            System.out.println("visitTerminal " + node.getText());

            if (node.getSymbol().getText().equals("]")) {
                if (fooBuilder.length() > 0) {
                    root.getExpressions().add(new FooExpression(fooBuilder.toString()));
                }
                parent.exitSubexpressionParsing();
            }

            if (ctx == ContextType.FOO) {
                if(node.getSymbol().getType() == JPQLSelectExpressionLexer.Input_parameter){
                    // cut of ':' at the start
                    root.getExpressions().add(new ParameterExpression(node.getText().substring(1)));
                }else{
                    fooBuilder.append(node.getSymbol().getText());
                }
            } else if (ctx == ContextType.ARRAY) {
                if (node.getSymbol().getText().equals("[")) {
                    subexpressionDelegate = new JPQLSelectExpressionListenerImpl(this);
                }
            }
        }
    }

    private void exitSubexpressionParsing() {
        CompositeExpression subexpression = subexpressionDelegate.getCompositeExpression();
        // unwrap if possible
        if (subexpression.getExpressions().size() == 1) {
            arrayExprIndex = subexpression.getExpressions().get(0);
        } else {
            arrayExprIndex = subexpression;
        }
        subexpressionDelegate = null;
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        throw new IllegalStateException("Parsing failed: " + node.getText());
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (subexpressionDelegate != null) {
            ctx.enterRule(subexpressionDelegate);
        } else {
            System.out.println("enter" + ctx.getClass().getSimpleName());
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if (subexpressionDelegate != null) {
            ctx.exitRule(subexpressionDelegate);
        } else {
            System.out.println("exit" + ctx.getClass().getSimpleName());
        }
    }
}
