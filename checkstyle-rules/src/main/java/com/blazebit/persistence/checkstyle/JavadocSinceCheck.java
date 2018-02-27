/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTag;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTagInfo;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTags;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtils;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple checkstyle check that asserts the since tag format. Heavily inspired by {@link JavadocTypeCheck}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JavadocSinceCheck extends AbstractCheck {

    private Scope scope = Scope.PRIVATE;
    private Scope excludeScope;

    private Pattern sinceFormatPattern;
    private String sinceFormat;

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    /**
     * Sets the scope to check.
     *
     * @param from string to set scope from
     */
    public void setScope(String from) {
        scope = Scope.getInstance(from);
    }

    /**
     * Set the excludeScope.
     *
     * @param excludeScope a {@code String} value
     */
    public void setExcludeScope(String excludeScope) {
        this.excludeScope = Scope.getInstance(excludeScope);
    }

    /**
     * Set the sinceFormat.
     *
     * @param sinceFormat a {@code String} value
     */
    public void setSinceFormat(String sinceFormat) {
        this.sinceFormat = sinceFormat;
        this.sinceFormatPattern = CommonUtils.createPattern(sinceFormat);
    }

    @Override
    public void visitToken(final DetailAST ast) {
        if (shouldCheck(ast)) {
            final FileContents contents = getFileContents();
            final int lineNo = ast.getLineNo();
            final TextBlock textBlock = contents.getJavadocBefore(lineNo);
            if (textBlock == null) {
                log(lineNo, JavadocTypeCheck.MSG_JAVADOC_MISSING);
            } else {
                final List<JavadocTag> tags = getJavadocTags(textBlock);
                if (ScopeUtils.isOuterMostType(ast)) {
                    // don't check author/version for inner classes
                    checkTag(lineNo, tags, JavadocTagInfo.SINCE.getName(),
                            sinceFormatPattern, sinceFormat);
                }
            }
        }
    }

    private boolean shouldCheck(final DetailAST ast) {
        final DetailAST mods = ast.findFirstToken(TokenTypes.MODIFIERS);
        final Scope declaredScope = ScopeUtils.getScopeFromMods(mods);
        final Scope customScope;

        if (ScopeUtils.isInInterfaceOrAnnotationBlock(ast)) {
            customScope = Scope.PUBLIC;
        } else {
            customScope = declaredScope;
        }
        final Scope surroundingScope = ScopeUtils.getSurroundingScope(ast);

        return customScope.isIn(scope)
                && (surroundingScope == null || surroundingScope.isIn(scope))
                && (excludeScope == null
                || !customScope.isIn(excludeScope)
                || surroundingScope != null
                && !surroundingScope.isIn(excludeScope));
    }

    private List<JavadocTag> getJavadocTags(TextBlock textBlock) {
        final JavadocTags tags = JavadocUtils.getJavadocTags(textBlock,
                JavadocUtils.JavadocTagType.BLOCK);
        return tags.getValidTags();
    }

    private void checkTag(int lineNo, List<JavadocTag> tags, String tagName,
                          Pattern formatPattern, String format) {
        if (formatPattern != null) {
            int tagCount = 0;
            final String tagPrefix = "@";
            for (int i = tags.size() - 1; i >= 0; i--) {
                final JavadocTag tag = tags.get(i);
                if (tag.getTagName().equals(tagName)) {
                    tagCount++;
                    if (!formatPattern.matcher(tag.getFirstArg()).find()) {
                        log(lineNo, JavadocTypeCheck.MSG_TAG_FORMAT, tagPrefix + tagName, format);
                    }
                }
            }
            if (tagCount == 0) {
                log(lineNo, JavadocTypeCheck.MSG_MISSING_TAG, tagPrefix + tagName);
            }
        }
    }
}
