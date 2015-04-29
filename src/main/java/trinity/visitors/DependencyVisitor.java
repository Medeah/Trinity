package trinity.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

import java.util.List;

public class DependencyVisitor extends TrinityBaseVisitor<Iterable<TrinityParser.ExprContext>> implements TrinityVisitor<Iterable<TrinityParser.ExprContext>> {


    @Override
    public Iterable<TrinityParser.ExprContext> visitIfStatement(TrinityParser.IfStatementContext ctx) {


       // Iterable<TrinityParser.ExprContext> test =  visitChildren(ctx);


        return super.visitIfStatement(ctx);
    }

    @Override
    public Iterable<TrinityParser.ExprContext> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        //TODO: null
        //Iterable<TrinityParser.ExprContext> exprs = ctx.vector().exprList().accept(this);
        return ImmutableList.copyOf(ctx.vector().exprList().expr());


    }

   /* @Override
    public Iterable<ExprContext> visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {

        Iterable<ExprContext> test = ImmutableList.of("lol);

        return test;
    }*/

    @Override
    public Iterable<TrinityParser.ExprContext> visitExprList(TrinityParser.ExprListContext ctx) {
       // Iterable<ExprContext> lol =  new
     /*   ctx.expr(0).accept(this);
        for(int i = 1; i < ctx.expr().size(); i++) {

            ctx.expr(i).accept(this);
        }*/

       // return ImmutableList.copyOf(ctx.expr());
        //return ImmutableList.of(ctx.expr());
        
        
    }


    @Override
    protected Iterable<TrinityParser.ExprContext> aggregateResult(Iterable<TrinityParser.ExprContext> aggregate, Iterable<TrinityParser.ExprContext> nextResult) {
        if(aggregate == null)
            return nextResult;
        else if(nextResult == null)
            return aggregate;
        else
            return Iterables.concat(aggregate, nextResult);
    }
}
