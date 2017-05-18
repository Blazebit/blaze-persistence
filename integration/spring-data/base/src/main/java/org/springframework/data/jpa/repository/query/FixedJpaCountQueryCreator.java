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

package org.springframework.data.jpa.repository.query;

import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class FixedJpaCountQueryCreator extends FixedJpaQueryCreator {

    public FixedJpaCountQueryCreator(PartTree tree, Class<?> domainClass, CriteriaBuilder builder,
                                     ParameterMetadataProvider provider) {
        super(tree, domainClass, builder, provider);
    }

    @Override
    protected CriteriaQuery<Object> complete(Predicate predicate, Sort sort, CriteriaQuery<Object> query,
                                             CriteriaBuilder builder, Root<?> root) {
        CriteriaQuery<Object> select = query.select(query.isDistinct() ? builder.countDistinct(root) : builder.count(root));
        return predicate == null ? select : select.where(predicate);
    }
}
