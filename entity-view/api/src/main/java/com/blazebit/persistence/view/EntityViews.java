/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewConfigurationProvider;
import java.util.ServiceLoader;

/**
 *
 * @author Christian Beikov
 */
public class EntityViews {
    
    public static EntityViewConfiguration getDefault() {
        ServiceLoader<EntityViewConfigurationProvider> serviceLoader = ServiceLoader.load(EntityViewConfigurationProvider.class);
        
        for (EntityViewConfigurationProvider ext : serviceLoader) {
            return ext.createConfiguration();
        }
        
        throw new IllegalStateException("No EntityViewConfigurationProvider found on the class path. Please check if a valid implementation is on the class path.");
    }
}
