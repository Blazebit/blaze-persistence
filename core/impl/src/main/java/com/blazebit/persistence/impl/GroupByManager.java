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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expression.Visitor;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class GroupByManager extends AbstractManager {

    private final Set<NodeInfo> groupByInfos;

    GroupByManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager) {
        super(queryGenerator, parameterManager);
        groupByInfos = new HashSet<NodeInfo>();
    }

    void groupBy(Expression expr) {
        groupByInfos.add(new NodeInfo(expr));
        registerParameterExpressions(expr);
    }

    Set<String> buildGroupByClauses() {
        if (groupByInfos.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> groupByClauses = new HashSet<String>();
        boolean conditionalContext = queryGenerator.setConditionalContext(false);
        StringBuilder sb = new StringBuilder();
        
        for(NodeInfo info : groupByInfos){
            sb.setLength(0);
            queryGenerator.setQueryBuffer(sb);
            info.getExpression().accept(queryGenerator);
            groupByClauses.add(sb.toString());
        }
        queryGenerator.setConditionalContext(conditionalContext);
        return groupByClauses;
    }
    
    void buildGroupBy(StringBuilder sb, Set<String> clauses) {
        if(!clauses.isEmpty()){
            sb.append(" GROUP BY ");
            build(sb, clauses);
        }
    }

    void applyTransformer(ExpressionTransformer transformer) {
        for (NodeInfo groupBy : groupByInfos) {
            groupBy.setExpression(transformer.transform(groupBy.getExpression(), ClauseType.GROUP_BY));
        }
    }

    void acceptVisitor(Visitor v) {
        for (NodeInfo groupBy : groupByInfos) {
            groupBy.getExpression().accept(v);
        }
    }
    
    boolean hasGroupBys() {
    	return groupByInfos.size() > 0;
    }

    boolean isEmpty() {
        return groupByInfos.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.groupByInfos != null ? this.groupByInfos.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupByManager other = (GroupByManager) obj;
        if (this.groupByInfos != other.groupByInfos && (this.groupByInfos == null || !this.groupByInfos.equals(other.groupByInfos))) {
            return false;
        }
        return true;
    }

}
