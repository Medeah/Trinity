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

    private static StaticMatrix createStaticMatrix_xxx(String id, List<TrinityParser.VectorContext> vectors) {
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = id;
        //staticMatrix.items = new ArrayList<>();
        //staticMatrix.rows= new ArrayList<>();

        if (vectors != null) {
            staticMatrix.rows = vectors;

            /*for (TrinityParser.VectorContext vector : vectors) {
                if (vector.exprList() != null) {
                    staticMatrix.items.addAll(vector.exprList().expr());
                } else if (vector.range() != null) {
                    //staticMatrix.range = vector.range();
                    staticMatrix.items.add(vector.range());

                    //ni.items = vector.range();
                    //TODO: range
                    //System.out.println("dv: no range yet!");
                }
            }*/
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

        //StaticMatrix staticMatrix = createStaticMatrix(ctx.ref, ctx.matrix().vector());

        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = ctx.matrix().vector();

        staticMatrix.size = ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        //assert staticMatrix.items.size() == ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        return aggregateResult(visitChildren(ctx), ImmutableList.of(staticMatrix));
    }

    @Override
    public Iterable<StaticMatrix> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();

        //StaticMatrix staticMatrix = createStaticMatrix(ctx.ref, singletonList(ctx.vector()));
        StaticMatrix staticMatrix = new StaticMatrix();
        staticMatrix.id = ctx.ref;
        staticMatrix.rows = singletonList(ctx.vector());

        staticMatrix.size =  ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

        //assert staticMatrix.items.size() == ((MatrixType) ctx.t).getCols() * ((MatrixType) ctx.t).getRows();

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
