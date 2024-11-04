/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import com.blazebit.persistence.examples.itsm.model.article.entity.Article;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Article.class)
public interface ArticleLocalized {

    @IdMapping
    Long getId();

    @Mapping("coalesce(title.localizedValues[:locale], title.localizedValues[:defaultLocale])")
    String getTitle();

    @Mapping("coalesce(content.localizedValues[:locale], content.localizedValues[:defaultLocale])")
    String getContent();

}
