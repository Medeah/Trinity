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

    private static StaticMatrix createStaticMatrix(String id, List<TrinityParser.VectorContext> vectors) {
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = id;
        staticMatrix.items = new ArrayList<>();

        if (vectors != null) {
            for (TrinityParser.VectorContext vector : vectors) {
                if (vector.exprList() != null) {
                    staticMatrix.items.addAll(vector.exprList().expr());
                } else if (vector.range() != null) {
                    //ni.items = vector.range();
                    //TODO: range
                    System.out.println("dv: no range yet!");
                }
            }
        }

        return staticMatrix;
    }

    // The ref variable stores the generated ids for pre-initialized matrices and vectors
    // so they can be referenced later on.
    @Override
    public Iterable<StaticMatrix> visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        if (ctx.matrix() == null) {
            return null;
        }

        ctx.ref = UniqueId.next();

        StaticMatrix staticMatrix = createStaticMatrix(ctx.ref, ctx.matrix().vector());

        //TODO: ensure and remove
        MatrixType type = ((MatrixType) ctx.t);
        assert staticMatrix.items.size() == type.getCols() * type.getRows();
        if (staticMatrix.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

        return aggregateResult(visitChildren(ctx), ImmutableList.of(staticMatrix));
    }

    @Override
    public Iterable<StaticMatrix> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();

        StaticMatrix staticMatrix = createStaticMatrix(ctx.ref, singletonList(ctx.vector()));

        //TODO: ensure and remove
        MatrixType type = ((MatrixType) ctx.t);
        assert staticMatrix.items.size() == type.getCols() * type.getRows();
        if (staticMatrix.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

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
