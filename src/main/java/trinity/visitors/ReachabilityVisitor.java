package trinity.visitors;

import trinity.ErrorReporter;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

public class ReachabilityVisitor extends TrinityBaseVisitor<Boolean> implements TrinityVisitor<Boolean> {

    private ErrorReporter errorReporter;

    public ReachabilityVisitor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public Boolean visitProg(TrinityParser.ProgContext ctx){
        for (int i = 0; i < ctx.functionDecl().size(); i++){
            // Check every FunctionDecl for unreachable code or missing return statements.
            // If a check has returned false, then end visitor.

            if (!ctx.functionDecl(i).accept(this)) {
                errorReporter.reportError("No return found in function.", ctx);
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        //TODO: Remember to check block for return-controls thrown too early.
        return ctx.block().accept(this);
    }

    @Override
    public Boolean visitBlock(TrinityParser.BlockContext ctx){
        int stmtSize = ctx.stmt().size();
        boolean isReturnValid = (ctx.returnStmt() != null) ? ctx.returnStmt().accept(this) : false, returnFound = false;

        for (TrinityParser.StmtContext bsctx : ctx.stmt()) {
            boolean test = bsctx.accept(this);
            returnFound = test;
            if (test)
                break;
        }

        System.out.println(returnFound);
        System.out.println(isReturnValid);

        return (returnFound || isReturnValid);
    }

    @Override
    public Boolean visitSingleExpression(TrinityParser.SingleExpressionContext ctx){
        return ctx.semiExpr().accept(this);
    }

    @Override
    public Boolean visitSemiExpr(TrinityParser.SemiExprContext ctx){
        return !(ctx.expr().accept(this) == null);
    }

    @Override
    public Boolean visitIfStatement(TrinityParser.IfStatementContext ctx){
        int blocks = ctx.block().size();
        boolean contentFound = (blocks != 0) ? true : false;

        for (TrinityParser.BlockContext btx : ctx.block()){
            if(contentFound){
                contentFound = btx.accept(this);
            }
        }

        return contentFound;
    }


    @Override
    public Boolean visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        return (ctx.semiExpr() != null) ? !ctx.semiExpr().isEmpty() : true;
    }


    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        // Simply return whatever the block returns, but do accept empty blocks.
        return ctx.block().accept(this);
    }

    @Override
    public Boolean visitForLoop(TrinityParser.ForLoopContext ctx) {
        return ctx.block().accept(this);
    }
}
