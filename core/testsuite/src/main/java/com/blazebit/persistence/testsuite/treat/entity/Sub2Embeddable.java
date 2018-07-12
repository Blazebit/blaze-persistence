
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

package com.blazebit.persistence.testsuite.treat.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Sub2Embeddable<T extends Base<T, ?>> {
    
    public Integer getSub2SomeValue();
    
    public void setSub2SomeValue(Integer sub2SomeValue);
    
    public T getSub2Parent();

    public void setSub2Parent(T sub2Parent);

    public List<? extends T> getSub2List();

    public void setSub2List(List<? extends T> sub2List);

    public Set<? extends T> getSub2Children();

    public void setSub2Children(Set<? extends T> sub2Children);

    public Map<? extends T, ? extends T> getSub2Map();

    public void setSub2Map(Map<? extends T, ? extends T> sub2Map);
    
}
