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

package com.blazebit.persistence.impl.eclipselink;

import com.blazebit.persistence.ObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.QueryRedirector;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ObjectBuilderQueryRedirectorAdapter implements QueryRedirector {

    private static final long serialVersionUID = 1L;
    
    private final ObjectBuilder<?> builder;

    public ObjectBuilderQueryRedirectorAdapter(ObjectBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object invokeQuery(DatabaseQuery query, Record arguments, Session session) {
        List<Object> tupleList = (List<Object>) query.execute((AbstractSession) session, (AbstractRecord) arguments);
        List<Object> results = new ArrayList<Object>(tupleList.size());

        for (int i = 0; i < tupleList.size(); i++) {
            Object tuple = tupleList.get(i);

            if (tuple instanceof Object[]) {
                results.add(builder.build((Object[]) tuple));
            } else {
                results.add(builder.build(new Object[]{ tuple }));
            }
        }

        return builder.buildList((List) results);
    }
}
