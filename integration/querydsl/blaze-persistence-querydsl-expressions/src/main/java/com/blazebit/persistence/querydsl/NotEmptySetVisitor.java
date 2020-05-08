/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Operation;
import com.querydsl.core.types.SubQueryExpression;

import static com.blazebit.persistence.querydsl.SetOperationFlag.getSetOperationFlag;

/**
 * Visitor implementation that checks if a query is empty (i.e. has no default joins).
 * Empty queries are removed from set operations, as they cannot be represented in SQL.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class NotEmptySetVisitor extends DefaultVisitorImpl<Boolean, Void> {

    public static final NotEmptySetVisitor INSTANCE = new NotEmptySetVisitor();

    @Override
    public Boolean visit(Operation<?> operation, Void aVoid) {
        return operation.getArg(0).accept(this, aVoid);
    }

    @Override
    public Boolean visit(SubQueryExpression<?> subQueryExpression, Void aVoid) {
        SetOperationFlag setOperationFlag = getSetOperationFlag(subQueryExpression.getMetadata());
        return setOperationFlag != null && setOperationFlag.getFlag().accept(this, aVoid)
                || !subQueryExpression.getMetadata().getJoins().isEmpty();
    }
}
