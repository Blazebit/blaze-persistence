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

import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author ccbem
 */
public class SelectObjectBuilderImpl<T extends QueryBuilder<?, T>> extends BuilderEndedListenerImpl implements SelectObjectBuilder<T>{

    private final T result;
    // maps positions to expressions
    private final SortedMap<Integer, Expression> expressions = new TreeMap<Integer, Expression>();
    private final SelectObjectBuilderEndedListener listener;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    
    public SelectObjectBuilderImpl(T result, SelectObjectBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory) {
        this.result = result;
        this.listener = listener;
        this.subqueryInitFactory = subqueryInitFactory;
    }

    @Override
    public SelectObjectBuilder<T> with(String expression) {
        if(expressions.containsKey(expressions.size())){
            throw new IllegalStateException("Argument for position " + expressions.size() + " already specified");
        }
        
        Expression exp = Expressions.createSimpleExpression(expression);
        expressions.put(expressions.size(), exp);
        return this;
    }

    @Override
    public SelectObjectBuilder<T> with(int position, String expression) {
        if(expressions.containsKey(position)){
            throw new IllegalStateException("Argument for position " + position + " already specified");
        }
        Expression exp = Expressions.createSimpleExpression(expression);
        expressions.put(position, exp);
        return this;
    }
    
    @Override
    public T end() {
        listener.onBuilderEnded(expressions.values());
        return result;
    } 

    @Override
    public SubqueryInitiator<? extends SelectObjectBuilder<T>> with(){
        return subqueryInitFactory.createSubqueryInitiator(this, this);
    }

    @Override
    public void onBuilderEnded(SubqueryBuilder<?> builder) {
        super.onBuilderEnded(builder);
        expressions.put(expressions.size(), new SubqueryExpression(builder));
    }
    
    
}
