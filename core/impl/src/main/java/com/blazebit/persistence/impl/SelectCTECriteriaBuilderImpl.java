/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.List;
import java.util.Map;

/**
 *
 * @param <Y> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class SelectCTECriteriaBuilderImpl<Y> extends AbstractCTECriteriaBuilder<Y, SelectCTECriteriaBuilder<Y>, Void, Void> implements SelectCTECriteriaBuilder<Y> {

    public SelectCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, CTEManager.CTEKey cteKey, Class<Object> clazz, Y result, CTEBuilderListener listener, boolean emulateJoins) {
        super(mainQuery, queryContext, cteKey, false, clazz, result, listener, null, null, null);
        joinManager.setEmulateJoins(emulateJoins);
    }

    public SelectCTECriteriaBuilderImpl(AbstractCTECriteriaBuilder<Y, SelectCTECriteriaBuilder<Y>, Void, Void> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    SelectCTECriteriaBuilderImpl<Y> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new SelectCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    public Y end() {
        listener.onBuilderEnded(this);
        return result;
    }

    public CTEInfo createCTEInfo() {
        List<String> attributes = prepareAndGetAttributes();
        List<String> columns = prepareAndGetColumnNames();
        CTEInfo info = new CTEInfo(cteKey.getName(), cteKey.getOwner(), inline, cteType, attributes, columns, false, false, this, null);
        return info;
    }

}
