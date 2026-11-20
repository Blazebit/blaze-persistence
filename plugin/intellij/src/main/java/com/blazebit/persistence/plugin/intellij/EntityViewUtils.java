package com.blazebit.persistence.plugin.intellij;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiExpression;
import com.siyeh.ig.psiutils.ExpressionUtils;

public final class EntityViewUtils {

    private EntityViewUtils() {
    }

    public static PsiClass getEntityViewEntityClass(PsiClass entityViewClass) {
        PsiAnnotation entityViewAnnotation = entityViewClass.getAnnotation("com.blazebit.persistence.view.EntityView");
        if (entityViewAnnotation == null) {
            return null;
        }

        return ((PsiClassType) ExpressionUtils.computeConstantExpression((PsiExpression) entityViewAnnotation.findAttributeValue("value"), true)).resolve();
    }
}
