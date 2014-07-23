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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expression.Visitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ccbem
 */
public class GroupByManager extends AbstractManager{
    private final List<NodeInfo> groupByInfos;
    
    GroupByManager(QueryGenerator queryGenerator, ParameterManager parameterManager) {
        super(queryGenerator, parameterManager);
        groupByInfos = new ArrayList<NodeInfo>();
    }
    
    void groupBy(Expression expr){
        groupByInfos.add(new NodeInfo(expr));
        registerParameterExpressions(expr);
    }
    
    void buildGroupBy(StringBuilder sb) {
        queryGenerator.setQueryBuffer(sb);
        applyGroupBys(queryGenerator, sb, groupByInfos);
    }
    
    void applyGroupBys(QueryGenerator queryGenerator, StringBuilder sb, List<NodeInfo> groupBys) {
        if (groupBys.isEmpty()) {
            return;
        }
        sb.append(" GROUP BY ");
        Iterator<NodeInfo> iter = groupBys.iterator();
        iter.next().getExpression().accept(queryGenerator);
        while (iter.hasNext()) {
            sb.append(", ");
            iter.next().getExpression().accept(queryGenerator);
        }
    }
    
    void applyTransformer(ArrayExpressionTransformer transformer){
        for (NodeInfo groupBy : groupByInfos) {
            groupBy.setExpression(transformer.transform(groupBy.getExpression()));
        }
    }
    
    void acceptVisitor(Visitor v){
        for (NodeInfo groupBy : groupByInfos) {
            groupBy.getExpression().accept(v);
        }
    }

    List<NodeInfo> getGroupByInfos() {
        return groupByInfos;
    }
    
    
}
