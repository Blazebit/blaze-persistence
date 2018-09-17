/*
 * Copyright 2011-2017 the original author or authors.
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
package com.blazebit.persistence.spring.data.base.query;

import org.springframework.data.repository.query.Parameters;
import org.springframework.util.Assert;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

/**
 * Special {@link org.springframework.data.jpa.repository.query.ParameterBinder} to bind {@link CriteriaQuery} parameters. parameters.
 *
 * Christian Beikov: Copied to be able to share code between Spring Data integrations for 1.x and 2.x.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public abstract class AbstractCriteriaQueryParameterBinder extends ParameterBinder {

    private final Iterator<ParameterMetadataProvider.ParameterMetadata<?>> expressions;

    /**
     * Creates a new {@link AbstractCriteriaQueryParameterBinder} for the given {@link Parameters}, values and some
     * {@link javax.persistence.criteria.ParameterExpression}.
     *
     * @param parameters must not be {@literal null}.
     * @param values must not be {@literal null}.
     * @param expressions must not be {@literal null}.
     */
    public AbstractCriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, Iterable<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {

        super(parameters, values);

        Assert.notNull(expressions, "Iterable of ParameterMetadata must not be null!");
        this.expressions = expressions.iterator();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.ParameterBinder#bind(javax.persistence.Query, org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter, java.lang.Object, int)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void bind(Query query, JpaParameters.JpaParameter parameter, Object value, int position) {

        ParameterMetadataProvider.ParameterMetadata<Object> metadata = (ParameterMetadataProvider.ParameterMetadata<Object>) expressions.next();

        if (metadata.isIsNullParameter()) {
            return;
        }

        // Christian Beikov: Parameters are now set by name or position instead of by the ParameterExpression object if possible
        if (parameter.isTemporalParameter()) {
            if (metadata.getExpression().getPosition() == null) {
                if (metadata.getExpression().getName() == null) {
                    query.setParameter((Parameter) metadata.getExpression(), (Date) metadata.prepare(value),
                            parameter.getTemporalType());
                } else {
                    query.setParameter(metadata.getExpression().getName(), (Date) metadata.prepare(value),
                            parameter.getTemporalType());
                }
            } else {
                query.setParameter(metadata.getExpression().getPosition(), (Date) metadata.prepare(value),
                        parameter.getTemporalType());
            }
        } else {
            if (metadata.getExpression().getPosition() == null) {
                if (metadata.getExpression().getName() == null) {
                    query.setParameter(metadata.getExpression(), metadata.prepare(value));
                } else {
                    query.setParameter(metadata.getExpression().getName(), metadata.prepare(value));
                }
            } else {
                query.setParameter(metadata.getExpression().getPosition(), metadata.prepare(value));
            }
        }
    }
}
