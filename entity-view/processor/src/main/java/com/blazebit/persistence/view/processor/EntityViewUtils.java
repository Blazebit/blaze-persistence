/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public final class EntityViewUtils {

    private EntityViewUtils() {
    }

    public static String getAttributeName(Element element) {
        if (element == null) {
            return null;
        }
        final String elementName = element.getSimpleName().toString();
        if (element.getKind() == ElementKind.METHOD) {
            if (elementName.startsWith("is") && elementName.length() > 2 && Character.isUpperCase(elementName.charAt(2))) {
                return Character.toLowerCase(elementName.charAt(2)) + elementName.substring(3);
            } else if (elementName.startsWith("get") && elementName.length() > 3 && Character.isUpperCase(elementName.charAt(3))) {
                return Character.toLowerCase(elementName.charAt(3)) + elementName.substring(4);
            }
        } else if (element.getKind() == ElementKind.FIELD) {
            return elementName;
        }
        return null;
    }

}
