import CustomExceptions.SymbolAlreadyDefinedException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends TrinityBaseVisitor<Type> implements TrinityVisitor<Type> {
    TypeVisitor(ErrorReporter errorReporter, SymbolTable symbolTable) {
        this.errorReporter = errorReporter;
        this.symbolTable = symbolTable;
    }

    TypeVisitor() {
        errorReporter = new StandardErrorReporter(true);
    }

    private ErrorReporter errorReporter;
    private SymbolTable symbolTable;

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {

        // Declared (expected) type:
        Type LHS = ctx.TYPE().accept(this);

        // Type found in expr (RHS of declaration)
        Type RHS = ctx.expr().accept(this);

        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.getType() == RHS.getType()) {

            try {
                symbolTable.enterSymbol(ctx.ID().getText(), LHS);

            } catch (SymbolAlreadyDefinedException e) {

                errorReporter.reportError("Symbol was already defined!");
            }

            return LHS;
        }
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return null;
    }

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {

        symbolTable.openScope();

        Type TheBlock = ctx.block().accept(this);
        Type FunctionType = ctx.TYPE().accept(this);
        List<Type> formalParamters = new ArrayList<>();
        for (int i = 0; i < ctx.formalParameters().formalParameter().size(); i++) {
            formalParamters.add(ctx.formalParameters().formalParameter(i).accept(this));
        }

        if (FunctionType == TheBlock) {
            Type FunctionDecl = new Type(FunctionType, formalParamters);
            try {
                symbolTable.enterSymbol(ctx.ID().getText(), FunctionDecl);
            } catch (SymbolAlreadyDefinedException e) {
                errorReporter.reportError("Symbol was already defined!");
            }
        } else {
            errorReporter.reportTypeError(FunctionType.getType(), TheBlock.getType());
        }

        symbolTable.closeScope();

        return null;
    }

    @Override
    public Type visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        System.out.println("Err");
        System.out.println("and err");
        System.out.println("and err again,");
        System.out.println("but less");
        System.out.println("and less");
        System.out.println("and less.");

        return null;
    }

    @Override
    public Type visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        Type parameterType = ctx.TYPE().accept(this);

        try {
            symbolTable.enterSymbol(ctx.ID().getText(), parameterType);
        } catch (SymbolAlreadyDefinedException e) {
            errorReporter.reportError("Symbol was already defined!");
        }

        return parameterType;
    }

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
        Type stmtType = new Type();
        List<Type> allStmtTypes = new ArrayList<>();

        // Add all stmt types which are not null...
        for (int i = 0; i < ctx.stmt().size(); i++) {
            stmtType = ctx.stmt(i).accept(this);
            if (stmtType.getType() != null) {
                allStmtTypes.add(stmtType);
            }
        }

        // Check if all stmtTypes are compatible...
        Type decidingStmtType = allStmtTypes.get(0);
        for (int i = 0; i < allStmtTypes.size(); i++) {
            if (allStmtTypes.get(i) != decidingStmtType) {
                errorReporter.reportError("");
            }
        }

        return null;
    }

    @Override
    public Type visitIfBlock(TrinityParser.IfBlockContext ctx) {
        return null;
    }

    @Override
    public Type visitIfStmt(TrinityParser.IfStmtContext ctx) {
        return null;
    }

    @Override
    public Type visitElseIfStmt(TrinityParser.ElseIfStmtContext ctx) {
        return null;
    }

    @Override
    public Type visitElseStmt(TrinityParser.ElseStmtContext ctx) {
        return null;
    }

    @Override
    public Type visitRelation(TrinityParser.RelationContext ctx) {

        // Type found in LHS expr
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());

        // Check if the two achieved types matches each other and return a boolean:
        // If not boolean, then an error must be shown to the user
        if (LHS.getType() == RHS.getType())
            return new Type(Type.TrinityType.BOOLEAN);
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return new Type(Type.TrinityType.BOOLEAN);
    }

    @Override
    public Type visitMatrixLit(TrinityParser.MatrixLitContext ctx) {
        return new Type(Type.TrinityType.MATRIX);
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return new Type(ctx.expr().accept(this).getType());
    }

    @Override
    public Type visitVectorLit(TrinityParser.VectorLitContext ctx) {
        return new Type(Type.TrinityType.VECTOR);
    }

    @Override
    public Type visitNumber(TrinityParser.NumberContext ctx) {
        return new Type(Type.TrinityType.SCALAR);
    }

    @Override
    public Type visitTranspose(TrinityParser.TransposeContext ctx) {
        return null;
    }

    @Override
    public Type visitAddSub(TrinityParser.AddSubContext ctx) {

        // Type found in LHS expr
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());


        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.getType() == RHS.getType())
            return LHS;
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return null;
    }

    @Override
    public Type visitBoolean(TrinityParser.BooleanContext ctx) {
        return new Type(Type.TrinityType.BOOLEAN);
    }

    @Override
    public Type visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        return null;
    }

    @Override
    public Type visitNot(TrinityParser.NotContext ctx) {
        return null;
    }

    @Override
    public Type visitMatrixIndexing(TrinityParser.MatrixIndexingContext ctx) {
        return new Type(Type.TrinityType.MATRIX);
    }

    @Override
    public Type visitExponent(TrinityParser.ExponentContext ctx) {
        return null;
    }

    @Override
    public Type visitOr(TrinityParser.OrContext ctx) {

        // Type found in LHS expr must be boolean
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr must be boolean
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());

        // Check if the two achieved types are boolean
        // If not boolean, then an error must be shown to the user
        if (LHS.getType().equals(Type.TrinityType.BOOLEAN) && RHS.getType().equals(Type.TrinityType.BOOLEAN))
            return new Type(Type.TrinityType.BOOLEAN);
        else
            errorReporter.reportError("Type error at OR.");

        return null;
    }

    @Override
    public Type visitMultDivMod(TrinityParser.MultDivModContext ctx) {

        // Type found in LHS expr
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());

        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.getType() == RHS.getType())
            return LHS;
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return null;
    }

    @Override
    public Type visitVectorIndexing(TrinityParser.VectorIndexingContext ctx) {
        return new Type(Type.TrinityType.VECTOR);
    }

    @Override
    public Type visitConst(TrinityParser.ConstContext ctx) {
        return null;
    }

    @Override
    public Type visitNegate(TrinityParser.NegateContext ctx) {
        return null;
    }

    @Override
    public Type visitAnd(TrinityParser.AndContext ctx) {

        // Type found in LHS expr must be boolean
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr must be boolean
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());

        // Check if the two achieved types are boolean
        // If not boolean, then an error must be shown to the user
        if (LHS.getType().equals(Type.TrinityType.BOOLEAN) && RHS.getType().equals(Type.TrinityType.BOOLEAN))
            return new Type(Type.TrinityType.BOOLEAN);
        else
            errorReporter.reportError("Type error at AND.");

        return new Type(Type.TrinityType.BOOLEAN);
    }

    @Override
    public Type visitEquality(TrinityParser.EqualityContext ctx) {

        // Type found in LHS expr
        Type LHS = new Type(ctx.expr().get(0).accept(this).getType());

        // Type found in RHS expr
        Type RHS = new Type(ctx.expr().get(1).accept(this).getType());

        // Check if the two achieved types matches each other and return a boolean:
        // If not boolean, then an error must be shown to the user
        if (LHS.getType() == RHS.getType())
            return new Type(Type.TrinityType.BOOLEAN);
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return new Type(Type.TrinityType.BOOLEAN);
    }

    @Override
    public Type visitExprList(TrinityParser.ExprListContext ctx) {
        return null;
    }

    @Override
    public Type visitVector(TrinityParser.VectorContext ctx) {
        return new Type(Type.TrinityType.VECTOR);
    }

    @Override
    public Type visitMatrix(TrinityParser.MatrixContext ctx) {
        return null;
    }

    @Override
    public Type visit(ParseTree tree) {
        return null;
    }

    @Override
    public Type visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public Type visitTerminal(TerminalNode node) {
        // Check and return type of node
        if (node.getSymbol().getText().contentEquals("Boolean"))
            return new Type(Type.TrinityType.BOOLEAN);
        else if (node.getSymbol().getText().contentEquals("Scalar"))
            return new Type(Type.TrinityType.SCALAR);
        else if (node.getSymbol().getText().contentEquals("Vector"))
            return new Type(Type.TrinityType.VECTOR);
        else if (node.getSymbol().getText().contentEquals("Matrix"))
            return new Type(Type.TrinityType.MATRIX);
        else
            return new Type();
    }

    @Override
    public Type visitErrorNode(ErrorNode node) {
        return null;
    }
}
