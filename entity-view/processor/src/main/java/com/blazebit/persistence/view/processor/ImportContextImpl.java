/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ImportContextImpl implements ImportContext {

    private static final Map<String, String> PRIMITIVES = new HashMap<>();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final Set<String> imports = new TreeSet<>();
    private final Map<String, String> simpleNames = new HashMap<>();
    private final String basePackage;

    static {
        PRIMITIVES.put("char", "Character");

        PRIMITIVES.put("byte", "Byte");
        PRIMITIVES.put("short", "Short");
        PRIMITIVES.put("int", "Integer");
        PRIMITIVES.put("long", "Long");

        PRIMITIVES.put("boolean", "Boolean");

        PRIMITIVES.put("float", "Float");
        PRIMITIVES.put("double", "Double");
    }

    public ImportContextImpl(String basePackage) {
        this.basePackage = basePackage;
    }

    public String importType(String fqcn) {
        String result = fqcn;

        String prefix = "";
        if (fqcn.startsWith("? extends ")) {
            prefix = "? extends ";
            fqcn = fqcn.substring(prefix.length());
        } else if (fqcn.startsWith("? super ")) {
            prefix = "? super ";
            fqcn = fqcn.substring(prefix.length());
        }

        String additionalTypePart = null;
        int ltIdx = fqcn.indexOf('<');
        int bracketIdx;
        if (ltIdx != -1) {
            int gtIdx = fqcn.lastIndexOf('>');
            additionalTypePart = importTypeArguments(result, ltIdx, gtIdx) + result.substring(gtIdx + 1);
            result = result.substring(0, ltIdx);
            fqcn = result;
        } else if ((bracketIdx = fqcn.indexOf('[')) != -1) {
            additionalTypePart = result.substring(bracketIdx);
            result = result.substring(0, bracketIdx);
            fqcn = result;
        }

        String pureFqcn = fqcn.replace('$', '.');

        boolean canBeSimple;

        String simpleName = unqualify(fqcn);
        String existingFqcn = simpleNames.get(simpleName);
        if (existingFqcn != null) {
            if (existingFqcn.equals(pureFqcn)) {
                canBeSimple = true;
            } else {
                canBeSimple = false;
            }
        } else {
            canBeSimple = true;
            simpleNames.put(simpleName, pureFqcn);
            imports.add(pureFqcn);
        }

        if (inJavaLang(fqcn)) {
            result = result.substring("java.lang.".length());
        } else if (inSamePackage(fqcn) || (imports.contains(pureFqcn) && canBeSimple)) {
            result = unqualify(result);
        }

        if (additionalTypePart != null) {
            result = result + additionalTypePart;
        }

        result = result.replace('$', '.');
        if (!prefix.isEmpty()) {
            result = prefix + result;
        }
        return result;
    }

    private String importTypeArguments(String typeArguments, int start, int end) {
        StringBuilder sb = new StringBuilder(end - start);
        sb.append('<');
        int currentTypeIdx = 0;
        start++;
        for (int i = start; i < end; i++) {
            final char c = typeArguments.charAt(i);
            //CHECKSTYLE:OFF: MissingSwitchDefault
            switch (c) {
                case ',':
                    if (currentTypeIdx++ > 0) {
                        sb.append(", ");
                    }
                    sb.append(importType(typeArguments.substring(start, i).trim()));
                    start = i + 1;
                    break;
                case '<':
                    currentTypeIdx = 1;
                    int gtIdx = typeArguments.lastIndexOf('>', i);
                    sb.append(importTypeArguments(typeArguments, i, gtIdx));
                    start = gtIdx + 1;
                    break;
            }
            //CHECKSTYLE:ON: MissingSwitchDefault
        }
        if (currentTypeIdx > 0) {
            sb.append(", ");
        }
        sb.append(importType(typeArguments.substring(start, end).trim()));
        sb.append('>');
        return sb.toString();
    }

    private boolean inDefaultPackage(String className) {
        return className.indexOf('.') == -1;
    }

    private boolean isPrimitive(String className) {
        return PRIMITIVES.containsKey(className);
    }

    private boolean inSamePackage(String className) {
        return isInPackage(className, basePackage);
    }

    private static boolean isInPackage(String className, String packageName) {
        return className.regionMatches(0, packageName, 0, packageName.length()) && className.lastIndexOf('.') == packageName.length();
    }

    private boolean inJavaLang(String className) {
        return isInPackage(className, "java.lang");
    }

    public String generateImports() {
        StringBuilder builder = new StringBuilder();

        for (String next : imports) {
            if (!isAutoImported(next)) {
                builder.append("import ").append(next).append(';').append(LINE_SEPARATOR);
            }
        }

        return builder.toString();
    }

    private boolean isAutoImported(String next) {
        return isPrimitive(next) || inDefaultPackage(next) || inJavaLang(next) || inSamePackage(next);
    }

    public static String unqualify(String qualifiedName) {
        int idx = qualifiedName.lastIndexOf('.');
        return (idx < 0) ? qualifiedName : qualifiedName.substring(idx + 1);
    }
}
