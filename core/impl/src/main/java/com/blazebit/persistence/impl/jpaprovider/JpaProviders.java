/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.jpaprovider;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class JpaProviders {
    
    public static JpaProvider resolveJpaProvider(EntityManager em) {
        boolean jpa21 = false;

        try {
            EntityManager.class
                .getMethod("createEntityGraph", Class.class);
            jpa21 = true;
        } catch (NoSuchMethodException e) {
        }

        try {
            Class<?> sessionClass = Class.forName("org.hibernate.Session");
            if (em == null || em.unwrap(sessionClass) != null) {
                if (jpa21) {
                    return new HibernateJpa21Provider();
                } else {
                    return new HibernateJpaProvider();
                }
            }
        } catch (ClassNotFoundException e) {
        }

        try {
            Class<?> jpaEMClass = Class.forName("org.eclipse.persistence.jpa.JpaEntityManager");
            if (em == null || em.unwrap(jpaEMClass) != null) {
                Class<?> versionClass = Class.forName("org.eclipse.persistence.Version");
                String version = (String) versionClass.getMethod("getVersion")
                    .invoke(null);
                String[] versionParts = version.split("\\.");
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);

                boolean eclipseLink24 = major > 2 || (major == 2 && minor >= 4);
                
                if (!eclipseLink24) {
                    throw new IllegalArgumentException("Unsupported EclipseLink version " + version + "!");
                }
                
                return new EclipseLinkJpaProvider();
            }
        } catch (Exception e) {
        }

        try {
            Class<?> jpaEMClass = Class.forName("org.datanucleus.ExecutionContext");
            if (em == null || em.unwrap(jpaEMClass) != null) {
                return new DataNucleusJpaProvider();
            }
        } catch (Exception e) {
        }
        
        throw new IllegalArgumentException("Unsupported jpa provider!");
    }
}
