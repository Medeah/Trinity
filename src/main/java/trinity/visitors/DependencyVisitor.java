package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.*;
import trinity.types.MatrixType;

import java.util.ArrayList;

public class DependencyVisitor extends TrinityBaseVisitor<Iterable<NeedInit>> implements TrinityVisitor<Iterable<NeedInit>> {

    //TODO: accept vectors instead
    @Override
    public Iterable<NeedInit> visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        ((MatrixType)ctx.t).cgid = UniqueId.next();

        NeedInit ni = new NeedInit();
        ni.type = ((MatrixType)ctx.t);

        ni.items = new ArrayList<TrinityParser.ExprContext>();

        //TODO: fix this
        if(ctx.matrix() != null){
            for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
                if (vector.exprList() != null) {
                    ni.items.addAll(vector.exprList().expr());

                    //ni.id = ((MatrixType) ctx.t).cgid;
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
        //visitChildren(ctx);


        assert ni.items.size() == ni.type.getCols() * ni.type.getRows();

        //TODO: rewrite this context to id...

        //TODO: null
        //Iterable<NeedInit> exprs = ctx.vector().exprList().accept(this);
        //return ImmutableList.copyOf(ctx.vector().exprList().expr());
        //return ImmutableList.of(ni);

        return aggregateResult(visitChildren(ctx), ImmutableList.of(ni));
    }

    @Override
    public Iterable<NeedInit> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        ((MatrixType)ctx.t).cgid = UniqueId.next();

        NeedInit ni = new NeedInit();
        ni.type = ((MatrixType)ctx.t);

        ni.items = new ArrayList<TrinityParser.ExprContext>();

        //TODO: fix this
        if(ctx.vector() != null){
            //for(TrinityParser.VectorContext vector : ctx.matrix().vector()) {
            TrinityParser.VectorContext vector = ctx.vector();
                if (vector.exprList() != null) {
                    ni.items.addAll(vector.exprList().expr());

                    //ni.id = ((MatrixType) ctx.t).cgid;
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
        //visitChildren(ctx);


        //TODO: ensure and remove
        assert ni.items.size() == ni.type.getCols() * ni.type.getRows();
        if(ni.items.size() == ni.type.getCols() * ni.type.getRows())
            System.out.println("DV ERROR");

        //TODO: rewrite this context to id...

        //TODO: null
        //Iterable<NeedInit> exprs = ctx.vector().exprList().accept(this);
        //return ImmutableList.copyOf(ctx.vector().exprList().expr());
        //return ImmutableList.of(ni);

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
