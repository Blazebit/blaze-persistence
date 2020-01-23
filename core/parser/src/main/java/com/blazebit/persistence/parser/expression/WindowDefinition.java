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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class WindowDefinition {
    private final String windowName;
    private final List<Expression> partitionExpressions;
    private final List<OrderByItem> orderByExpressions;
    private Predicate filterPredicate;

    private final WindowFrameMode frameMode;
    private final WindowFramePositionType frameStartType;
    private Expression frameStartExpression;
    private final WindowFramePositionType frameEndType;
    private Expression frameEndExpression;
    private final WindowFrameExclusionType frameExclusionType;

    public WindowDefinition(String windowName, Predicate filterPredicate) {
        this.windowName = windowName;
        this.partitionExpressions = Collections.emptyList();
        this.filterPredicate = filterPredicate;
        this.orderByExpressions = Collections.emptyList();
        this.frameMode = null;
        this.frameStartType = null;
        this.frameStartExpression = null;
        this.frameEndType = null;
        this.frameEndExpression = null;
        this.frameExclusionType = null;
    }

    public WindowDefinition(String windowName, List<Expression> partitionExpressions, List<OrderByItem> orderByExpressions, Predicate filterPredicate,
                            WindowFrameMode frameMode, WindowFramePositionType frameStartType, Expression frameStartExpression, WindowFramePositionType frameEndType, Expression frameEndExpression, WindowFrameExclusionType frameExclusionType) {
        this.windowName = windowName;
        this.partitionExpressions = partitionExpressions;
        this.orderByExpressions = orderByExpressions;
        this.filterPredicate = filterPredicate;
        this.frameMode = frameMode;
        this.frameStartType = frameStartType;
        this.frameStartExpression = frameStartExpression;
        this.frameEndType = frameEndType;
        this.frameEndExpression = frameEndExpression;
        this.frameExclusionType = frameExclusionType;
    }

    public WindowDefinition copy() {
        int size = this.partitionExpressions.size();
        List<Expression> partitionExpressions = new ArrayList<>(size);
        List<Expression> expressions = this.partitionExpressions;
        for (int i = 0; i < size; i++) {
            partitionExpressions.add(expressions.get(i).copy());
        }

        size = this.orderByExpressions.size();
        List<OrderByItem> orderByExpressions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            orderByExpressions.add(this.orderByExpressions.get(i).copy());
        }

        Predicate filterPredicate = null;
        if (this.filterPredicate != null) {
            filterPredicate = this.filterPredicate.copy();
        }

        Expression frameStartExpression = null;
        if (this.frameStartExpression != null) {
            frameStartExpression = this.frameStartExpression.copy();
        }

        Expression frameEndExpression = null;
        if (this.frameEndExpression != null) {
            frameEndExpression = this.frameEndExpression.copy();
        }

        return new WindowDefinition(windowName, partitionExpressions, orderByExpressions, filterPredicate, frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression, frameExclusionType);
    }

    public String getWindowName() {
        return windowName;
    }

    public List<Expression> getPartitionExpressions() {
        return partitionExpressions;
    }

    public List<OrderByItem> getOrderByExpressions() {
        return orderByExpressions;
    }

    public Predicate getFilterPredicate() {
        return filterPredicate;
    }

    public void setFilterPredicate(Predicate filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    public WindowFrameMode getFrameMode() {
        return frameMode;
    }

    public WindowFramePositionType getFrameStartType() {
        return frameStartType;
    }

    public Expression getFrameStartExpression() {
        return frameStartExpression;
    }

    public void setFrameStartExpression(Expression frameStartExpression) {
        this.frameStartExpression = frameStartExpression;
    }

    public WindowFramePositionType getFrameEndType() {
        return frameEndType;
    }

    public Expression getFrameEndExpression() {
        return frameEndExpression;
    }

    public void setFrameEndExpression(Expression frameEndExpression) {
        this.frameEndExpression = frameEndExpression;
    }

    public WindowFrameExclusionType getFrameExclusionType() {
        return frameExclusionType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((windowName == null) ? 0 : windowName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WindowDefinition other = (WindowDefinition) obj;
        if (windowName == null) {
            if (other.windowName != null) {
                return false;
            }
        } else if (!windowName.equals(other.windowName)) {
            return false;
        }
        return true;
    }
}