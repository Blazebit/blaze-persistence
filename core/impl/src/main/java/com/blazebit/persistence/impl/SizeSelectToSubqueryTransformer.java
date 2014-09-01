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

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.VisitorAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author Moritz Becker
 */
public class SizeSelectToSubqueryTransformer implements SelectInfoTransformer {

//    private final OrderByManager orderByManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final AliasManager aliasManager;

    public SizeSelectToSubqueryTransformer(SubqueryInitiatorFactory subqueryInitFactory, AliasManager aliasManager) {
        this.subqueryInitFactory = subqueryInitFactory;
        this.aliasManager = aliasManager;
    }

    @Override
    public void transform(SelectInfo info) {
        if (ExpressionUtils.isSizeExpression(info.getExpression())) {

            PathExpression sizeArg = (PathExpression) ((CompositeExpression) info.getExpression()).getExpressions().get(1);
            Class<?> collectionPropertyClass = ((JoinNode) sizeArg.getBaseNode()).getPropertyClass();
            String baseAlias = ((JoinNode) sizeArg.getBaseNode()).getAliasInfo().getAlias();
            String collectionPropertyName = sizeArg.getField();
            String collectionPropertyAlias = collectionPropertyName;
            String collectionPropertyClassName = collectionPropertyClass.getSimpleName();
            String collectionPropertyClassAlias = collectionPropertyClassName;
            
            if (aliasManager.getAliasInfo(collectionPropertyClassName) != null) {
                collectionPropertyClassAlias = aliasManager.generatePostfixedAlias(collectionPropertyClassName);
            }
            if (aliasManager.getAliasInfo(collectionPropertyName) != null) {
                collectionPropertyAlias = aliasManager.generatePostfixedAlias(collectionPropertyName);
            }
            Subquery countSubquery = (Subquery) subqueryInitFactory.createSubqueryInitiator(null, null).from(collectionPropertyClass, collectionPropertyClassAlias)
                    .select(new StringBuilder("COUNT(").append(collectionPropertyAlias).append(")").toString())
                    .leftJoin(new StringBuilder(collectionPropertyClassAlias).append('.').append(collectionPropertyName).toString(), collectionPropertyAlias)
                    .where(collectionPropertyClassAlias).eqExpression(baseAlias);

            info.setExpression(new SubqueryExpression(countSubquery));
        }

//        if (orderByManager.getOrderBySelectAliases().contains(info.getAlias())) {
//            info.getExpression().accept(new VisitorAdapter() {
//
//                @Override
//                public void visit(PathExpression expression) {
//                    ((JoinNode) expression.getBaseNode()).getClauseDependencies().add(ClauseType.ORDER_BY);
//                }
//
//            });
//
//        }
    }
}
