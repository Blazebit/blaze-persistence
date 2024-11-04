/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import com.blazebit.persistence.examples.itsm.model.article.entity.Article;
import com.blazebit.persistence.examples.itsm.model.common.view.AuditedView;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Article.class)
@CreatableEntityView
@UpdatableEntityView
public interface ArticleView extends AuditedView {

    @IdMapping
    Long getId();

    PersonView getAuthor();

    void setAuthor(PersonView person);

    LocalizedStringView getTitle();

    void setTitle(LocalizedStringView title);

    String getSlug();

    void setSlug(String slug);

}
