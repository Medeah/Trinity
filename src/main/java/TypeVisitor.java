import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TypeVisitor implements TrinityVisitor<String> {
    @Override
    public String visitProg(TrinityParser.ProgContext ctx) {
        return null;
    }

    @Override
    public String visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        return null;
    }

    @Override
    public String visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        return null;
    }

    @Override
    public String visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        return null;
    }

    @Override
    public String visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        return null;
    }

    @Override
    public String visitBlock(TrinityParser.BlockContext ctx) {
        return null;
    }

    @Override
    public String visitStmt(TrinityParser.StmtContext ctx) {
        return null;
    }

    @Override
    public String visitIfBlock(TrinityParser.IfBlockContext ctx) {
        return null;
    }

    @Override
    public String visitIfStmt(TrinityParser.IfStmtContext ctx) {
        return null;
    }

    @Override
    public String visitElseIfStmt(TrinityParser.ElseIfStmtContext ctx) {
        return null;
    }

    @Override
    public String visitElseStmt(TrinityParser.ElseStmtContext ctx) {
        return null;
    }

    @Override
    public String visitRelation(TrinityParser.RelationContext ctx) {
        return null;
    }

    @Override
    public String visitMatrixLit(TrinityParser.MatrixLitContext ctx) {
        return null;
    }

    @Override
    public String visitParens(TrinityParser.ParensContext ctx) {
        return null;
    }

    @Override
    public String visitVectorLit(TrinityParser.VectorLitContext ctx) {
        return null;
    }

    @Override
    public String visitNumber(TrinityParser.NumberContext ctx) {
        return null;
    }

    @Override
    public String visitTranspose(TrinityParser.TransposeContext ctx) {
        return null;
    }

    @Override
    public String visitAddSub(TrinityParser.AddSubContext ctx) {
        return null;
    }

    @Override
    public String visitBoolean(TrinityParser.BooleanContext ctx) {
        return null;
    }

    @Override
    public String visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        return null;
    }

    @Override
    public String visitNot(TrinityParser.NotContext ctx) {
        return null;
    }

    @Override
    public String visitMatrixIndexing(TrinityParser.MatrixIndexingContext ctx) {
        return null;
    }

    @Override
    public String visitExponent(TrinityParser.ExponentContext ctx) {
        return null;
    }

    @Override
    public String visitOr(TrinityParser.OrContext ctx) {
        return null;
    }

    @Override
    public String visitMultDivMod(TrinityParser.MultDivModContext ctx) {
        return null;
    }

    @Override
    public String visitVectorIndexing(TrinityParser.VectorIndexingContext ctx) {
        return null;
    }

    @Override
    public String visitConst(TrinityParser.ConstContext ctx) {
        return null;
    }

    @Override
    public String visitNegate(TrinityParser.NegateContext ctx) {
        return null;
    }

    @Override
    public String visitAnd(TrinityParser.AndContext ctx) {
        return null;
    }

    @Override
    public String visitEquality(TrinityParser.EqualityContext ctx) {
        return null;
    }

    @Override
    public String visitExprList(TrinityParser.ExprListContext ctx) {
        return null;
    }

    @Override
    public String visitVector(TrinityParser.VectorContext ctx) {
        return null;
    }

    @Override
    public String visitMatrix(TrinityParser.MatrixContext ctx) {
        return null;
    }

    @Override
    public String visit(ParseTree tree) {
        return null;
    }

    @Override
    public String visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        return null;
    }
}
