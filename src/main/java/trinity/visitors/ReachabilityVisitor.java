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

        //TODO: Hmm assert false ved "do end", skal fixes.
        if(ctx.stmt().size() == 0 && ctx.getChildCount() != 2){
            //if no statements is found, and return does not exsist or not contain return object.
            errorReporter.reportError("No return found 1.", ctx);
            return false;
        }

        for (int i = 0; i < ctx.stmt().size(); i++) {
            //visit all statements in block
            if(!ctx.stmt(i).accept(this)) {
                errorReporter.reportError("No return found 2.", ctx);
                return false;
            }
        }

        return true;
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
        for (int i = 0; i < ctx.block().size(); i++){
            ctx.getChild(i).accept(this);
        }

        return false;
    }

    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx){
        // Simply return whatever the block returns, but do accept empty blocks.
        return ctx.block().accept(this);
    }
}
