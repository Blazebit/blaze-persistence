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

import com.blazebit.reflection.ReflectionUtils;
import com.blazebit.text.FormatUtils;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Collection;
import java.util.Map;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType;

/**
 *
 * @author ccbem
 */
public class ModelUtils {

    public static boolean isJoinable(Attribute attr) {
        return attr.isCollection() 
                || attr.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE 
                || attr.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE;
    }

    public static Class<?> resolveFieldClass(Attribute attr) {
        if (attr.isCollection()) {
            return ((PluralAttribute) attr).getElementType().getJavaType();
        }
        
        return attr.getJavaType();
    }
}
