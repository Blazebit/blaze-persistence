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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (type.getKind() == TypeKind.TYPEVAR) {
            final TypeMirror compositeUpperBound = ((TypeVariable) type).getUpperBound();
            return extractClosestRealTypeAsString(compositeUpperBound, context);
        } else {
            final TypeMirror erasureType = context.getTypeUtils().erasure(type);
            return TypeRenderingVisitor.toString(erasureType);
        }
    }

    public static String toTypeString(DeclaredType declaredType, TypeMirror typeMirror, Context context) {
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            typeMirror = context.getTypeUtils().asMemberOf(declaredType, ((TypeVariable) typeMirror).asElement());
        }
        return TypeRenderingVisitor.toString(typeMirror);
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
            // The lazy initialization of class symbols is unfortunately not thread safe,
            // so we force the initialization here in a synchronized block.
            // This should be fine as this process happens very early and should initialize all the necessary bits
            synchronized (superClassElement) {
                superClassElement.getKind();
            }
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

    public static String getPackageName(Element element) {
        Element parent = element.getEnclosingElement();
        while (parent.getKind() != ElementKind.PACKAGE) {
            parent = parent.getEnclosingElement();
        }
        return parent.toString();
    }

    /**
     * @author Christian Beikov
     * @since 1.6.2
     */
    public static final class TypeRenderingVisitor extends SimpleTypeVisitor8<Object, Object> {

        private final StringBuilder sb = new StringBuilder();
        private final Set<TypeVariable> visitedTypeVariables = new HashSet<>();

        private TypeRenderingVisitor() {
        }

        public static String toString(TypeMirror typeMirror) {
            if (typeMirror.getKind() == TypeKind.TYPEVAR) {
                // Top level type variables don't need to render the upper bound as `T extends Type`
                final Element typeVariableElement = ((TypeVariable) typeMirror).asElement();
                if (typeVariableElement instanceof TypeParameterElement) {
                    final TypeParameterElement typeParameter = (TypeParameterElement) typeVariableElement;
                    if (typeParameter.getEnclosingElement().getKind() == ElementKind.METHOD) {
                        // But for method level type variable we return the upper bound
                        // because the type variable has no meaning except for that method
                        typeMirror = ((TypeVariable) typeMirror).getUpperBound();
                    } else {
                        return typeParameter.toString();
                    }
                } else {
                    typeMirror = typeVariableElement.asType();
                }
            } else if (typeMirror instanceof IntersectionType) {
                // For top level type only the first type is relevant
                typeMirror = ((IntersectionType) typeMirror).getBounds().get(0);
            }
            final TypeRenderingVisitor typeRenderingVisitor = new TypeRenderingVisitor();
            typeMirror.accept(typeRenderingVisitor, null);
            return typeRenderingVisitor.sb.toString();
        }

        @Override
        public Object visitPrimitive(PrimitiveType t, Object o) {
            final String primitiveTypeName = getPrimitiveTypeName(t.getKind());
            if (primitiveTypeName != null) {
                sb.append(primitiveTypeName);
            }
            return null;
        }

        private static String getPrimitiveTypeName(TypeKind kind) {
            switch (kind) {
                case INT:
                    return "int";
                case BOOLEAN:
                    return "boolean";
                case BYTE:
                    return "byte";
                case CHAR:
                    return "char";
                case DOUBLE:
                    return "double";
                case FLOAT:
                    return "float";
                case LONG:
                    return "long";
                case SHORT:
                    return "short";
                case VOID:
                    return "void";
                default:
                    return null;
            }
        }

        @Override
        public Object visitNull(NullType t, Object o) {
            return null;
        }

        @Override
        public Object visitArray(ArrayType t, Object o) {
            t.getComponentType().accept(this, null);
            sb.append("[]");
            return t;
        }

        @Override
        public Object visitDeclared(DeclaredType t, Object o) {
            sb.append(t.asElement().toString());
            List<? extends TypeMirror> typeArguments = t.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                sb.append('<');
                typeArguments.get(0).accept(this, null);
                for (int i = 1; i < typeArguments.size(); i++) {
                    sb.append(", ");
                    typeArguments.get(i).accept(this, null);
                }
                sb.append('>');
            }
            return null;
        }

        @Override
        public Object visitTypeVariable(TypeVariable t, Object o) {
            final Element typeVariableElement = t.asElement();
            if (typeVariableElement instanceof TypeParameterElement) {
                final TypeParameterElement typeParameter = (TypeParameterElement) typeVariableElement;
                sb.append(typeParameter);
                if (!"java.lang.Object".equals(t.getUpperBound().toString()) && visitedTypeVariables.add(t)) {
                    sb.append(" extends ");
                    t.getUpperBound().accept(this, null);
                    visitedTypeVariables.remove(t);
                }
            } else {
                typeVariableElement.asType().accept(this, null);
            }
            return null;
        }

        @Override
        public Object visitWildcard(WildcardType t, Object o) {
            sb.append('?');
            if (t.getExtendsBound() != null) {
                sb.append(" extends ");
                t.getExtendsBound().accept(this, null);
            }
            if (t.getSuperBound() != null) {
                sb.append(" super ");
                t.getSuperBound().accept(this, null);
            }
            return null;
        }

        @Override
        public Object visitUnion(UnionType t, Object o) {
            return null;
        }

        @Override
        public Object visitIntersection(IntersectionType t, Object o) {
            final List<? extends TypeMirror> bounds = t.getBounds();
            bounds.get(0).accept(this, null);
            for (int i = 0; i < bounds.size(); i++) {
                sb.append(" & ");
                bounds.get(i).accept(this, null);
            }
            return null;
        }

        @Override
        public Object visitExecutable(ExecutableType t, Object o) {
            return null;
        }

        @Override
        public Object visitNoType(NoType t, Object o) {
            sb.append("void");
            return null;
        }
    }

}
