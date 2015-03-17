import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TypeVisitor implements TrinityVisitor<Type> {
    TypeVisitor(GenericErrorReporter genericErrorReporter) {
        errorReporter = genericErrorReporter;
    }

    TypeVisitor() {
        errorReporter = new ErrorReporter(true);
    }

    private GenericErrorReporter errorReporter;

    @Override
    public Type visitProg(TrinityParser.ProgContext ctx) {
        return null;
    }

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {

        // Declared (expected) type:
        Type LHS = new Type(ctx.getChild(0).accept(this).getType());

        // Type found in expr (RHS of declaration)
        Type RHS = new Type(ctx.expr().accept(this).getType());

        // Check if the two achieved types matches each other and react accordingly:
        if (LHS.getType() == RHS.getType())
            return LHS;
        else
            errorReporter.reportError("Type error!");

        return null;
    }

    @Override
    public Type visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return null;
    }

    @Override
    public Type visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        return null;
    }

    @Override
    public Type visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        return null;
    }

    @Override
    public Type visitBlock(TrinityParser.BlockContext ctx) {
        return null;
    }

    @Override
    public Type visitStmt(TrinityParser.StmtContext ctx) {
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
        return null;
    }

    @Override
    public Type visitMatrixLit(TrinityParser.MatrixLitContext ctx) {
        return new Type(Type.TrinityType.Matrix);
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return null;
    }

    @Override
    public Type visitVectorLit(TrinityParser.VectorLitContext ctx) {
        return new Type(Type.TrinityType.Vector);
    }

    @Override
    public Type visitNumber(TrinityParser.NumberContext ctx) {
        return new Type(Type.TrinityType.Scalar);
    }

    @Override
    public Type visitTranspose(TrinityParser.TransposeContext ctx) {
        return null;
    }

    @Override
    public Type visitAddSub(TrinityParser.AddSubContext ctx) {
        return null;
    }

    @Override
    public Type visitBoolean(TrinityParser.BooleanContext ctx) {
        return new Type(Type.TrinityType.Boolean);
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
        return new Type(Type.TrinityType.Matrix);
    }

    @Override
    public Type visitExponent(TrinityParser.ExponentContext ctx) {
        return null;
    }

    @Override
    public Type visitOr(TrinityParser.OrContext ctx) {
        return null;
    }

    @Override
    public Type visitMultDivMod(TrinityParser.MultDivModContext ctx) {
        return null;
    }

    @Override
    public Type visitVectorIndexing(TrinityParser.VectorIndexingContext ctx) {
        return new Type(Type.TrinityType.Vector);
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
        return null;
    }

    @Override
    public Type visitEquality(TrinityParser.EqualityContext ctx) {
        return null;
    }

    @Override
    public Type visitExprList(TrinityParser.ExprListContext ctx) {
        return null;
    }

    @Override
    public Type visitVector(TrinityParser.VectorContext ctx) {
        return new Type(Type.TrinityType.Vector);
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
            return new Type(Type.TrinityType.Boolean);
        else if (node.getSymbol().getText().contentEquals("Scalar"))
            return new Type(Type.TrinityType.Scalar);
        else if (node.getSymbol().getText().contentEquals("Vector"))
            return new Type(Type.TrinityType.Vector);
        else if (node.getSymbol().getText().contentEquals("Matrix"))
            return new Type(Type.TrinityType.Matrix);
        else
            return new Type();
    }

    @Override
    public Type visitErrorNode(ErrorNode node) {
        return null;
    }
}
