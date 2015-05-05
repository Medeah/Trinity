package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.*;
import trinity.types.MatrixType;

import java.util.ArrayList;

// TODO: Find a way to initialize vectors and matrices before referencing them without all this hacky shit.
public class DependencyVisitor extends TrinityBaseVisitor<Iterable<NeedInit>> implements TrinityVisitor<Iterable<NeedInit>> {

    // TODO: accept vectors instead
    @Override
    public Iterable<NeedInit> visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {

        ctx.cgid = UniqueId.next();

        NeedInit ni = new NeedInit();
        ni.id = ctx.cgid;
        ni.items = new ArrayList<TrinityParser.ExprContext>();

        //TODO: fix this
        if(ctx.matrix() != null) {
            for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
                if (vector.exprList() != null) {
                    ni.items.addAll(vector.exprList().expr());
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
        assert ni.items.size() == type.getCols() * type.getRows();
        if(ni.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

        return aggregateResult(visitChildren(ctx), ImmutableList.of(ni));
    }

    @Override
    public Iterable<NeedInit> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ctx.cgid = UniqueId.next();

        NeedInit ni = new NeedInit();
        ni.id = ctx.cgid;
        ni.items = new ArrayList<TrinityParser.ExprContext>();

        //TODO: fix this
        if(ctx.vector() != null){
            //for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
            TrinityParser.VectorContext vector = ctx.vector();
                if (vector.exprList() != null) {
                    ni.items.addAll(vector.exprList().expr());
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
        assert ni.items.size() == type.getCols() * type.getRows();
        if(ni.items.size() != type.getCols() * type.getRows())
            System.out.println("DV ERROR");

        return aggregateResult(visitChildren(ctx), ImmutableList.of(ni));

    }

    @Override
    protected Iterable<NeedInit> aggregateResult(Iterable<NeedInit> aggregate, Iterable<NeedInit> nextResult) {
        if(aggregate == null)
            return nextResult;
        else if(nextResult == null)
            return aggregate;
        else
            return Iterables.concat(aggregate, nextResult);
    }
}
