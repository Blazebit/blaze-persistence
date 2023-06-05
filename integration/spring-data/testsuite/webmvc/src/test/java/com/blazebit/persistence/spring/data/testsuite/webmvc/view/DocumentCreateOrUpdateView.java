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

package com.blazebit.persistence.spring.data.testsuite.webmvc.view;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Moritz Becker
 * @since 1.6.9
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Document.class)
public interface DocumentCreateOrUpdateView extends DocumentUpdateView {

    @JsonIgnore
    @IdMapping
    @Override
    Long getId();
}
