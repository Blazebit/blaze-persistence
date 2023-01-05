/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.parser.expression.WindowFrameMode;
import com.blazebit.persistence.parser.expression.WindowFramePositionType;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;

/**
 * {@code WindowRows} provides the building of the rows/range part of the window function expression.
 * Analog to {@link com.querydsl.sql.WindowRows}.
 *
 * @param <Def> Builder type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class WindowRows<Def extends WindowDefinition<Def, ?>> {

    private final WindowFrameMode frameMode;
    private WindowFramePositionType frameStartType;
    private Expression<?> frameStartExpression;
    private WindowFramePositionType frameEndType;
    private Expression<?> frameEndExpression;

    /**
     * An interface for building a window frame clause for analytics functions.
     *
     * @author Jan-Willem Gmelig Meyling
     */
    public class Between {

        /**
         * Uses UNBOUNDED PRECEDING as lower bound and continues to the frame exclusion builder.
         *
         * @return The frame exclusion builder
         */
        public BetweenAnd unboundedPreceding() {
            frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
            return new BetweenAnd();
        }

        /**
         * Uses CURRENT ROW as lower bound and continues to the frame exclusion builder.
         *
         * @return The frame exclusion builder
         */
        public BetweenAnd currentRow() {
            frameStartType = WindowFramePositionType.CURRENT_ROW;
            return new BetweenAnd();
        }

        /**
         * Uses expression PRECEDING as lower bound for the frame and starts a frame between builder for the upper bound.
         *
         * @param expr The expression for the frame bound
         * @return The frame between builder
         */
        public BetweenAnd preceding(Expression<Integer> expr) {
            frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
            frameStartExpression = expr;
            return new BetweenAnd();
        }

        /**
         * Uses expression PRECEDING as lower bound for the frame and starts a frame between builder for the upper bound.
         *
         * @param i The number of preceding rows
         * @return The frame between builder
         */
        public BetweenAnd preceding(int i) {
            return preceding(ConstantImpl.create(i));
        }

        /**
         * Uses expression FOLLOWING as lower bound for the frame and starts a frame between builder for the upper bound.
         *
         * @param expr The expression for the frame bound
         * @return The frame between builder
         */
        public BetweenAnd following(Expression<Integer> expr) {
            frameStartType = WindowFramePositionType.BOUNDED_FOLLOWING;
            frameStartExpression = expr;
            return new BetweenAnd();
        }

        /**
         * Uses expression FOLLOWING as lower bound for the frame and starts a frame between builder for the upper bound.
         *
         * @param i The number of following rows
         * @return The frame between builder
         */
        public BetweenAnd following(int i) {
            return following(ConstantImpl.create(i));
        }
    }

    /**
     * Intermediate step
     * @author Jan-Willem Gmelig Meyling
     */
    public class BetweenAnd {

        /**
         * Uses UNBOUNDED FOLLOWING as upper bound and continues to the frame exclusion builder.
         *
         * @return The frame exclusion builder
         */
        public Def unboundedFollowing() {
            frameEndType = WindowFramePositionType.UNBOUNDED_FOLLOWING;
            return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
        }

        /**
         * Uses CURRENT ROW as upper bound and continues to the frame exclusion builder.
         *
         * @return The frame exclusion builder
         */
        public Def currentRow() {
            frameEndType = WindowFramePositionType.CURRENT_ROW;
            return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
        }

        /**
         * Uses X PRECEDING as upper bound and continues to the frame exclusion builder.
         *
         * @param expr The expression for the frame bound
         * @return The frame exclusion builder
         */
        public Def preceding(Expression<Integer> expr) {
            frameEndType = WindowFramePositionType.BOUNDED_PRECEDING;
            frameEndExpression = expr;
            return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
        }

        /**
         * Uses X PRECEDING as upper bound and continues to the frame exclusion builder.
         *
         * @param i The expression for the frame bound
         * @return The frame exclusion builder
         */
        public Def preceding(int i) {
            return preceding(ConstantImpl.create(i));
        }

        /**
         * Uses X FOLLOWING as upper bound and continues to the frame exclusion builder.
         *
         * @param expr The expression for the frame bound
         * @return The frame exclusion builder
         */
        public Def following(Expression<Integer> expr) {
            frameEndType = WindowFramePositionType.BOUNDED_FOLLOWING;
            frameEndExpression = expr;
            return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
        }

        /**
         * Uses X FOLLOWING as upper bound and continues to the frame exclusion builder.
         *
         * @param i The expression for the frame bound
         * @return The frame exclusion builder
         */
        public Def following(int i) {
            return following(ConstantImpl.create(i));
        }
    }

    private final Def rv;

    public WindowRows(Def windowFunction, WindowFrameMode frameMode) {
        this.rv = windowFunction;
        this.frameMode = frameMode;
    }

    /**
     * Start a frame between builder.
     *
     * @return The frame between builder
     */
    public Between between() {
        return new Between();
    }

    /**
     * Uses UNBOUNDED PRECEDING as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @return The frame builder
     */
    public Def unboundedPreceding() {
        frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
        return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
    }

    /**
     * Uses CURRENT ROW as lower bound for the frame and starts a frame between builder for the upper bound.
     *
     * @return The frame builder
     */
    public Def currentRow() {
        frameStartType = WindowFramePositionType.CURRENT_ROW;
        return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
    }

    /**
     * Uses X PRECEDING as lower bound and continues to the frame exclusion builder.
     *
     * @param expr The expression for the frame bound
     * @return The frame exclusion builder
     */
    public Def preceding(Expression<Integer> expr) {
        frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
        frameStartExpression = expr;
        return rv.withFrame(frameMode, frameStartType, frameStartExpression, frameEndType, frameEndExpression);
    }

    /**
     * Uses X PRECEDING as lower bound and continues to the frame exclusion builder.
     *
     * @param i The number of preceding rows
     * @return The frame exclusion builder
     */
    public Def preceding(int i) {
        return preceding(ConstantImpl.create(i));
    }

}
