/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class TypeUtils {

    private static final Map<TypeKind, String> PRIMITIVE_WRAPPERS = new HashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(TypeKind.CHAR, "Character");

        PRIMITIVE_WRAPPERS.put(TypeKind.BYTE, "Byte");
        PRIMITIVE_WRAPPERS.put(TypeKind.SHORT, "Short");
        PRIMITIVE_WRAPPERS.put(TypeKind.INT, "Integer");
        PRIMITIVE_WRAPPERS.put(TypeKind.LONG, "Long");

        PRIMITIVE_WRAPPERS.put(TypeKind.BOOLEAN, "Boolean");

        PRIMITIVE_WRAPPERS.put(TypeKind.FLOAT, "Float");
        PRIMITIVE_WRAPPERS.put(TypeKind.DOUBLE, "Double");
    }

    private TypeUtils() {
    }

    public static String toWrapperTypeString(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return PRIMITIVE_WRAPPERS.get(type.getKind());
        }
        return type.toString();
    }

    public static String extractClosestRealTypeAsString(TypeMirror type, Context context) {
        if (type instanceof TypeVariable) {
            final TypeMirror compositeUpperBound = ((TypeVariable) type).getUpperBound();
            return extractClosestRealTypeAsString(compositeUpperBound, context);
        } else {
            final TypeMirror erasureType = context.getTypeUtils().erasure(type);
            if (TypeKind.ARRAY.equals(erasureType.getKind())) {
                return erasureType.toString();
            } else {
                return ((TypeElement) context.getTypeUtils().asElement(erasureType)).getQualifiedName().toString();
            }
        }
    }

    public static String toTypeString(DeclaredType declaredType, TypeMirror typeMirror, Context context) {
        if (typeMirror instanceof TypeVariable) {
            typeMirror = context.getTypeUtils().asMemberOf(declaredType, ((TypeVariable) typeMirror).asElement());
        }
        return typeMirror.toString();
    }

    public static boolean containsAnnotation(Element element, String... annotations) {
        assert element != null;
        assert annotations != null;

        List<String> annotationClassNames = new ArrayList<>();
        Collections.addAll(annotationClassNames, annotations);

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    public static AnnotationMirror getAnnotationMirror(Element element, String fqcn) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(fqcn)) {
                return am;
            }
        }
        return null;
    }

    public static String getDefaultValue(TypeKind kind) {
        switch (kind) {
            case INT:
                return "0";
            case BOOLEAN:
                return "false";
            case BYTE:
                return "(byte) 0";
            case CHAR:
                return "'\\0'";
            case DOUBLE:
                return "0D";
            case FLOAT:
                return "0F";
            case LONG:
                return "0L";
            case SHORT:
                return "(short) 0";
            default:
                return "null";
        }
    }
}
