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

package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.MiddleOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class SetOperationManager {

    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> startQueryBuilder;
    private SetOperationType operator;
    private final boolean nested;
    private final List<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> setOperations;

    SetOperationManager(SetOperationType operator, boolean nested) {
        this.operator = operator;
        this.nested = nested;
        this.setOperations = new ArrayList<>();
    }

    SetOperationManager(SetOperationManager original, QueryContext queryContext) {
        this.operator = original.operator;
        this.nested = original.nested;
        this.startQueryBuilder = original.startQueryBuilder.copy(queryContext);
        this.setOperations = new ArrayList<>(original.setOperations.size());
        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperation : original.setOperations) {
            setOperations.add(setOperation.copy(queryContext));
        }
    }

    List<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> getSetOperations() {
        return setOperations;
    }

    boolean hasSetOperations() {
        return setOperations.size() > 0;
    }
    
    boolean isNested() {
        return nested;
    }

    boolean isEmpty() {
        if (startQueryBuilder != null) {
            if (!startQueryBuilder.isEmpty()) {
                return false;
            }
        }
        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder : setOperations) {
            if (!builder.isEmpty()) {
                return false;
            }
        }

        return true;
    }
    
    AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getStartQueryBuilder() {
        return startQueryBuilder;
    }
    
    void setStartQueryBuilder(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> startQueryBuilder) {
        this.startQueryBuilder = startQueryBuilder;
    }

    SetOperationType getOperator() {
        return operator;
    }
    
    void setOperator(SetOperationType operator) {
        this.operator = operator;
    }
    
    void addSetOperation(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (startQueryBuilder.isEmpty()) {
            startQueryBuilder = queryBuilder;
            operator = null;
        } else {
            setOperations.add(queryBuilder);
        }
    }

    public <T, Z extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> void replaceOperand(MiddleOngoingSetOperationCTECriteriaBuilder<T, Z> oldOperand, Z newOperand) {
        if (startQueryBuilder == oldOperand) {
            startQueryBuilder = newOperand;
            return;
        } else {
            for (int i = 0; i < setOperations.size(); i++) {
                if (setOperations.get(i) == oldOperand) {
                    setOperations.set(i, newOperand);
                    return;
                }
            }
        }

        throw new IllegalStateException("Could not replace old with new operand!");
    }

    public boolean removeOperand(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> operand) {
        BaseFinalSetOperationBuilderImpl<?, ?, ?> finalSetOperationBuilder;
        if (startQueryBuilder == operand) {
            startQueryBuilder = null;
            return true;
        }
        if (startQueryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?> && ((BaseFinalSetOperationBuilderImpl<?, ?, ?>) startQueryBuilder).setOperationManager.removeOperand(operand)) {
            return true;
        }
        for (int i = 0; i < setOperations.size(); i++) {
            if (setOperations.get(i) == operand) {
                setOperations.remove(i);
                return true;
            }
            if (setOperations.get(i) instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?> && (finalSetOperationBuilder = (BaseFinalSetOperationBuilderImpl<?, ?, ?>) setOperations.get(i)).setOperationManager.removeOperand(operand)) {
                if (finalSetOperationBuilder.isEmpty()) {
                    setOperations.remove(i);
                }
                return true;
            }
        }

        return false;
    }
}
