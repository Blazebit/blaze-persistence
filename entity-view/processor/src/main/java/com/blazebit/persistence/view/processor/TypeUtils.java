/*
 * Copyright 2014 - 2021 Blazebit.
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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class TypeUtils {

    private static final Map<TypeKind, String> PRIMITIVE_WRAPPERS = new HashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(TypeKind.CHAR, "java.lang.Character");

        PRIMITIVE_WRAPPERS.put(TypeKind.BYTE, "java.lang.Byte");
        PRIMITIVE_WRAPPERS.put(TypeKind.SHORT, "java.lang.Short");
        PRIMITIVE_WRAPPERS.put(TypeKind.INT, "java.lang.Integer");
        PRIMITIVE_WRAPPERS.put(TypeKind.LONG, "java.lang.Long");

        PRIMITIVE_WRAPPERS.put(TypeKind.BOOLEAN, "java.lang.Boolean");

        PRIMITIVE_WRAPPERS.put(TypeKind.FLOAT, "java.lang.Float");
        PRIMITIVE_WRAPPERS.put(TypeKind.DOUBLE, "java.lang.Double");
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

        Collection<String> annotationClassNames = new HashSet<>();
        Collections.addAll(annotationClassNames, annotations);

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    public static List<AnnotationMirror> getAnnotationMirrors(Element element, String... annotations) {
        assert element != null;
        assert annotations != null;

        Collection<String> annotationClassNames = new HashSet<>();
        Collections.addAll(annotationClassNames, annotations);

        List<AnnotationMirror> found = new ArrayList<>(annotations.length);
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
                found.add(mirror);
            }
        }
        return found;
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

    public static String getSimpleTypeName(TypeElement element) {
        Element parent = element.getEnclosingElement();
        if (parent.getKind() == ElementKind.PACKAGE) {
            return element.getSimpleName().toString();
        }
        StringBuilder sb = new StringBuilder();
        while (parent.getKind() != ElementKind.PACKAGE) {
            sb.insert(0, parent.getSimpleName().toString());
            parent = parent.getEnclosingElement();
        }
        sb.append(element.getSimpleName().toString());
        return sb.toString();
    }

    public static String getDerivedTypeName(TypeElement element) {
        Element parent = element.getEnclosingElement();
        if (parent.getKind() == ElementKind.PACKAGE) {
            return element.getQualifiedName().toString();
        }
        StringBuilder sb = new StringBuilder();
        while (parent.getKind() != ElementKind.PACKAGE) {
            sb.insert(0, parent.getSimpleName().toString());
            parent = parent.getEnclosingElement();
        }
        sb.insert(0, parent.toString() + ".");
        sb.append(element.getSimpleName().toString());
        return sb.toString();
    }

    public static Collection<Element> getAllMembers(TypeElement element, Context context) {
        List<TypeMirror> superClasses = new ArrayList<>();
        superClasses.add(element.asType());
        Map<String, Element> members = new TreeMap<>();
        for (int i = 0; i < superClasses.size(); i++) {
            TypeMirror superClass = superClasses.get(i);
            final Element superClassElement = ((DeclaredType) superClass).asElement();
            for (Element enclosedElement : superClassElement.getEnclosedElements()) {
                String name = enclosedElement.getSimpleName().toString();
                if ("<init>".equals(name)) {
                    if (element == superClassElement) {
                        name = enclosedElement.toString();
                    } else {
                        continue;
                    }
                }
                Element old = members.put(name, enclosedElement);
                if (old != null && context.getTypeUtils().isAssignable(old.getEnclosingElement().asType(), superClass)) {
                    members.put(name, old);
                }
            }

            superClass = ((TypeElement) superClassElement).getSuperclass();
            if (superClass.getKind() == TypeKind.DECLARED) {
                superClasses.add(superClass);
            }
            superClasses.addAll(((TypeElement) superClassElement).getInterfaces());
        }
        return members.values();
    }

    public static <T> T getAnnotationValue(AnnotationMirror mirror, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
            if (name.equals(entry.getKey().getSimpleName().toString())) {
                return (T) entry.getValue().getValue();
            }
        }
        return null;
    }

    public static String getType(Element element, Context context) {
        DeclaredType entityDeclaredType = (DeclaredType) element.asType();
        TypeElement returnedElement = (TypeElement) context.getTypeUtils().asElement(entityDeclaredType);
        return returnedElement.getQualifiedName().toString();
    }

    public static String getRealType(Element element, Context context) {
        DeclaredType entityDeclaredType = (DeclaredType) element.asType();
        if (element instanceof ExecutableElement) {
            return toTypeString(entityDeclaredType, ((ExecutableElement) element).getReturnType(), context);
        } else {
            return toTypeString(entityDeclaredType,element.asType(), context);
        }
    }

    public static void forEachSuperType(TypeElement element, TypeVisitor visitor) {
        visitor.visit(element);
        for (TypeMirror interfaceMirror : element.getInterfaces()) {
            forEachSuperType((TypeElement) ((DeclaredType) interfaceMirror).asElement(), visitor);
        }
        forEachSuperType((TypeElement) ((DeclaredType) element.getSuperclass()).asElement(), visitor);
    }

    public static String getPackageName(Element element) {
        Element parent = element.getEnclosingElement();
        while (parent.getKind() != ElementKind.PACKAGE) {
            parent = parent.getEnclosingElement();
        }
        return parent.toString();
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    public static interface TypeVisitor {
        void visit(TypeElement element);
    }
}
