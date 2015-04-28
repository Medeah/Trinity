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
    public Boolean visitProg(TrinityParser.ProgContext ctx) {

        for (int i = 0; i < ctx.functionDecl().size(); i++) {
            // Check every FunctionDecl for unreachable code or missing return statements.
            // If a check has returned false, then end visitor.
            if (!ctx.functionDecl(i).accept(this)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        // If the function block is empty, then throw error!
        if (ctx.block().getChildCount() == 0) {
            errorReporter.reportError("Function contains no return value.", ctx);
            return false;
        }
        // If there exists only 1 statement and it returns a false check, then throw error!
        if (ctx.block().getChildCount() == 1 && !ctx.block().stmt(0).accept(this)) {
            errorReporter.reportError("Block has no return statement.", ctx);
            return false;
        }

/*
        // If the second last is not a return statement, then investigate last statement further!
        if(!ctx.block().getChild(ctx.block().getChildCount()-2).getText().equalsIgnoreCase("return")){

            // If the investigation returns a false check, then throw error!
            if(!ctx.getChild(ctx.block().getChildCount()-1).accept(this)){
                errorReporter.reportError("Function has either unreachable code or no return statement.",ctx);
                return false;
            }
        }*/

        //TODO: Remember to check block for return-controls thrown too early.
        return true;
    }

    @Override
    public Boolean visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {

        // Whenever a semiExpr is reached, it can never have a return,
        // thus false must be returned.
        return false;
    }

    @Override
    public Boolean visitIfStatement(TrinityParser.IfStatementContext ctx) {


        return true;
    }


    @Override
    public Boolean visitBlock(TrinityParser.BlockContext ctx) {

        // If the function block is empty, then return false.
        if (ctx.getChildCount() == 0) {
            return false;
        }
        // If there exists only 1 child and it returns a false check, then throw error!
        if (ctx.getChildCount() == 1 && !ctx.stmt(0).accept(this)) {
            return false;
        }
        // Check if the last statement in the block contains a return.
        // if not, then return false
        if (ctx.stmt().size() > 0 && !ctx.stmt(ctx.getChildCount() - 1).accept(this)) {
            return false;
        }
        // If the second last is a return statement, then return true.
        if (ctx.getChildCount() >= 2 && ctx.getChild(ctx.getChildCount() - 2).getText().equalsIgnoreCase("return")) {
            return true;
        }

        return true;
    }

    @Override
    public Boolean visitBlockStatement(TrinityParser.BlockStatementContext ctx) {

        // Simply return whatever the block returns.
        return ctx.block().accept(this);

    }

}
