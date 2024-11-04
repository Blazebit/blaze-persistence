/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.examples.itsm.model.article.view.ArticleView;
import com.blazebit.persistence.examples.itsm.model.article.view.PersonView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface ArticleViewRepository
        extends JpaRepository<ArticleView, Long> {

    List<ArticleView> findByAuthor(PersonView author);

}
