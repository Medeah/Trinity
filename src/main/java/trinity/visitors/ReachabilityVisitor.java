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

            System.out.println(" ");
            System.out.println(" ");
            System.out.println(" NEW! ");
            System.out.println(" ");
            System.out.println(" ");

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
        boolean isReturnNull = (ctx.returnStmt() != null) ? !ctx.returnStmt().accept(this) : true;

        //checks if a function does not contain any statements or returns.
        //ignores blocks who is not directly in the function base.
        if(ctx.parent instanceof TrinityParser.FunctionDeclContext && stmtSize == 0 && isReturnNull){
            errorReporter.reportError("Empty functions is not allowed.", ctx);
            return false;
        }

        if(stmtSize == 0 && isReturnNull) {
            return false;
        }else{
            for (int i = 0; i < stmtSize; i++) {
                boolean tmp = ctx.stmt(i).accept(this);
                System.out.println("Current code: " + ctx.getText());
                System.out.println("Line: " + ctx.stmt(i).getText());
                System.out.println("Current return: " + isReturnNull);
                System.out.println("Current...: " + tmp);
                if(!tmp && isReturnNull){
                    errorReporter.reportError("No return found in inner blocks.", ctx);
                }
            }
            if(errorReporter.getErrorAmount() != 0){
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
            ctx.block(i).accept(this);
        }

        return false;
    }

    @Override
    public Boolean visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        // TODO: update visitBlock to accommodate this new rule.
        boolean returnStmtSemiExpr = (ctx.semiExpr() != null) ? ctx.semiExpr().accept(this) : true;

        if(returnStmtSemiExpr){
            return false;
        }

        return true;
    }


    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        // Simply return whatever the block returns, but do accept empty blocks.
        if(ctx.block().getText().isEmpty()){
            System.out.println("From here?");
            return false;
        }else if (ctx.block().returnStmt() != null){
            return ctx.block().returnStmt().accept(this);
        }

        return ctx.block().accept(this);
    }
}
