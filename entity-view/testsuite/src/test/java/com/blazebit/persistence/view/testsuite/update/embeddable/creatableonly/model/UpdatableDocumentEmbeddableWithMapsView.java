/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.embeddable.creatableonly.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.embeddable.model.UpdatableDocumentEmbeddableWithMapsViewBase;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdatableDocumentEmbeddableWithMapsView extends UpdatableDocumentEmbeddableWithMapsViewBase {

    @UpdatableMapping(cascade = { CascadeType.PERSIST })
    public Map<String, NameObject> getNameMap();

    public void setNameMap(Map<String, NameObject> nameMap);

}
