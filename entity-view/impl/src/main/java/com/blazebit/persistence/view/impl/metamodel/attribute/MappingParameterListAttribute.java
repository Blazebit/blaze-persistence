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

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.MappingAttribute;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MappingParameterListAttribute<X, Y> extends AbstractParameterListAttribute<X, Y> implements MappingAttribute<X, List<Y>> {

    public MappingParameterListAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(mappingConstructor, mapping, context);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }
}
