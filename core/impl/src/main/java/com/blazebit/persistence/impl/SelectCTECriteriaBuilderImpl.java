/*
 * Copyright 2014 - 2024 Blazebit.
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
