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
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author Moritz Becker
 */
class JPQLSelectExpressionListenerImpl extends JPQLSelectExpressionBaseListener {
    private static final Logger LOG = Logger.getLogger("com.blazebit.persistence.parser");
    
    public JPQLSelectExpressionListenerImpl() {
    }

    public JPQLSelectExpressionListenerImpl(JPQLSelectExpressionListenerImpl parent) {
        this.parent = parent;
    }

    enum ContextType {

        FOO, PATH, ARRAY, OUTER
    }

    private CompositeExpression root = new CompositeExpression(new ArrayList<Expression>());
    private PathExpression path;
    private OuterExpression outerExpression;
    private boolean usedInCollectionFunction = false;

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
        if(outerExpression == null){
            fooContext();
        }else{
            outerContext();
        }
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
        if(outerExpression == null){
            fooContext();
        }else{
            outerContext();
        }
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
        if(outerExpression == null){
            fooContext();
        }else{
            outerContext();
        }
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
        if(outerExpression == null){
            fooContext();
        }else{
            outerContext();
        }
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
            path.setUsedInCollectionFunction(usedInCollectionFunction);
            usedInCollectionFunction = false;
        } else if(ctx == ContextType.OUTER){
            ctx = ContextType.PATH;
            path = new PathExpression(new ArrayList<PathElementExpression>());
            outerExpression.setPath(path);
            path.setUsedInCollectionFunction(usedInCollectionFunction);
            usedInCollectionFunction = false;
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
    
    private void fooContext() {
        if (this.ctx == ContextType.PATH) {
            ctx = ContextType.FOO;
            root.getExpressions().add(path);
        }else if(this.ctx == ContextType.OUTER){
            ctx = ContextType.FOO;
            root.getExpressions().add(outerExpression);
            outerExpression = null;
        }
    }
    
    private void outerContext(){
        if(this.ctx == ContextType.FOO){
            if (fooBuilder.length() > 0) {
                root.getExpressions().add(new FooExpression(fooBuilder.toString()));
                fooBuilder.setLength(0);
            }
            outerExpression = new OuterExpression();
            this.ctx = ContextType.OUTER;
        }else if(this.ctx == ContextType.PATH){
            this.ctx = ContextType.OUTER;
        }
    }

    @Override
    public void exitArray_expression(JPQLSelectExpressionParser.Array_expressionContext ctx) {
        pathContext();
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
    
    private void exitExpressionListener(){
        if (subexpressionDelegate != null) {
            return;
        }

        if (fooBuilder.length() > 0) {
            root.getExpressions().add(new FooExpression(fooBuilder.toString()));
        }
    }

    @Override
    public void exitParseSimpleExpression(JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx) {
        exitExpressionListener();
    }

    @Override
    public void exitParseSimpleSubqueryExpression(JPQLSelectExpressionParser.ParseSimpleSubqueryExpressionContext ctx) {
        exitExpressionListener();
    }

    private void arrayContext() {
        ctx = ContextType.ARRAY;
    }

    @Override
    public void enterArray_expression(JPQLSelectExpressionParser.Array_expressionContext ctx) {
        arrayContext();
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        if (subexpressionDelegate != null) {
            subexpressionDelegate.visitTerminal(node);
        } else {
            LOG.finest("visitTerminal " + node.getText());

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
                } else if(node.getSymbol().getType() == JPQLSelectExpressionLexer.Outer_function){
                    outerContext();
                } else{
                    if(node.getSymbol().getType() == JPQLSelectExpressionLexer.Size_function){
                        usedInCollectionFunction = true;
                    }
                    fooBuilder.append(node.getSymbol().getText());
                }
            } else if (ctx == ContextType.ARRAY) {
                if (node.getSymbol().getText().equals("[")) {
                    subexpressionDelegate = new JPQLSelectExpressionListenerImpl(this);
                }
            } else if (ctx == ContextType.OUTER){
                if(node.getSymbol().getText().equals(")")){
                    fooContext();
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
        throw new SyntaxErrorException("Parsing failed: " + node.getText());
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (subexpressionDelegate != null) {
            ctx.enterRule(subexpressionDelegate);
        } else {
            LOG.finest("enter" + ctx.getClass().getSimpleName());
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if (subexpressionDelegate != null) {
            ctx.exitRule(subexpressionDelegate);
        } else {
            LOG.finest("exit" + ctx.getClass().getSimpleName());
        }
    }
}
