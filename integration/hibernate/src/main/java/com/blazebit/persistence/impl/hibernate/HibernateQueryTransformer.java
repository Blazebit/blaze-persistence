/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.spi.QueryTransformer;
import javax.persistence.TypedQuery;
import org.hibernate.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@ServiceProvider(QueryTransformer.class)
public class HibernateQueryTransformer implements QueryTransformer {

    @Override
    public <X> TypedQuery<X> transformQuery(TypedQuery<X> query, ObjectBuilder<X> objectBuilder) {
        Query hQuery = query.unwrap(Query.class);
        hQuery.setResultTransformer(new ObjectBuilderResultTransformerAdapter(objectBuilder));
        return query;
    }

}
