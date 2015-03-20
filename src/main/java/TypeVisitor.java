import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TypeVisitor extends TrinityBaseVisitor<Type> implements TrinityVisitor<Type> {
    TypeVisitor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    TypeVisitor() {
        errorReporter = new StandardErrorReporter(true);
    }

    private ErrorReporter errorReporter;

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {

        // Declared (expected) type:
        Type LHS = ctx.TYPE().accept(this);

        // Type found in expr (RHS of declaration)
        Type RHS = ctx.expr().accept(this);

        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.getType() == RHS.getType())
            return LHS;
        else
            errorReporter.reportTypeError(LHS.getType(), RHS.getType());

        return null;
    }

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return null;
    }

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
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
        System.out.println("TEST");

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
