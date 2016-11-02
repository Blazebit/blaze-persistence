/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.impl.update;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class DirtyCheckingListener {

    @PrePersist
    public void prePersist(Object o) {
        System.out.println("PrePersist: " + o);
    }
    
    @PostPersist
    public void postPersist(Object o) {
        System.out.println("PostPersist: " + o);
    }
    
    @PreUpdate
    public void preUpdate(Object o) {
        System.out.println("PreUpdate: " + o);
    }
    
    @PreUpdate
    public void postUpdate(Object o) {
        System.out.println("PostUpdate: " + o);
    }
    
    @PreRemove
    public void preRemove(Object o) {
        System.out.println("PreRemove: " + o);
    }
    
    @PostRemove
    public void postRemove(Object o) {
        System.out.println("PostRemove: " + o);
    }
    
}
