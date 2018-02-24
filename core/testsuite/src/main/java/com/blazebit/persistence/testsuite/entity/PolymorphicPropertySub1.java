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

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@AssociationOverrides({
    @AssociationOverride(name = "base", joinColumns = @JoinColumn(name = "base_sub_1"))
})
public class PolymorphicPropertySub1 extends PolymorphicPropertyMapBase<PolymorphicSub1> {
    private static final long serialVersionUID = 1L;

    public PolymorphicPropertySub1() {
    }
}
