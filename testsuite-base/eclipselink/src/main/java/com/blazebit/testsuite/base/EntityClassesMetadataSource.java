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

package com.blazebit.testsuite.base;

import java.io.StringReader;
import java.util.Map;
import org.eclipse.persistence.internal.jpa.metadata.xml.XMLEntityMappings;
import org.eclipse.persistence.internal.jpa.metadata.xml.XMLEntityMappingsReader;
import org.eclipse.persistence.jpa.metadata.XMLMetadataSource;
import org.eclipse.persistence.logging.SessionLog;

/**
 *
 * @author Christian
 */
public class EntityClassesMetadataSource extends XMLMetadataSource {

    @Override
    public XMLEntityMappings getEntityMappings(Map<String, Object> properties, ClassLoader classLoader, SessionLog sl) {
        Class<?>[] entityClasses = (Class<?>[]) properties.get("eclipselink.metadata-source.entity-classes");
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<entity-mappings version=\"2.3\"\n");
        sb.append("    xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/orm\"\n");
        sb.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("    xsi:schemaLocation=\"http://www.eclipse.org/eclipselink/xsds/persistence/orm http://www.eclipse.org/eclipselink/xsds/eclipselink_orm_2_3.xsd\">\n");

        for (Class<?> entityClass : entityClasses) {
            sb.append("<entity class=\"").append(entityClass.getName()).append("\"/>\n");
        }
        
        sb.append("</entity-mappings>");
        
        return XMLEntityMappingsReader.read(getRepositoryName(), new StringReader(sb.toString()), classLoader, properties);
    }
    
}
