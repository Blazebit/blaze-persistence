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

package com.blazebit.persistence.deltaspike.data.impl;

import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DeltaspikeExtensionDeactivator implements ClassDeactivator {
    @Override
    public Boolean isActivated(Class<? extends Deactivatable> aClass) {
        return !(aClass.equals(RepositoryExtension.class) || aClass.equals(PartialBeanBindingExtension.class));
    }
}