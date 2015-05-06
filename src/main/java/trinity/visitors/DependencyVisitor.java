package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.*;
import trinity.types.MatrixType;

import java.util.ArrayList;

/**
 * Visit vector and matrix literals and aggregates a list of all element expressions, and stores a unique id for later referencing.
 */
public class DependencyVisitor extends TrinityBaseVisitor<Iterable<NeedInit>> implements TrinityVisitor<Iterable<NeedInit>> {

    // TODO: accept vectors instead
    // TODO: the ref is a hack for doing dependency referencing :(
    // The variable stores the generated ids for pre-initialized matrices and vectors
    // so they can be referenced later on.
    @Override
    public Iterable<NeedInit> visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        ctx.ref = UniqueId.next();

        NeedInit needInit = new NeedInit();
        needInit.id = ctx.ref;
        needInit.items = new ArrayList<>();

        //TODO: fix this
        if(ctx.matrix() != null) {
            for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
                if (vector.exprList() != null) {
                    needInit.items.addAll(vector.exprList().expr());
                } else if (vector.range() != null) {
                    //ni.items = ctx.vector().range();
                    //TODO: range
                    System.out.println("dv: no range yet!");
                }
            }
        } else {
            //TODO: remove the risk.
            //throw new Exception
            System.out.println("Congratz! This should not happen :D");
        }

        MatrixType type = ((MatrixType)ctx.t);
        //TODO: ensure and remove
        assert needInit.items.size() == type.getCols() * type.getRows();
        if(needInit.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

        return aggregateResult(visitChildren(ctx), ImmutableList.of(needInit));
    }

    @Override
    public Iterable<NeedInit> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.ref = UniqueId.next();

        NeedInit needInit = new NeedInit();
        needInit.id = ctx.ref;
        needInit.items = new ArrayList<TrinityParser.ExprContext>();

        //TODO: fix this
        if(ctx.vector() != null){
            //for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
            TrinityParser.VectorContext vector = ctx.vector();
                if (vector.exprList() != null) {
                    needInit.items.addAll(vector.exprList().expr());
                } else if (vector.range() != null) {
                    //ni.items = ctx.vector().range();
                    //TODO: range
                    System.out.println("dv: no range yet!");
                }
            //}
        } else {
            //TODO: remove the risk.
            //throw new Exception
            System.out.println("Congratz! This should not happen :D");
        }

        MatrixType type = ((MatrixType)ctx.t);
        //TODO: ensure and remove
        assert needInit.items.size() == type.getCols() * type.getRows();
        if(needInit.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

        return aggregateResult(visitChildren(ctx), ImmutableList.of(needInit));

    }

    @Override
    protected Iterable<NeedInit> aggregateResult(Iterable<NeedInit> aggregate, Iterable<NeedInit> nextResult) {
        if(aggregate == null) {
            return nextResult;
        } else if(nextResult == null) {
            return aggregate;
        }
        return Iterables.concat(aggregate, nextResult);
    }
}
