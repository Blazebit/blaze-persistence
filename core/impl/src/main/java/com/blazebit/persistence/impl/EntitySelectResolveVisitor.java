package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.FetchType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

/**
 * This visitor resolves entity references to their attributes. This is needed for entity references
 * in the select clause when used in combination with aggregate functions. We have to decompose the
 * entity and add the components to the group by because all components will end up in the select clause.
 * 
 * @author Christian Beikov
 * @since 1.0.5
 */
public class EntitySelectResolveVisitor extends VisitorAdapter {

    private final Metamodel m;
    private final Set<PathExpression> pathExpressions;

    public EntitySelectResolveVisitor(Metamodel m) {
        this(m, new LinkedHashSet<PathExpression>());
    }

    public EntitySelectResolveVisitor(Metamodel m, Set<PathExpression> pathExpressions) {
        this.m = m;
        this.pathExpressions = pathExpressions;
    }

    public Set<PathExpression> getPathExpressions() {
        return pathExpressions;
    }

    @Override
    public void visit(FunctionExpression expression) {
        /**
         * Only functions returning an entity should be further resolved here in which case
         * the resulting entity's fields would belong into the group by.
         * Only until grouping by entities is resolved: https://hibernate.atlassian.net/browse/HHH-1615
         */
        if (com.blazebit.persistence.impl.util.ExpressionUtils.isValueFunction(expression) ||
                com.blazebit.persistence.impl.util.ExpressionUtils.isEntryFunction(expression)) {
            super.visit(expression);
        }
    }

    @Override
    public void visit(PathExpression expression) {
        if (expression.getField() == null) {
            /**
             * We need to resolve entity selects because hibernate will
             * select every entity attribute. Since we need every select in
             * the group by (because of DB2) we need to resolve such entity
             * selects here
             */
            JoinNode baseNode = ((JoinNode) expression.getBaseNode());
            EntityType<?> entityType;

            try {
                entityType = m.entity(baseNode.getPropertyClass());
            } catch (IllegalArgumentException e) {
                // ignore if the expression is not an entity
                return;
            }

            // we need to ensure a deterministic order for testing
            SortedSet<Attribute<?, ?>> sortedAttributes = new TreeSet<Attribute<?, ?>>(new Comparator<Attribute<?, ?>>() {

                @Override
                public int compare(Attribute<?, ?> o1, Attribute<?, ?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }

            });
            // TODO: a polymorphic query will fail because we don't collect subtype properties
            sortedAttributes.addAll(entityType.getAttributes());
            for (Attribute<?, ?> attr : sortedAttributes) {
                boolean resolve = false;
                if (ExpressionUtils.isAssociation(attr) && !attr.isCollection()) {
                    resolve = true;
                } else if (ExpressionUtils.getFetchType(attr) == FetchType.EAGER) {
                    if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                        throw new UnsupportedOperationException("Eager element collections are not supported");
                    }
                    resolve = true;
                }

                if (resolve) {
                    PathExpression attrPath = new PathExpression(new ArrayList<PathElementExpression>(expression.getExpressions()));
                    attrPath.setPathReference(new SimplePathReference(baseNode, attr.getName(), null));
                    pathExpressions.add(attrPath);
                }
            }
        }
    }

    public void resolve(JoinNode baseNode) {

    }
}