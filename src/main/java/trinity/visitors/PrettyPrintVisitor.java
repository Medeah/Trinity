package trinity.visitors;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;

public class PrettyPrintVisitor extends TrinityBaseVisitor<Object> implements TrinityVisitor<Object> {

    private int indentLevel = 0;
    private int spaces = 0;
    //TODO: fix output method
    private String outputString = "";

    private void print(String string) {
        System.out.print(string);
        outputString += string;
    }

    private void print(ParseTree node) {
        print(node.getText());
    }

    private void print(Token token) {
        print(token.getText());
    }

    private void indent() {
        print(Strings.repeat(" ", indentLevel * spaces));
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
            print(System.lineSeparator());
        }
        return null;
    }

    @Override
    public Object visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        ctx.type().accept(this);
        print(ctx.ID());
        print(" = ");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Object visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        ctx.type().accept(this);
        print(ctx.ID());
        print(" (");
        if (ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        print(") do");
        ctx.block().accept(this);
        print("end");
        return null;
    }

    @Override
    public Object visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        ctx.formalParameter(0).accept(this);
        for (int i = 1; i < ctx.formalParameter().size(); i++) {
            print(", ");
            ctx.formalParameter(i).accept(this);
        }
        return null;
    }

    @Override
    public Object visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        ctx.type().accept(this);
        print(ctx.ID());
        return null;
    }

    @Override
    public Object visitType(TrinityParser.TypeContext ctx) {
        print(ctx.TYPE());
        if (ctx.size() != null)
            ctx.size().accept(this);
        print(" ");
        return null;
    }

    @Override
    public Object visitBlock(TrinityParser.BlockContext ctx) {
        print(System.lineSeparator());
        this.indentLevel++;
        for (int i = 0; i < ctx.stmt().size(); i++) {
            indent();
            ctx.stmt(i).accept(this);
            print(System.lineSeparator());
        }
        if(ctx.semiExpr() != null) {
            indent();
            print("return ");
            ctx.semiExpr().accept(this);
            print(System.lineSeparator());
        }
        this.indentLevel--;
        indent();
        return null;
    }

    @Override
    public Object visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        ctx.expr().accept(this);
        print(";");
        return null;
    }

    @Override
    public Object visitOr(TrinityParser.OrContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitExponent(TrinityParser.ExponentContext ctx) {
        ctx.expr(0).accept(this);
        print(ctx.op);
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitMatrixIndexing(TrinityParser.MatrixIndexingContext ctx) {
        print(ctx.ID());
        print("[");
        ctx.expr(0).accept(this);
        print(", ");
        ctx.expr(1).accept(this);
        print("]");
        return null;
    }

    @Override
    public Object visitForLoop(TrinityParser.ForLoopContext ctx) {
        print("for ");
        ctx.type().accept(this);
        print(ctx.ID());
        print(" in ");
        ctx.expr().accept(this);
        print(" do");
        ctx.block().accept(this);
        print("end");
        return null;
    }

    @Override
    public Object visitIfStatement(TrinityParser.IfStatementContext ctx) {
        print("if ");
        ctx.expr(0).accept(this);
        print(" then");
        ctx.block(0).accept(this);

        int i;
        for(i = 1; i < ctx.expr().size(); i++) {
            print("elseif ");
            ctx.expr(i).accept(this);
            print(" then");
            ctx.block(i).accept(this);
        }

        if(ctx.block(i) != null) {
            print("else");
            ctx.block(i).accept(this);
        }

        print("end");
        return null;
    }

    @Override
    public Object visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        print("do");
        ctx.block().accept(this);
        print("end");
        return null;
    }

    @Override
    public Object visitAddSub(TrinityParser.AddSubContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitParens(TrinityParser.ParensContext ctx) {
        print("(");
        ctx.expr().accept(this);
        print(")");
        return null;
    }

    @Override
    public Object visitTranspose(TrinityParser.TransposeContext ctx) {
        ctx.expr().accept(this);
        print("'");
        return null;
    }

    @Override
    public Object visitNot(TrinityParser.NotContext ctx) {
        print("!");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Object visitRelation(TrinityParser.RelationContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitIdentifier(TrinityParser.IdentifierContext ctx) {
        print(ctx.ID());
        return null;
    }

    @Override
    public Object visitNumber(TrinityParser.NumberContext ctx) {
        print(ctx.NUMBER());
        return null;
    }

    @Override
    public Object visitMultDivMod(TrinityParser.MultDivModContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitAnd(TrinityParser.AndContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitNegate(TrinityParser.NegateContext ctx) {
        print("-");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Object visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        print(ctx.ID().getText());
        print("(");
        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
        }
        print(")");
        return null;
    }

    @Override
    public Object visitVectorIndexing(TrinityParser.VectorIndexingContext ctx) {
        print(ctx.ID());
        print("[");
        ctx.expr().accept(this);
        print("]");
        return null;
    }

    @Override
    public Object visitEquality(TrinityParser.EqualityContext ctx) {
        ctx.expr(0).accept(this);
        print(" ");
        print(ctx.op);
        print(" ");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Object visitBoolean(TrinityParser.BooleanContext ctx) {
        print(ctx.BOOL());
        return null;
    }

    @Override
    public Object visitExprList(TrinityParser.ExprListContext ctx) {
        ctx.expr(0).accept(this);
        for(int i = 1; i < ctx.expr().size(); i++) {
            print(", ");
            ctx.expr(i).accept(this);
        }
        return null;
    }

    @Override
    public Object visitVector(TrinityParser.VectorContext ctx) {
        print("[");
        if(ctx.exprList() != null) {
            ctx.exprList().accept(this);
        } else {
            ctx.range().accept(this);
        }
        print("]");
        return null;
    }

    @Override
    public Object visitRange (TrinityParser.RangeContext ctx) {
        print(ctx.NUMBER(0));
        print("..");
        print(ctx.NUMBER(1));
        return null;
    }

    @Override
    public Object visitMatrixSize(TrinityParser.MatrixSizeContext ctx) {
        print("[");
        print(ctx.getChild(1).getText());
        print(", ");
        print(ctx.getChild(3).getText());
        print("]");
        return null;
    }

    @Override
    public Object visitVectorSize(TrinityParser.VectorSizeContext ctx) {
        print("[");
        print(ctx.getChild(1).getText());
        print("]");
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
