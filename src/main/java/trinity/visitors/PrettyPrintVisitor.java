package trinity.visitors;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;
import trinity.types.Type;

public class PrettyPrintVisitor extends TrinityBaseVisitor<Object> implements TrinityVisitor<Object> {

    private int indentLevel = 0;
    private int spaces = 0;
    //TODO: fix output method
    private String outputString = "";

    private void emit(String string) {
        outputString += string;
    }

    private void emit(ParseTree node) {
        emit(node.getText());
    }

    private void emit(Token token) {
        emit(token.getText());
    }

    private void indent() {
        emit(Strings.repeat(" ", indentLevel * spaces));
    }

    public PrettyPrintVisitor() {
        this(4);
    }

    public PrettyPrintVisitor(int spaces) {
        this.spaces = spaces;
    }

    public String prettyfy(ParseTree tree) {
        this.visit(tree);
        return outputString;
    }

    @Override
    public Object visitProg(TrinityParser.ProgContext ctx) {
        for (ParseTree child : ctx.children) {
            child.accept(this);
            emit(System.lineSeparator());
        }
        return null;
    }

    @Override
    public Object visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        ctx.type().accept(this);
        emit(ctx.ID());
        emit(" = ");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Object visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        ctx.type().accept(this);
        emit(ctx.ID());
        emit(" (");
        if (ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        emit(") do");
        ctx.block().accept(this);
        emit("end");
        return null;
    }

    @Override
    public Object visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        ctx.formalParameter(0).accept(this);
        for (int i = 1; i < ctx.formalParameter().size(); i++) {
            emit(", ");
            ctx.formalParameter(i).accept(this);
        }
        return null;
    }

    @Override
    public Object visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        ctx.type().accept(this);
        emit(ctx.ID());
        return null;
    }

    @Override
    public Object visitBlock(TrinityParser.BlockContext ctx) {
        emit(System.lineSeparator());
        this.indentLevel++;
        for (int i = 0; i < ctx.stmt().size(); i++) {
            indent();
            ctx.stmt(i).accept(this);
            emit(System.lineSeparator());
        }
        if (ctx.semiExpr() != null) {
            indent();
            emit("return ");
            ctx.semiExpr().accept(this);
            emit(System.lineSeparator());
        }
        this.indentLevel--;
        indent();
        return null;
    }

    @Override
    public Object visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        ctx.expr().accept(this);
        emit(";");
        return null;
    }

    @Override
    public Object visitOr(TrinityParser.OrContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitExponent(TrinityParser.ExponentContext ctx) {
        ctx.expr(0).accept(this);
        emit(ctx.op);
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        emit(ctx.ID());
        emit("[");
        ctx.expr(0).accept(this);
        emit(", ");
        ctx.expr(1).accept(this);
        emit("]");
        return null;
    }

    @Override
    public Object visitForLoop(TrinityParser.ForLoopContext ctx) {
        emit("for ");
        ctx.type().accept(this);
        emit(ctx.ID());
        emit(" in ");
        ctx.expr().accept(this);
        emit(" do");
        ctx.block().accept(this);
        emit("end");
        return null;
    }

    @Override
    public Object visitIfStatement(TrinityParser.IfStatementContext ctx) {
        emit("if ");
        ctx.expr(0).accept(this);
        emit(" then");
        ctx.block(0).accept(this);

        int i;
        for (i = 1; i < ctx.expr().size(); i++) {
            emit("elseif ");
            ctx.expr(i).accept(this);
            emit(" then");
            ctx.block(i).accept(this);
        }

        if (ctx.block(i) != null) {
            emit("else");
            ctx.block(i).accept(this);
        }

        emit("end");
        return null;
    }

    @Override
    public Object visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        emit("do");
        ctx.block().accept(this);
        emit("end");
        return null;
    }

    @Override
    public Object visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitParens(TrinityParser.ParensContext ctx) {
        emit("(");
        ctx.expr().accept(this);
        emit(")");
        return null;
    }

    @Override
    public Object visitTranspose(TrinityParser.TransposeContext ctx) {
        ctx.expr().accept(this);
        emit("'");
        return null;
    }

    @Override
    public Object visitNot(TrinityParser.NotContext ctx) {
        emit("!");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Object visitRelation(TrinityParser.RelationContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitIdentifier(TrinityParser.IdentifierContext ctx) {
        emit(ctx.ID());
        return null;
    }

    @Override
    public Object visitNumber(TrinityParser.NumberContext ctx) {
        emit(ctx.NUMBER());
        return null;
    }

    @Override
    public Object visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitAnd(TrinityParser.AndContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitNegate(TrinityParser.NegateContext ctx) {
        emit("-");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Object visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        emit(ctx.ID().getText());
        emit("(");
        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
        }
        emit(")");
        return null;
    }

    @Override
    public Object visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        emit(ctx.ID());
        emit("[");
        ctx.expr().accept(this);
        emit("]");
        return null;
    }

    @Override
    public Object visitEquality(TrinityParser.EqualityContext ctx) {
        ctx.expr(0).accept(this);
        emit(" ");
        emit(ctx.op);
        emit(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitBoolean(TrinityParser.BooleanContext ctx) {
        emit(ctx.BOOL());
        return null;
    }

    @Override
    public Object visitExprList(TrinityParser.ExprListContext ctx) {
        ctx.expr(0).accept(this);
        for (int i = 1; i < ctx.expr().size(); i++) {
            emit(", ");
            ctx.expr(i).accept(this);
        }
        return null;
    }

    @Override
    public Object visitVector(TrinityParser.VectorContext ctx) {
        emit("[");
        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
        } else {
            ctx.range().accept(this);
        }
        emit("]");
        return null;
    }

    @Override
    public Object visitRange(TrinityParser.RangeContext ctx) {
        emit(ctx.NUMBER(0));
        emit("..");
        emit(ctx.NUMBER(1));
        return null;
    }

    @Override
    public Type visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        emit(ctx.getChild(0));
        emit(" ");
        return null;
    }

    @Override
    public Type visitVectorType(TrinityParser.VectorTypeContext ctx) {
        emit("Vector[");
        if (ctx.NUMBER() != null) {
            emit(ctx.NUMBER());
        } else {
            emit(ctx.ID());
        }
        emit("] ");
        return null;
    }

    @Override
    public Type visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        emit("Matrix[");
        if (ctx.NUMBER(0) != null) {
            emit(ctx.NUMBER(0));
        } else {
            emit(ctx.ID(0));
        }
        emit(",");
        if (ctx.NUMBER(1) != null) {
            emit(ctx.NUMBER(1));
        } else {
            emit(ctx.ID(1));
        }

        emit("] ");
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        return null;
    }

    // TODO: comment stuff / remove
//    BufferedTokenStream tokens;
//    TokenStreamRewriter rewriter;
//
//    public trinity.visitors.PrettyPrintVisitor(BufferedTokenStream tokens) {
//        this.tokens = tokens;
//        rewriter = new TokenStreamRewriter(tokens);
//    }

//        Token semi = ctx.getStop();
//        int i = semi.getTokenIndex();
//        List<Token> cmtChannel = tokens.getHiddenTokensToRight(i, TrinityLexer.COMMENT);
//        if (cmtChannel != null) {
//            Token cmt = cmtChannel.get(0);
//            if (cmt != null) {
//                String txt = cmt.getText().substring(2);
//                String newCmt = "/* " + txt.trim() + " */\n";
//                rewriter.insertBefore(ctx.start, newCmt);
//                rewriter.replace(cmt, "\n");
//            }
//        }

}
