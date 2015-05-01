package trinity.visitors;

import org.antlr.v4.runtime.tree.*;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

import java.util.ArrayList;
import java.util.List;

// TODO: the type names should use same implementation as codegen :(
public class GlobalsVisitor extends AbstractParseTreeVisitor<String> implements TrinityVisitor<String> {

    private List<String> globals;

    public List<String> walk(ParseTree tree) {
        globals = new ArrayList<String>();
        tree.accept(this);

        return globals;
    }

    @Override
    public String visitProg(TrinityParser.ProgContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public String visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public String visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        globals.add(ctx.type().accept(this) + " " + ctx.ID().getText());
        return null;
    }

    @Override
    public String visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        if (ctx.getText().equals("Boolean")) {
            return "bool";
        }
        // Scalar
        return "float";
    }

    @Override
    public String visitVectorType(TrinityParser.VectorTypeContext ctx) {
        return "float*";
    }

    @Override
    public String visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        return "float*";
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
    public String visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        return null;
    }

    @Override
    public String visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitForLoop(TrinityParser.ForLoopContext ctx) {
        return null;
    }

    @Override
    public String visitIfStatement(TrinityParser.IfStatementContext ctx) {
        return null;
    }

    @Override
    public String visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        return null;
    }

    @Override
    public String visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        return null;
    }

    @Override
    public String visitOr(TrinityParser.OrContext ctx) {
        return null;
    }

    @Override
    public String visitExponent(TrinityParser.ExponentContext ctx) {
        return null;
    }

    @Override
    public String visitParens(TrinityParser.ParensContext ctx) {
        return null;
    }

    @Override
    public String visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        return null;
    }

    @Override
    public String visitTranspose(TrinityParser.TransposeContext ctx) {
        return null;
    }

    @Override
    public String visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        return null;
    }

    @Override
    public String visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        return null;
    }

    @Override
    public String visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        return null;
    }

    @Override
    public String visitNot(TrinityParser.NotContext ctx) {
        return null;
    }

    @Override
    public String visitRelation(TrinityParser.RelationContext ctx) {
        return null;
    }

    @Override
    public String visitIdentifier(TrinityParser.IdentifierContext ctx) {
        return null;
    }

    @Override
    public String visitNumber(TrinityParser.NumberContext ctx) {
        return null;
    }

    @Override
    public String visitAnd(TrinityParser.AndContext ctx) {
        return null;
    }

    @Override
    public String visitNegate(TrinityParser.NegateContext ctx) {
        return null;
    }

    @Override
    public String visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        return null;
    }

    @Override
    public String visitEquality(TrinityParser.EqualityContext ctx) {
        return null;
    }

    @Override
    public String visitBoolean(TrinityParser.BooleanContext ctx) {
        return null;
    }

    @Override
    public String visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
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
    public String visitRange(TrinityParser.RangeContext ctx) {
        return null;
    }

    @Override
    public String visit(ParseTree tree) {
        // do not visit without list instantiation
        return null;
    }


}
