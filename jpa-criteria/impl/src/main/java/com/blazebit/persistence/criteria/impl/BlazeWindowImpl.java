/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeWindow;
import com.blazebit.persistence.criteria.BlazeWindowFrameEndType;
import com.blazebit.persistence.criteria.BlazeWindowFrameExclusion;
import com.blazebit.persistence.criteria.BlazeWindowFrameKind;
import com.blazebit.persistence.criteria.BlazeWindowFrameStartType;
import com.blazebit.persistence.criteria.BlazeWindowFrameMode;
import com.blazebit.persistence.parser.expression.WindowFrameExclusionType;
import com.blazebit.persistence.parser.expression.WindowFramePositionType;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class BlazeWindowImpl implements BlazeWindow {

    private List<BlazeOrder> orderList = Collections.emptyList();
    private List<BlazeExpression<?>> partitionList = Collections.emptyList();
    private BlazeWindowFrameMode frameMode = BlazeWindowFrameMode.ROWS;
    private WindowFramePositionType frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
    private BlazeExpression<?> frameStartExpression;
    private WindowFramePositionType frameEndType = WindowFramePositionType.CURRENT_ROW;
    private BlazeExpression<?> frameEndExpression;
    private WindowFrameExclusionType frameExclusion = WindowFrameExclusionType.EXCLUDE_NO_OTHERS;

    public BlazeWindowImpl() {
    }

    public List<BlazeOrder> getOrderList() {
        return orderList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeWindow orderBy(Order... orders) {
        if (orders == null || orders.length == 0) {
            this.orderList = Collections.EMPTY_LIST;
        } else {
            this.orderList = (List<BlazeOrder>) (List<?>) Arrays.asList(orders);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeWindow orderBy(List<Order> orderList) {
        this.orderList = (List<BlazeOrder>) (List<?>) orderList;
        return this;
    }

    @Override
    public List<BlazeExpression<?>> getPartitionList() {
        return partitionList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeWindow partitionBy(Expression<?>... groupings) {
        if (groupings == null || groupings.length == 0) {
            partitionList = Collections.EMPTY_LIST;
        } else {
            partitionList = (List<BlazeExpression<?>>) (List<?>) Arrays.asList(groupings);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeWindow partitionBy(List<Expression<?>> groupings) {
        partitionList = (List<BlazeExpression<?>>) (List<?>) groupings;
        return this;
    }

    @Override
    public BlazeWindow rows(BlazeWindowFrameStartType start) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow rows(Expression<Integer> start, BlazeWindowFrameKind startKind) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start, startKind);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow rowsBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow rowsBetween(BlazeWindowFrameStartType start, Expression<Integer> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindow rowsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start, startKind);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow rowsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, Expression<Integer> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.ROWS;
        setStart(start, startKind);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindow range(BlazeWindowFrameStartType start) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow range(Expression<?> start, BlazeWindowFrameKind startKind) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start, startKind);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow rangeBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow rangeBetween(BlazeWindowFrameStartType start, Expression<?> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindow rangeBetween(Expression<?> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start, startKind);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow rangeBetween(Expression<?> start, BlazeWindowFrameKind startKind, Expression<?> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.RANGE;
        setStart(start, startKind);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindow groups(BlazeWindowFrameStartType start) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow groups(Expression<Integer> start, BlazeWindowFrameKind startKind) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start, startKind);
        setEnd(BlazeWindowFrameEndType.CURRENT_ROW);
        return this;
    }

    @Override
    public BlazeWindow groupsBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow groupsBetween(BlazeWindowFrameStartType start, Expression<Integer> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindow groupsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start, startKind);
        setEnd(end);
        return this;
    }

    @Override
    public BlazeWindow groupsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, Expression<Integer> end, BlazeWindowFrameKind endKind) {
        this.frameMode = BlazeWindowFrameMode.GROUPS;
        setStart(start, startKind);
        setEnd(end, endKind);
        return this;
    }

    @Override
    public BlazeWindowFrameMode getFrameMode() {
        return frameMode;
    }

    @Override
    public BlazeExpression<?> getFrameStart() {
        return frameStartExpression;
    }

    @Override
    public BlazeWindowFrameKind getFrameStartKind() {
        return mapFrameKind(frameStartType);
    }

    @Override
    public BlazeWindowFrameStartType getFrameStartType() {
        switch (frameStartType) {
            case CURRENT_ROW:
                return BlazeWindowFrameStartType.CURRENT_ROW;
            case BOUNDED_PRECEDING:
            case BOUNDED_FOLLOWING:
            case UNBOUNDED_FOLLOWING:
                return null;
            case UNBOUNDED_PRECEDING:
                return BlazeWindowFrameStartType.UNBOUNDED_PRECEDING;
            default:
                throw new IllegalArgumentException("Unknown frame type: " + frameStartType);
        }
    }

    @Override
    public BlazeExpression<?> getFrameEnd() {
        return frameEndExpression;
    }

    @Override
    public BlazeWindowFrameKind getFrameEndKind() {
        return mapFrameKind(frameEndType);
    }

    @Override
    public BlazeWindowFrameEndType getFrameEndType() {
        switch (frameEndType) {
            case CURRENT_ROW:
                return BlazeWindowFrameEndType.CURRENT_ROW;
            case BOUNDED_PRECEDING:
            case BOUNDED_FOLLOWING:
            case UNBOUNDED_PRECEDING:
                return null;
            case UNBOUNDED_FOLLOWING:
                return BlazeWindowFrameEndType.UNBOUNDED_FOLLOWING;
            default:
                throw new IllegalArgumentException("Unknown frame type: " + frameEndType);
        }
    }

    @Override
    public BlazeWindowFrameExclusion getFrameExclusion() {
        switch (frameExclusion) {
            case EXCLUDE_TIES:
                return BlazeWindowFrameExclusion.TIES;
            case EXCLUDE_CURRENT_ROW:
                return BlazeWindowFrameExclusion.CURRENT_ROW;
            case EXCLUDE_GROUP:
                return BlazeWindowFrameExclusion.GROUP;
            case EXCLUDE_NO_OTHERS:
                return BlazeWindowFrameExclusion.NO_OTHERS;
            default:
                throw new IllegalArgumentException("Unknown frame exclusion: " + frameExclusion);
        }
    }

    @Override
    public BlazeWindow exclude(BlazeWindowFrameExclusion exclusion) {
        this.frameExclusion = mapExclusionType(exclusion);
        return this;
    }

    private void setStart(BlazeWindowFrameStartType start) {
        switch (start) {
            case CURRENT_ROW:
                this.frameStartType = WindowFramePositionType.CURRENT_ROW;
                break;
            case UNBOUNDED_PRECEDING:
                this.frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
                break;
            default:
                throw new IllegalArgumentException("Unknown frame start type: " + start);
        }
        this.frameStartExpression = null;
    }

    private void setStart(Expression<?> start, BlazeWindowFrameKind startKind) {
        switch (startKind) {
            case FOLLOWING:
                this.frameStartType = WindowFramePositionType.BOUNDED_FOLLOWING;
                break;
            case PRECEDING:
                this.frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
                break;
            default:
                throw new IllegalArgumentException("Unknown frame kind: " + startKind);
        }
        if (start == null) {
            throw new IllegalArgumentException("Start expression can't be null");
        }
        this.frameStartExpression = (BlazeExpression<?>) start;
    }

    private void setEnd(BlazeWindowFrameEndType end) {
        switch (end) {
            case CURRENT_ROW:
                this.frameEndType = WindowFramePositionType.CURRENT_ROW;
                break;
            case UNBOUNDED_FOLLOWING:
                this.frameEndType = WindowFramePositionType.UNBOUNDED_FOLLOWING;
                break;
            default:
                throw new IllegalArgumentException("Unknown frame end type: " + end);
        }
        this.frameEndExpression = null;
    }

    private void setEnd(Expression<?> end, BlazeWindowFrameKind endKind) {
        switch (endKind) {
            case FOLLOWING:
                this.frameEndType = WindowFramePositionType.BOUNDED_FOLLOWING;
                break;
            case PRECEDING:
                this.frameEndType = WindowFramePositionType.BOUNDED_PRECEDING;
                break;
            default:
                throw new IllegalArgumentException("Unknown frame kind: " + endKind);
        }
        if (end == null) {
            throw new IllegalArgumentException("End expression can't be null");
        }
        this.frameEndExpression = (BlazeExpression<?>) end;
    }

    private BlazeWindowFrameKind mapFrameKind(WindowFramePositionType framePositionType) {
        switch (framePositionType) {
            case CURRENT_ROW:
            case UNBOUNDED_FOLLOWING:
            case UNBOUNDED_PRECEDING:
                return null;
            case BOUNDED_PRECEDING:
                return BlazeWindowFrameKind.PRECEDING;
            case BOUNDED_FOLLOWING:
                return BlazeWindowFrameKind.FOLLOWING;
            default:
                throw new IllegalArgumentException("Unknown frame position: " + framePositionType);
        }
    }

    private WindowFrameExclusionType mapExclusionType(BlazeWindowFrameExclusion exclusion) {
        switch (exclusion) {
            case TIES:
                return WindowFrameExclusionType.EXCLUDE_TIES;
            case CURRENT_ROW:
                return WindowFrameExclusionType.EXCLUDE_CURRENT_ROW;
            case GROUP:
                return WindowFrameExclusionType.EXCLUDE_GROUP;
            case NO_OTHERS:
                return WindowFrameExclusionType.EXCLUDE_NO_OTHERS;
            default:
                throw new IllegalArgumentException("Unknown frame exclusion: " + exclusion);
        }
    }
}
