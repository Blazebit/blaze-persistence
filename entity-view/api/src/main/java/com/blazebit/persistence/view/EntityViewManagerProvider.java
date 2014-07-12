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

import java.lang.reflect.Method;
import java.util.ServiceLoader;
import javax.enterprise.inject.spi.Extension;
import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 */
public class EntityViewManagerProvider {
    
    // TODO: This has to be refactord
    public static EntityViewManagerFactory getDefault() {
        ServiceLoader<Extension> serviceLoader = ServiceLoader.load(Extension.class);
        
        for (Extension ext : serviceLoader) {
            try {
                Method m = ext.getClass().getDeclaredMethod("getEntityViewManagerFactory");
                if (m != null && EntityViewManagerFactory.class.isAssignableFrom(m.getReturnType())) {
                    return EntityViewManagerFactory.class.cast(m.invoke(null));
                }
            } catch (Exception ex) {
                throw new RuntimeException("Invalid EntityViewProvider!", ex);
            }
        }
        
        throw new IllegalStateException("No EntityViewProvider found on the class path. Please check if a valid implementation is on the class path.");
    }
    
    public static EntityViewManager from(EntityManager em) {
        return getDefault().createEntityViewManager(em);
    }
}
