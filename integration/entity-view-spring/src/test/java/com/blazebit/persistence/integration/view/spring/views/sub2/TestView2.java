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
package com.blazebit.persistence.integration.view.spring.views.sub2;

import com.blazebit.persistence.integration.view.spring.entity.TestEntity;
import com.blazebit.persistence.integration.view.spring.qualifier.TestEntityViewQualifier;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@TestEntityViewQualifier
@EntityView(TestEntity.class)
public interface TestView2 {

    @IdMapping
    public String getId();

    @Mapping("id + 2")
    public String getIdPlusTwo();

}
