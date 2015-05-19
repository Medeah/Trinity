package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.StaticMatrix;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;
import trinity.types.MatrixType;
import trinity.utils.UniqueId;

import static java.util.Collections.singletonList;

// TODO: maybe create private output list, and access through method instead of returning and aggregating it.

/**
 * Visits vector and matrix literals, creating a list of all element expressions,
 * and store a unique id for future reference. This information is used to initialize
 * matrix and vector literals during code generation.
 */
public class DependencyVisitor extends TrinityBaseVisitor<Iterable<StaticMatrix>> implements TrinityVisitor<Iterable<StaticMatrix>> {

    /**
     * Creates a new instance of StaticMatrix and adds it to the list.
     *
     * @param ctx the parse tree
     * @return an aggregated list of StaticMatrix
     */
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

    /**
     * Creates a new instance of StaticMatrix and adds it to the list.
     * A vector is a matrix with a single row {@code singletonList}
     *
     * @param ctx the parse tree
     * @return an aggregated list of StaticMatrix
     */
    @Override
    public Iterable<StaticMatrix> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = singletonList(ctx.vector());
        staticMatrix.size = ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        return aggregateResult(visitChildren(ctx), ImmutableList.of(staticMatrix));
    }

    /**
     * Concatenates two lists
     *
     * @param aggregate  The previous aggregate value.
     * @param nextResult The result of the immediately preceding call
     *                   to visit a child node.
     * @return The updated aggregate result.
     */
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
