/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import java.util.List;

/**
 *
 * Defines a window to use for a window function.
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeWindow {

    /**
     * Sets the order by items for this window.
     *
     * @param orderByItems order by items
     * @return <code>this</code> for method chaining
     */
    BlazeWindow orderBy(Order... orderByItems);

    /**
     * Sets the order by items for this window.
     *
     * @param orderByItems order by items
     * @return <code>this</code> for method chaining
     */
    BlazeWindow orderBy(List<Order> orderByItems);

    /**
     * Returns the order by items.
     *
     * @return the order by items
     */
    List<BlazeOrder> getOrderList();

    /**
     * Sets the partition by items for this window.
     *
     * @param expressions The partition by expressions
     * @return <code>this</code> for method chaining
     */
    BlazeWindow partitionBy(Expression<?>... expressions);

    /**
     * Sets the partition by items for this window.
     *
     * @param expressions The partition by expressions
     * @return <code>this</code> for method chaining
     */
    BlazeWindow partitionBy(List<Expression<?>> expressions);

    /**
     * Returns the partition by items.
     *
     * @return the partition by items
     */
    List<BlazeExpression<?>> getPartitionList();

    /**
     * Sets the window frame to the ROWS mode with the given start and CURRENT ROW as end type.
     *
     * @param start The frame start type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rows(BlazeWindowFrameStartType start);

    /**
     * Sets the window frame to the ROWS mode with the given start and CURRENT ROW as end type.
     *
     * @param start The amount of rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rows(Expression<Integer> start, BlazeWindowFrameKind startKind);

    /**
     * Sets the window frame to the ROWS mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rowsBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the ROWS mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The amount of rows preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rowsBetween(BlazeWindowFrameStartType start, Expression<Integer> end, BlazeWindowFrameKind endKind);

    /**
     * Sets the window frame to the ROWS mode with the given start and end.
     *
     * @param start The amount of rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rowsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the ROWS mode with the given start and end.
     *
     * @param start The amount of rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @param end The amount of rows preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rowsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, Expression<Integer> end, BlazeWindowFrameKind endKind);

    /**
     * Sets the window frame to the RANGE mode with the given start and CURRENT ROW as end type.
     *
     * @param start The frame start type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow range(BlazeWindowFrameStartType start);

    /**
     * Sets the window frame to the RANGE mode with the given start and CURRENT ROW as end type.
     *
     * @param start The maximum difference of the value of the current row and the rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow range(Expression<?> start, BlazeWindowFrameKind startKind);

    /**
     * Sets the window frame to the RANGE mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rangeBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the RANGE mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The maximum difference of the value of the current row and the rows preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rangeBetween(BlazeWindowFrameStartType start, Expression<?> end, BlazeWindowFrameKind endKind);

    /**
     * Sets the window frame to the RANGE mode with the given start and end.
     *
     * @param start The maximum difference of the value of the current row and the rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rangeBetween(Expression<?> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the RANGE mode with the given start and end.
     *
     * @param start The maximum difference of the value of the current row and the rows preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current row
     * @param end The maximum difference of the value of the current row and the rows preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current row
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow rangeBetween(Expression<?> start, BlazeWindowFrameKind startKind, Expression<?> end, BlazeWindowFrameKind endKind);

    /**
     * Sets the window frame to the GROUPS mode with the given start and CURRENT ROW as end type.
     *
     * @param start The frame start type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groups(BlazeWindowFrameStartType start);

    /**
     * Sets the window frame to the GROUPS mode with the given start and CURRENT ROW as end type.
     *
     * @param start The amount of groups preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current rows group
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groups(Expression<Integer> start, BlazeWindowFrameKind startKind);

    /**
     * Sets the window frame to the GROUPS mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groupsBetween(BlazeWindowFrameStartType start, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the GROUPS mode with the given start and end.
     *
     * @param start The frame start type
     * @param end The amount of groups preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current rows group
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groupsBetween(BlazeWindowFrameStartType start, Expression<Integer> end, BlazeWindowFrameKind endKind);

    /**
     * Sets the window frame to the GROUPS mode with the given start and end.
     *
     * @param start The amount of groups preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current rows group
     * @param end The frame end type
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groupsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, BlazeWindowFrameEndType end);

    /**
     * Sets the window frame to the GROUPS mode with the given start and end.
     *
     * @param start The amount of groups preceding or following to use for the start of the frame
     * @param startKind Whether the frame start should be preceding or following the current rows group
     * @param end The amount of groups preceding or following to use for the end of the frame
     * @param endKind Whether the frame end should be preceding or following the current rows group
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow groupsBetween(Expression<Integer> start, BlazeWindowFrameKind startKind, Expression<Integer> end, BlazeWindowFrameKind endKind);

    /**
     * Returns the frame mode used for this window.
     *
     * @return the frame mode
     */
    public BlazeWindowFrameMode getFrameMode();

    /**
     * Returns the frame start expression used for this window, or null if it uses a special frame start type.
     *
     * @return the frame start expression, or null
     */
    public BlazeExpression<?> getFrameStart();

    /**
     * Returns the frame start kind used for this window, or null if it uses a special frame start type.
     *
     * @return the frame start kind, or null
     */
    public BlazeWindowFrameKind getFrameStartKind();

    /**
     * Returns the frame start type used for this window.
     *
     * @return the frame start type
     */
    public BlazeWindowFrameStartType getFrameStartType();

    /**
     * Returns the frame end expression used for this window, or null if it uses a special frame end type.
     *
     * @return the frame end expression, or null
     */
    public BlazeExpression<?> getFrameEnd();

    /**
     * Returns the frame end kind used for this window, or null if it uses a special frame end type.
     *
     * @return the frame end kind, or null
     */
    public BlazeWindowFrameKind getFrameEndKind();

    /**
     * Returns the frame end type used for this window.
     *
     * @return the frame end type
     */
    public BlazeWindowFrameEndType getFrameEndType();

    /**
     * Sets the window frame exclusion for this window.
     *
     * @param exclusion The frame exclusion
     * @return <code>this</code> for method chaining
     */
    public BlazeWindow exclude(BlazeWindowFrameExclusion exclusion);

    /**
     * Returns the frame exclusion for this window.
     *
     * @return the frame exclusion
     */
    public BlazeWindowFrameExclusion getFrameExclusion();

}
