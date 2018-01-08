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

package com.blazebit.persistence.view.testsuite.inheritance.nested.model;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(SingleTableBase.class)
@EntityViewInheritance
public interface SingleTableBaseView {
    
    @IdMapping
    public Long getId();

    @Mapping("name")
    String getName();

    // The key to reproduce issue 456 is to have a method in the base class
    // that is lexically ordered first
    @Mapping("name")
    String getA();
}
