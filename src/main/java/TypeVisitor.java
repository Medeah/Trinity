import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TypeVisitor implements TrinityVisitor<Type> {
    @Override
    public Type visitProg(TrinityParser.ProgContext ctx) {
        return null;
    }

    @Override
    public Type visitConstDecl(TrinityParser.ConstDeclContext ctx) {
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
        return null;
    }

    @Override
    public Type visitParens(TrinityParser.ParensContext ctx) {
        return null;
    }

    @Override
    public Type visitVectorLit(TrinityParser.VectorLitContext ctx) {
        return null;
    }

    @Override
    public Type visitNumber(TrinityParser.NumberContext ctx) {
        return null;
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
        return null;
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
        return null;
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
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public Type visitErrorNode(ErrorNode node) {
        return null;
    }
}
