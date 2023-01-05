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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeWindow;
import com.blazebit.persistence.criteria.BlazeWindowFrameEndType;
import com.blazebit.persistence.criteria.BlazeWindowFrameExclusion;
import com.blazebit.persistence.criteria.BlazeWindowFrameKind;
import com.blazebit.persistence.criteria.BlazeWindowFrameMode;
import com.blazebit.persistence.criteria.BlazeWindowFrameStartType;
import com.blazebit.persistence.criteria.BlazeWindowFunctionExpression;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import javax.persistence.criteria.Expression;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class WindowFunctionExpressionImpl<X> extends FunctionExpressionImpl<X> implements BlazeWindowFunctionExpression<X> {

    private static final long serialVersionUID = 1L;

    private BlazeWindow window;

    public WindowFunctionExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, functionName, argumentExpressions);
    }

    @Override
    public BlazeWindow getWindow() {
        return window;
    }

    @Override
    public BlazeWindowFunctionExpression<X> window(BlazeWindow window) {
        this.window = window;
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        super.visitParameters(visitor);
        if (window != null) {
            for (BlazeOrder blazeOrder : window.getOrderList()) {
                ((AbstractSelection<?>) blazeOrder.getExpression()).visitParameters(visitor);
            }
            for (BlazeExpression<?> blazeExpression : window.getPartitionList()) {
                ((AbstractSelection<?>) blazeExpression).visitParameters(visitor);
            }
            if (window.getFrameStart() != null) {
                ((AbstractSelection<?>) window.getFrameStart()).visitParameters(visitor);
            }
            if (window.getFrameEnd() != null) {
                ((AbstractSelection<?>) window.getFrameEnd()).visitParameters(visitor);
            }
        }
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        List<Expression<?>> args = getArgumentExpressions();
        buffer.append(getFunctionName()).append('(');
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                buffer.append(',');
            }

            context.apply(args.get(i));
        }
        buffer.append(')');
        renderWindow(context);
    }

    protected void renderWindow(RenderContext context) {
        if (window != null) {
            List<BlazeExpression<?>> partitionList = window.getPartitionList();
            List<BlazeOrder> orderList = window.getOrderList();
            BlazeWindowFrameMode frameMode = window.getFrameMode();
            BlazeWindowFrameStartType frameStartType = window.getFrameStartType();
            BlazeWindowFrameEndType frameEndType = window.getFrameEndType();
            BlazeWindowFrameExclusion frameExclusion = window.getFrameExclusion();
            boolean hasPartition = !partitionList.isEmpty();
            boolean hasOrder = !orderList.isEmpty();
            boolean hasFrameEnd = frameEndType != BlazeWindowFrameEndType.CURRENT_ROW;
            boolean hasFrame = frameMode != BlazeWindowFrameMode.ROWS || frameStartType != BlazeWindowFrameStartType.UNBOUNDED_PRECEDING || hasFrameEnd || frameExclusion != BlazeWindowFrameExclusion.NO_OTHERS;
            if (hasPartition || hasOrder || hasFrame) {
                final StringBuilder buffer = context.getBuffer();
                buffer.append(" OVER (");
                boolean needsSpace = false;
                if (hasPartition) {
                    buffer.append("PARTITION BY ");
                    for (BlazeExpression<?> blazeExpression : partitionList) {
                        ((AbstractExpression<?>) blazeExpression).render(context);
                        buffer.append(", ");
                    }
                    buffer.setLength(buffer.length() - 2);
                    needsSpace = true;
                }
                if (hasOrder) {
                    if (needsSpace) {
                        buffer.append(' ');
                    }
                    buffer.append("ORDER BY ");
                    for (BlazeOrder blazeOrder : orderList) {
                        ((AbstractExpression<?>) blazeOrder.getExpression()).render(context);
                        if (blazeOrder.isAscending()) {
                            buffer.append(" ASC");
                        } else {
                            buffer.append(" DESC");
                        }
                        if (blazeOrder.isNullsFirst()) {
                            buffer.append(" NULLS FIRST");
                        } else {
                            buffer.append(" NULLS LAST");
                        }
                        buffer.append(", ");

                    }
                    buffer.setLength(buffer.length() - 2);
                    needsSpace = true;
                }
                if (hasFrame) {
                    if (needsSpace) {
                        buffer.append(' ');
                    }
                    buffer.append(frameMode.name());
                    if (hasFrameEnd) {
                        buffer.append(" BETWEEN ");
                    }
                    if (frameStartType == null) {
                        ((AbstractExpression<?>) window.getFrameStart()).render(context);
                        if (window.getFrameStartKind() == BlazeWindowFrameKind.PRECEDING) {
                            buffer.append(" PRECEDING");
                        } else {
                            buffer.append(" FOLLOWING");
                        }
                    } else {
                        switch (frameStartType) {
                            case CURRENT_ROW:
                                buffer.append("CURRENT ROW");
                                break;
                            case UNBOUNDED_PRECEDING:
                                buffer.append("UNBOUNDED PRECEDING");
                                break;
                            default:
                                throw new IllegalStateException("Unsupported frame type: " + frameStartType);
                        }
                    }
                    if (hasFrameEnd) {
                        buffer.append(" AND ");
                        if (frameEndType == null) {
                            ((AbstractExpression<?>) window.getFrameEnd()).render(context);
                            if (window.getFrameEndKind() == BlazeWindowFrameKind.PRECEDING) {
                                buffer.append(" PRECEDING");
                            } else {
                                buffer.append(" FOLLOWING");
                            }
                        } else if (frameEndType == BlazeWindowFrameEndType.UNBOUNDED_FOLLOWING) {
                            buffer.append("UNBOUNDED FOLLOWING");
                        }
                    }
                    switch (frameExclusion) {
                        case CURRENT_ROW:
                            buffer.append(" EXCLUDE CURRENT ROW");
                            break;
                        case GROUP:
                            buffer.append(" EXCLUDE GROUP");
                            break;
                        case TIES:
                            buffer.append(" EXCLUDE TIES");
                            break;
                        case NO_OTHERS:
                            break;
                        default:
                            throw new IllegalStateException("Unsupported frame exclusion: " + frameExclusion);
                    }
                }

                buffer.append(')');
            }
        }
    }
}
