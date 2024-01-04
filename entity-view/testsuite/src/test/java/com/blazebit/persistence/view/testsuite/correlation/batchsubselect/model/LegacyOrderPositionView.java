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

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@EntityView(LegacyOrderPosition.class)
public interface LegacyOrderPositionView extends IdHolderView<LegacyOrderPositionView.Id> {

    String getArticleNumber();

    @BatchFetch(size = 10)
    @Mapping(fetch = FetchStrategy.SELECT)
    Set<LegacyOrderPositionElementView> getElems();

    @EntityView(LegacyOrderPositionId.class)
    interface Id {

        Long getOrderId();
        void setOrderId(Long orderId);

        Integer getPositionId();
        void setPositionId(Integer positionId);
    }
}
