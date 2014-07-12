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

/**
 *
 * @author ccbem
 */
public class ModelUtils {

    /**
     *
     * @param path normalized, relative to root; field separation by '.'
     * @param root root entity class
     * @return true if the field referenced by path is joinable, i.e. if it is a
     * relation type.
     */
//    public static boolean isJoinablePath(String path, Class<?> root) {
//        String[] fieldNames = path.split("\\.");
//        Class<?> currentClass = root;
//
//        // traverse path
//        Field f = null;
//        for (int i = 0; i < fieldNames.length; i++) {
//            if ((f = ReflectionUtils.getField(currentClass, fieldNames[i])) == null) {
//                throw new IllegalStateException("Unresolved entity path " + path + " for root " + root);
//            }
//            currentClass = f.getType();
//        }
//
//        // check if last element is joinable
//        return isJoinable(f);
//    }

//    public static boolean isJoinable(Field f) {
//        Class[] joinableAnnotations = new Class[]{OneToMany.class, ManyToMany.class, OneToOne.class, ManyToOne.class, ElementCollection.class};
//        Set<Class<?>> fieldAndGetterAnnotations = new HashSet<Class<?>>();
//        Method getter = ReflectionUtils.getGetter(f.getDeclaringClass(), f.getName());
//
//        Annotation[] fieldAnnotations = f.getAnnotations();
//        Annotation[] getterAnnoations = getter.getDeclaredAnnotations();
//        for (Annotation a : fieldAnnotations) {
//            fieldAndGetterAnnotations.add(a.getClass());
//        }
//        for (Annotation a : getterAnnoations) {
//            fieldAndGetterAnnotations.addAll(Arrays.asList(a.getClass().getInterfaces()));
//        }
//
//        for (Class<?> joinableAnnotation : joinableAnnotations) {
//            if (fieldAndGetterAnnotations.contains(joinableAnnotation)) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    //TODO: what about JPA annotations? @Transient, @Embedded, etc

    public static boolean isJoinable(Class<?> fieldClass) {
        boolean fieldCollectionType = ReflectionUtils.isSubtype(fieldClass,
                Collection.class);

        // avoid call when not necessary
        boolean fieldMapType = fieldCollectionType ? false : ReflectionUtils
                .isSubtype(fieldClass, Map.class);

        if (!fieldCollectionType && !fieldMapType && (FormatUtils.isParseableType(fieldClass)
                || Blob.class.equals(fieldClass)
                || Clob.class.equals(fieldClass)
                || new byte[0].getClass().equals(fieldClass))) {
            return false;
        }
        return true;
    }

    public static Class<?> resolveFieldClass(Class<?> clazz, String propertyName) {

        Class<?> fieldClass = ReflectionUtils.getResolvedFieldType(clazz,
                propertyName);
        if (fieldClass == null) {
            throw new IllegalArgumentException("Could not find field '"
                    + propertyName + "' on class '" + clazz);
        }

        boolean fieldCollectionType = ReflectionUtils.isSubtype(fieldClass,
                Collection.class);

        // avoid call when not necessary
        boolean fieldMapType = fieldCollectionType ? false : ReflectionUtils
                .isSubtype(fieldClass, Map.class);

        // FIXED: add exceptions when type arguments are not sufficient
        if (fieldCollectionType) {
            Class<?>[] types = ReflectionUtils
                    .getResolvedFieldTypeArguments(clazz,
                            propertyName);

            if (types.length != 1) {
                throw new IllegalArgumentException(
                        "No type parameter given for collection type in class "
                        + clazz + " for field "
                        + propertyName);
            }

            fieldClass = types[0];
        } else if (fieldMapType) {
            Class<?>[] types = ReflectionUtils
                    .getResolvedFieldTypeArguments(clazz,
                            propertyName);

            if (types.length != 2) {
                throw new IllegalArgumentException(
                        "No type parameter given for map type in class "
                        + clazz + " for field "
                        + propertyName);
            }

            fieldClass = types[1];
        }

        // Fail if the field class can not be retrieved
        if (fieldClass == null) {
            throw new IllegalArgumentException("Field with name "
                    + propertyName + " was not found within class "
                    + clazz.getName());
        }

        return fieldClass;
    }
}
