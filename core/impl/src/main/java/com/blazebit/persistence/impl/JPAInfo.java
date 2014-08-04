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
    public final boolean isEclipseLink24;

    public JPAInfo(EntityManager em) {
        boolean jpa21 = false;
        boolean hibernate = false;
        boolean eclipseLink24 = false;

        try {
            em.getClass()
                .getMethod("createEntityGraph", Class.class);
            jpa21 = true;
        } catch (NoSuchMethodException e) {
        }

        try {
            Class<?> sessionClass = Class.forName("org.hibernate.Session");
            Object o = em.unwrap(sessionClass);
            hibernate = o != null;
        } catch (ClassNotFoundException e) {
        }

        try {
            Class<?> jpaEMClass = Class.forName("org.eclipse.persistence.jpa.JpaEntityManager");
            Object o = em.unwrap(jpaEMClass);
            if (o != null) {
                Class<?> versionClass = Class.forName("org.eclipse.persistence.Version");
                String version = (String) versionClass.getMethod("getVersion")
                    .invoke(null);
                String[] versionParts = version.split("\\.");
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);

                eclipseLink24 = major > 2 || (major == 2 && minor >= 4);
            }
        } catch (Exception e) {
        }

        this.isJPA21 = jpa21;
        this.isHibernate = hibernate;
        this.isEclipseLink24 = eclipseLink24;
    }

    public String getOnClause() {
        if (isJPA21 || isEclipseLink24) {
            return "ON";
        } else if (isHibernate) {
            return "WITH";
        } else {
            throw new UnsupportedOperationException("Unsupported JPA provider");
        }
    }
    
    public String getCollectionValueFunction(){
        if (isEclipseLink24) {
            return "VALUE";
        }
        return null;
    }
}
