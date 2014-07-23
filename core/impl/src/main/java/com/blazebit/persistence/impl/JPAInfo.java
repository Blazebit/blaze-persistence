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

package com.blazebit.persistence.impl;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian
 */
public class JPAInfo {
    
        public final boolean isJPA21;
        public final boolean isHibernate;

        public JPAInfo(EntityManager em) {
            boolean jpa21 = false;
            boolean hibernate = false;
            
            try {
                em.getClass().getMethod("createEntityGraph", Class.class);
                jpa21 = true;
            } catch (NoSuchMethodException e) {
            }
            

            try {
                Class<?> sessionClass = Class.forName("org.hibernate.Session");
                Object o = em.unwrap(sessionClass);
                hibernate = o != null;
            } catch (ClassNotFoundException e) {
            }
            
            this.isJPA21 = jpa21;
            this.isHibernate = hibernate;
        }
}
