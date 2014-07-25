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

package com.blazebit.persistence.spi;

import com.blazebit.persistence.CriteriaBuilderFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 */
public interface CriteriaBuilderConfiguration {
    
    public <T> void registerObjectBuilder(Class<T> clazz, ObjectBuilderFactory<T> factory);
    
    public Map<Class<?>, ObjectBuilderFactory<?>> getObjectBuilders();
    
    public void registerQueryTransformer(QueryTransformer clazz);
    
    public List<QueryTransformer> getQueryTransformers();
    
    public CriteriaBuilderFactory createCriteriaBuilderFactory();
}
