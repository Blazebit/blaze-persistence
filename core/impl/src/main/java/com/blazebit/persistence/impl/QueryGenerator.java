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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.SelectManager.SelectInfo;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.InSubqueryPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsMemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import com.blazebit.persistence.impl.predicate.QuantifiableBinaryExpressionPredicate;

/**
 *
 * @author ccbem
 */
public class QueryGenerator implements Predicate.Visitor, Expression.Visitor {

    private StringBuilder sb;
    private final ParameterManager parameterManager;
    private boolean replaceSelectAliases = true;
    private boolean generateRequiredMapKeyFiltersOnly = false;
    // cyclic dependency
    private SelectManager<?> selectManager;

    public QueryGenerator(ParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    public QueryGenerator(SelectManager<?> selectManager, ParameterManager parameterManager) {
        this(parameterManager);
        this.selectManager = selectManager;
    }

    void setSelectManager(SelectManager<?> selectManager) {
        this.selectManager = selectManager;
    }

    void setQueryBuffer(StringBuilder sb) {
        this.sb = sb;
    }

    public void setGenerateRequiredMapKeyFiltersOnly(boolean generateRequiredMapKeyFiltersOnly) {
        this.generateRequiredMapKeyFiltersOnly = generateRequiredMapKeyFiltersOnly;
    }

    @Override
    public void visit(AndPredicate predicate) {
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final String and = " AND ";
        for (Predicate child : predicate.getChildren()) {
            if (child instanceof OrPredicate) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                if (len == sb.length()) {
                    // delete "("
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(and);
                }

            } else if (child instanceof EqPredicate || generateRequiredMapKeyFiltersOnly == false) {
                int len = sb.length();
                child.accept(this);
                if (len < sb.length()) {
                    sb.append(and);
                }
            }
        }
//        char[] currentPostfix = new char[and.length()];
//        int len = sb.length();
//        sb.getChars(len-and.length(), and.length(), currentPostfix, 0);
//        if(and.equals(String.valueOf(currentPostfix))){
//            //remove unused " AND " postfix
//            sb.delete(len-and.length(), and.length());
//        }   

        if (predicate.getChildren().size() > 1) {
            sb.delete(sb.length() - and.length(), sb.length());
        }
    }

    @Override
    public void visit(OrPredicate predicate) {
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final String or = " OR ";
        for (Predicate child : predicate.getChildren()) {
            if (child instanceof AndPredicate) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                if (len == sb.length()) {
                    // delete "("
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(or);
                }

            } else if (child instanceof EqPredicate || generateRequiredMapKeyFiltersOnly == false) {
                int len = sb.length();
                child.accept(this);
                if (len < sb.length()) {
                    sb.append(or);
                }
            }
        }
        if (predicate.getChildren().size() > 1) {
            sb.delete(sb.length() - or.length(), sb.length());
        }
    }

    @Override
    public void visit(NotPredicate predicate) {
        sb.append("NOT ");
        predicate.getPredicate().accept(this);
    }

    @Override
    public void visit(EqPredicate predicate) {
        if (generateRequiredMapKeyFiltersOnly == false || predicate.isRequiredByMapValueSelect() == true) {
            visitQuantifiableBinaryPredicate(predicate, " = ");
        }
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        predicate.getExpression().accept(this);
        sb.append(" IS NULL");
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        predicate.getExpression().accept(this);
        sb.append(" IS EMPTY");
    }

    @Override
    public void visit(IsMemberOfPredicate predicate) {
        predicate.getLeft().accept(this);
        sb.append(" MEMBER OF ");
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LikePredicate predicate) {
        if (!predicate.isCaseSensitive()) {
            sb.append("UPPER(");
        }
        predicate.getLeft().accept(this);
        if (!predicate.isCaseSensitive()) {
            sb.append(")");
        }
        sb.append(" LIKE ");
        if (!predicate.isCaseSensitive()) {
            sb.append("UPPER(");
        }
        predicate.getRight().accept(this);
        if (!predicate.isCaseSensitive()) {
            sb.append(")");
        }
        if (predicate.getEscapeCharacter() != null) {
            sb.append(" ESCAPE ");
            if (!predicate.isCaseSensitive()) {
                sb.append("UPPER(");
            }
            sb.append("'").append(predicate.getEscapeCharacter()).append("'");
            if (!predicate.isCaseSensitive()) {
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        predicate.getLeft().accept(this);
        sb.append(" BETWEEN ");
        predicate.getStart().accept(this);
        sb.append(" AND ");
        predicate.getEnd().accept(this);
    }

    @Override
    public void visit(InPredicate predicate) {
        predicate.getLeft().accept(this);
        sb.append(" IN (");
        predicate.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(InSubqueryPredicate predicate) {
        predicate.getLeft().accept(this);
        sb.append(" IN (");
        sb.append(predicate.getRight().getQueryString());
        sb.append(")");
    }
    
    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        predicate.getLeft().accept(this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
            sb.append("(");
        }
        predicate.getRight().accept(this);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(")");
        }
    }

    @Override
    public void visit(GtPredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " > ");
    }

    @Override
    public void visit(GePredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " >= ");
    }

    @Override
    public void visit(LtPredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " < ");
    }

    @Override
    public void visit(LePredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " <= ");
    }

    /* Expression.Visitor */
    //TODO: remove
    @Override
    public void visit(PropertyExpression expression) {

    }

    @Override
    public void visit(ParameterExpression expression) {
        String paramName;
        if (expression.getName() == null) {
//            paramName = parameterManager.getParamNameForObject(expression.getValue());
            throw new IllegalStateException("Unsatisfied parameter " + expression.getName());
        } else {
            paramName = expression.getName();
        }
        sb.append(":");
        sb.append(paramName);
    }

    @Override
    public void visit(CompositeExpression expression) {
        for (Expression e : expression.getExpressions()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(FooExpression expression) {
        sb.append(expression.getString());
    }

    @Override
    public void visit(PathExpression expression) {
        if (replaceSelectAliases) {
            if (expression.getBaseNode() != null) {
                String absPath = expression.getBaseNode().getAliasInfo().getAbsolutePath();
                SelectInfo selectInfo = selectManager.getSelectAbsolutePathToInfoMap().get(absPath);
                if (selectInfo != null) {
                    sb.append(selectInfo.getAlias());
                    return;
                }
            }
        }
        if (expression.getBaseNode() == null) {
            sb.append(expression.getPath());
        } else if (expression.getField() == null) {
            sb.append(expression.getBaseNode().getAliasInfo().getAlias());
        } else {
            sb.append(expression.getBaseNode().getAliasInfo().getAlias())
                    .append(".")
                    .append(expression.getField());
        }
    }

    public boolean isReplaceSelectAliases() {
        return replaceSelectAliases;
    }

    public void setReplaceSelectAliases(boolean replaceSelectAliases) {
        this.replaceSelectAliases = replaceSelectAliases;
    }

    @Override
    public void visit(ArrayExpression expression) {
    }

}
