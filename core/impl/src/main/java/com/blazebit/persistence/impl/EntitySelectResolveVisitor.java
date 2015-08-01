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

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

/**
 * This visitor resolves entity references to their attributes. This is needed for entity references
 * in the select clause when used in combination with aggregate functions. We have to decompose the
 * entity and add the components to the group by because all component will end up in the select clause.
 * 
 * @author Christian
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
        if (!(expression instanceof AggregateExpression)) {
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
            try {
                EntityType<?> entityType = m.entity(baseNode.getPropertyClass());
                // we need to ensure a deterministic order for testing
                SortedSet<Attribute<?, ?>> sortedAttributes = new TreeSet<Attribute<?, ?>>(new Comparator<Attribute<?, ?>>() {

                    @Override
                    public int compare(Attribute<?, ?> o1, Attribute<?, ?> o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                });
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
                        attrPath.setBaseNode(baseNode);
                        attrPath.setField(attr.getName());
                        pathExpressions.add(attrPath);
                    }
                }
                return;
            } catch (IllegalArgumentException e) {
                // ignore if the expression is not an entity
            }
        }
    }
}