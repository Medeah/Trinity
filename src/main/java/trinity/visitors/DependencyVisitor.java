package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.*;
import trinity.types.MatrixType;
import trinity.utils.UniqueId;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Visit vector and matrix literals to aggregates a list of all element expressions, and store a unique id for future reference.
 */
public class DependencyVisitor extends TrinityBaseVisitor<Iterable<StaticMatrix>> implements TrinityVisitor<Iterable<StaticMatrix>> {

    // The ref variable stores the generated ids for pre-initialized matrices and vectors
    // so they can be referenced later on.
    @Override
    public Iterable<StaticMatrix> visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        if (ctx.matrix() == null) {
            return null;
        }

        ctx.ref = UniqueId.next();
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = ctx.matrix().vector();
        staticMatrix.size = ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        return aggregateResult(visitChildren(ctx), ImmutableList.of(staticMatrix));
    }

    @Override
    public Iterable<StaticMatrix> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = singletonList(ctx.vector());
        staticMatrix.size =  ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        return aggregateResult(visitChildren(ctx), ImmutableList.of(staticMatrix));
    }

    @Override
    protected Iterable<StaticMatrix> aggregateResult(Iterable<StaticMatrix> aggregate, Iterable<StaticMatrix> nextResult) {
        if (aggregate == null) {
            return nextResult;
        } else if (nextResult == null) {
            return aggregate;
        }
        return Iterables.concat(aggregate, nextResult);
    }
}
