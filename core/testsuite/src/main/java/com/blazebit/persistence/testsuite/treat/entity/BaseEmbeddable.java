
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

public interface BaseEmbeddable<T extends Base<T, ?>> {
    
    public T getParent();

    public void setParent(T parent);

    public List<? extends T> getList();

    public void setList(List<? extends T> list);

    public Set<? extends T> getChildren();

    public void setChildren(Set<? extends T> children);

    public Map<? extends T, ? extends T> getMap();

    public void setMap(Map<? extends T, ? extends T> map);
    
}
