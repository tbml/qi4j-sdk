package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.specification.Specification;

/**
 * TODO
 */
public abstract class BinarySpecification
    extends ExpressionSpecification
{
    protected Iterable<Specification<Composite>> operands;

    protected BinarySpecification( Iterable<Specification<Composite>> operands )
    {
        this.operands = operands;
    }

    public Iterable<Specification<Composite>> getOperands()
    {
        return operands;
    }
}
