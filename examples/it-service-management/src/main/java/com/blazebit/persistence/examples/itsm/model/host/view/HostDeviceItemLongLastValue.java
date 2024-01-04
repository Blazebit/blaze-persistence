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

package com.blazebit.persistence.examples.itsm.model.host.view;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.examples.itsm.model.host.entity.HostDeviceItemLong;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(HostDeviceItemLong.class)
public interface HostDeviceItemLongLastValue extends HostDeviceItemDetail {

    @MappingSubquery(LastValueProvider.class)
    Long getLastValue();

    class LastValueProvider implements SubqueryProvider {

        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            // @formatter:off
            return subqueryInitiator
                    .from("embedding_view(values)", "v")
                    .select("v")
                    .orderByDesc("key(v)")
                    .setMaxResults(1)
                    .end();
            // @formatter:on
        }

    }

}
