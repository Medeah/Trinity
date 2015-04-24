package trinity.visitors;

import org.antlr.v4.runtime.tree.ParseTree;
import trinity.*;
import trinity.types.Type;

public class ReachabilityVisitor extends TrinityBaseVisitor<Object> implements TrinityVisitor<Object>{

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    public ReachabilityVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;
    }


    @Override
    public Object visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        boolean returnNotFound = true;

        //for loop that treverse through all the children of the block.
        for (ParseTree child : ctx.block().children){

            if(child.getText().equalsIgnoreCase("return")){
                returnNotFound = false;
            }
        }

        if(returnNotFound){
            errorReporter.reportError("ARGH!", ctx);
        }

        return null;
    }

}
