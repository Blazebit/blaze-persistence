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


/**
 *
 * @author Moritz Becker
 */
public class SizeSelectInfoTransformer implements SelectInfoTransformer {

    private final OrderByManager orderByManager;
    private final SizeTransformationVisitor sizeTransformationVisitor;
    private final SelectManager<?> selectManager;

    public SizeSelectInfoTransformer(SizeTransformationVisitor sizeTransformationVisitor, OrderByManager orderByManager, SelectManager selectManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.orderByManager = orderByManager;
        this.selectManager = selectManager;
    }

    @Override
    public void transform(SelectInfo info) {
        sizeTransformationVisitor.setOrderBySelectClause(orderByManager.getOrderBySelectAliases().contains(info.getAlias()));
        sizeTransformationVisitor.setClause(ClauseType.SELECT);
        boolean[] groupBySelectStatus = selectManager.containsGroupBySelect(true);
        sizeTransformationVisitor.setHasGroupBySelects(groupBySelectStatus[0]);
        sizeTransformationVisitor.setHasComplexGroupBySelects(groupBySelectStatus[1]);
        if (com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(info.getExpression())) {
            info.setExpression(info.getExpression().accept(sizeTransformationVisitor));
        } else {
            info.getExpression().accept(sizeTransformationVisitor);
        }
    }

}
