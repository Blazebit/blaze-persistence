/*
 * Copyright 2014 - 2024 Blazebit.
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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class EntityViewTypeUtils {

    private static final Map<String, Mutability> MUTABILITY;

    static {
        Map<String, Mutability> mutability = new HashMap<>();
        immutable(mutability, "java.lang.Boolean");
        immutable(mutability, "boolean");
        immutable(mutability, "java.lang.Byte");
        immutable(mutability, "byte");
        immutable(mutability, "java.lang.Short");
        immutable(mutability, "short");
        immutable(mutability, "java.lang.Character");
        immutable(mutability, "char");
        immutable(mutability, "java.lang.Integer");
        immutable(mutability, "int");
        immutable(mutability, "java.lang.Long");
        immutable(mutability, "long");
        immutable(mutability, "java.lang.Float");
        immutable(mutability, "float");
        immutable(mutability, "java.lang.Double");
        immutable(mutability, "double");
        immutable(mutability, "java.math.BigInteger");
        immutable(mutability, "java.math.BigDecimal");
        immutable(mutability, "java.lang.String");

        immutable(mutability, "java.io.InputStream");

        trackable(mutability, "java.sql.Blob");
        trackable(mutability, "java.sql.Clob");
        trackable(mutability, "java.sql.NClob");

        mutable(mutability, "java.lang.Byte[]");
        mutable(mutability, "byte[]");
        mutable(mutability, "java.lang.Character[]");
        mutable(mutability, "char[]");

        mutable(mutability, "java.util.Date");
        mutable(mutability, "java.sql.Date");
        mutable(mutability, "java.sql.Time");
        mutable(mutability, "java.sql.Timestamp");
        mutable(mutability, "java.util.TimeZone");

        mutable(mutability, "java.util.Calendar");
        mutable(mutability, "java.util.GregorianCalendar");

        immutable(mutability, "java.lang.Class");
        immutable(mutability, "java.util.Currency");
        immutable(mutability, "java.util.Locale");
        immutable(mutability, "java.util.UUID");
        immutable(mutability, "java.net.URL");

        immutable(mutability, "java.time.LocalDate");
        immutable(mutability, "java.time.LocalTime");
        immutable(mutability, "java.time.LocalDateTime");
        immutable(mutability, "java.time.OffsetTime");
        immutable(mutability, "java.time.OffsetDateTime");
        immutable(mutability, "java.time.ZonedDateTime");
        immutable(mutability, "java.time.Duration");
        immutable(mutability, "java.time.Instant");
        immutable(mutability, "java.time.MonthDay");
        immutable(mutability, "java.time.Year");
        immutable(mutability, "java.time.YearMonth");
        immutable(mutability, "java.time.Period");
        immutable(mutability, "java.time.ZoneId");
        immutable(mutability, "java.time.ZoneOffset");
        MUTABILITY = mutability;
    }

    private EntityViewTypeUtils() {
    }

    private static void immutable(Map<String, Mutability> mutability, String fqcn) {
        mutability.put(fqcn, Mutability.IMMUTABLE);
    }

    private static void mutable(Map<String, Mutability> mutability, String fqcn) {
        mutability.put(fqcn, Mutability.MUTABLE);
    }

    private static void trackable(Map<String, Mutability> mutability, String fqcn) {
        mutability.put(fqcn, Mutability.DIRTY_TRACKABLE);
    }

    public static Mutability getMutability(String fqcn) {
        Mutability mutability = MUTABILITY.get(fqcn);
        return mutability == null ? Mutability.MUTABLE : mutability;
    }

    public static String getAttributeName(Element element) {
        String name = element.getSimpleName().toString();
        if (element.getKind() == ElementKind.METHOD) {
            if (name.startsWith("get")) {
                return firstToLower(3, name);
            } else if (name.startsWith("is")) {
                return firstToLower(2, name);
            } else {
                return firstToLower(0, name);
            }
        }
        return name;
    }

    public static String firstToLower(int skip, CharSequence s) {
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(Character.toLowerCase(s.charAt(skip)));
        sb.append(s, skip + 1, s.length());
        return sb.toString();
    }

    public static SubviewInfo getSubviewInfo(TypeElement typeElement, Context context) {
        Element subviewIdElement = null;
        boolean hasEmptyConstructor = typeElement.getKind() == ElementKind.INTERFACE;
        for (Element enclosedElement : context.getAllMembers(typeElement)) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                if (((ExecutableElement) enclosedElement).getParameters().isEmpty()) {
                    hasEmptyConstructor = true;
                    if (subviewIdElement != null) {
                        break;
                    }
                }
            } else if (TypeUtils.containsAnnotation(enclosedElement, Constants.ID_MAPPING)) {
                subviewIdElement = enclosedElement;
                if (hasEmptyConstructor) {
                    break;
                }
            }
        }
        return new SubviewInfo(subviewIdElement, hasEmptyConstructor);
    }

    public static List<EntityIdAttribute> getEntityIdAttributes(String realType, Context context) {
        List<EntityIdAttribute> elements = new ArrayList<>();
        TypeElement typeElement = context.getTypeElement(realType);
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (TypeUtils.containsAnnotation(enclosedElement, Constants.ID, Constants.EMBEDDED_ID)) {
                elements.add(new EntityIdAttribute(enclosedElement));
            }
        }
        return elements;
    }

    public static boolean isMutable(TypeElement subviewElement, Context context) {
        return TypeUtils.containsAnnotation(subviewElement, Constants.UPDATABLE_ENTITY_VIEW, Constants.CREATABLE_ENTITY_VIEW);
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    public static class SubviewInfo {
        private final Element entityViewIdElement;
        private final boolean hasEmptyConstructor;

        public SubviewInfo(Element entityViewIdElement, boolean hasEmptyConstructor) {
            this.entityViewIdElement = entityViewIdElement;
            this.hasEmptyConstructor = hasEmptyConstructor;
        }

        public Element getEntityViewIdElement() {
            return entityViewIdElement;
        }

        public boolean hasEmptyConstructor() {
            return hasEmptyConstructor;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    public enum Mutability {
        IMMUTABLE,
        DIRTY_TRACKABLE,
        MUTABLE;
    }
}
