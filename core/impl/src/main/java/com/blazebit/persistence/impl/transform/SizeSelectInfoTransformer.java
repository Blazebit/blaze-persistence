/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl.transform;


import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.OrderByManager;
import com.blazebit.persistence.impl.SelectInfo;
import com.blazebit.persistence.parser.util.ExpressionUtils;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeSelectInfoTransformer implements ExpressionModifierVisitor<SelectInfo> {

    private final OrderByManager orderByManager;
    private final SizeTransformationVisitor sizeTransformationVisitor;

    public SizeSelectInfoTransformer(SizeTransformationVisitor sizeTransformationVisitor, OrderByManager orderByManager) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
        this.orderByManager = orderByManager;
    }

    @Override
    public void visit(SelectInfo info, ClauseType clauseType) {
        sizeTransformationVisitor.setClause(clauseType);
        sizeTransformationVisitor.setOrderBySelectClause(orderByManager.containsOrderBySelectAlias(info.getAlias()));
        if (ExpressionUtils.isSizeFunction(info.getExpression())) {
            sizeTransformationVisitor.visit(info);
        } else {
            info.getExpression().accept(sizeTransformationVisitor);
        }
    }

}
