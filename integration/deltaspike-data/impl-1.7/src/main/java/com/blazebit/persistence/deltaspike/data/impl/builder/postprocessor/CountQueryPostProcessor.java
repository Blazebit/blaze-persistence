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

package com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewJpaQueryPostProcessor;
import com.blazebit.persistence.deltaspike.data.impl.param.ExtendedParameters;
import org.apache.deltaspike.data.impl.util.jpa.QueryStringExtractorFactory;

import javax.persistence.Query;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;
import static org.apache.deltaspike.data.impl.util.QueryUtils.nullSafeValue;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.CountQueryPostProcessor} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountQueryPostProcessor implements EntityViewJpaQueryPostProcessor {

    private static final Logger LOG = Logger.getLogger(org.apache.deltaspike.data.impl.builder.postprocessor.CountQueryPostProcessor.class.getName());

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Query postProcess(EntityViewCdiQueryInvocationContext context, Query query) {
        String queryString = getQueryString(context, query);
        CountQueryPostProcessor.QueryExtraction extract = new CountQueryPostProcessor.QueryExtraction(queryString);
        String count = extract.rewriteToCount();
        LOG.log(Level.FINER, "Rewrote query {0} to {1}", new Object[]{queryString, count});
        Query result = context.getEntityManager().createQuery(count);
        ExtendedParameters params = context.getParams();
        params.applyTo(result);
        return result;
    }

    private String getQueryString(EntityViewCdiQueryInvocationContext context, Query query) {
        if (isNotEmpty(context.getQueryString())) {
            return context.getQueryString();
        }
        return factory.extract(query);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class QueryExtraction {

        private String select;
        private String from;
        private String where;

        private String entityName;
        private final String query;

        public QueryExtraction(String query) {
            this.query = query;
        }

        public String rewriteToCount() {
            splitQuery();
            extractEntityName();
            return rewrite();
        }

        private String rewrite() {
            return new StringBuilder()
                    .append("select count(")
                    .append(nullSafeValue(select, entityName))
                    .append(") ")
                    .append(from)
                    .append(nullSafeValue(where))
                    .toString();
        }

        private void extractEntityName() {
            String[] split = from.split(" ");
            if (split.length > 1) {
                entityName = split[split.length - 1];
            } else {
                entityName = "*";
            }
        }

        private void splitQuery() {
            String lower = query.toLowerCase();
            int selectIndex = lower.indexOf("select");
            int fromIndex = lower.indexOf("from");
            int whereIndex = lower.indexOf("where");
            int orderByIndex = lower.indexOf("order by");
            if (selectIndex >= 0) {
                select = query.substring("select".length(), fromIndex);
            }
            if (whereIndex >= 0) {
                from = query.substring(fromIndex, whereIndex);
                where = query.substring(whereIndex);
                if (orderByIndex > 0) {
                    where = where.substring(0, orderByIndex - whereIndex);
                }
            } else {
                from = query.substring(fromIndex);
            }
        }

    }
}