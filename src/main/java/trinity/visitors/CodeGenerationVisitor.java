package trinity.visitors;

import com.google.common.base.Charsets;
import org.antlr.v4.runtime.ParserRuleContext;
import trinity.StaticMatrix;
import trinity.TrinityBaseVisitor;
import trinity.TrinityParser;
import trinity.TrinityVisitor;
import trinity.types.EnumType;
import trinity.types.MatrixType;
import trinity.types.PrimitiveType;
import trinity.types.Type;
import trinity.utils.Emitter;
import trinity.utils.UniqueId;

import java.io.IOException;
import java.net.URL;

import static com.google.common.io.Resources.getResource;

/**
 * Visitor for C code generation.
 */
public class CodeGenerationVisitor extends TrinityBaseVisitor<Void> implements TrinityVisitor<Void> {

    private static final StringBuilder mainBody = new StringBuilder();
    private static final StringBuilder funcBody = new StringBuilder();
    private static final StringBuilder globals = new StringBuilder();

    private final DependencyVisitor dependencyVisitor = new DependencyVisitor();
    private Emitter emitter;
    private int scopeDepth = 0;

    /**
     * Get C code output after calling visit.
     *
     * @return C code
     */
    public String getOutput() {
        StringBuilder output = new StringBuilder();

        output.append(stdlib());
        output.append(globals.toString());
        output.append(funcBody.toString());

        // Main body
        output.append("int main(void){");
        output.append(mainBody.toString());
        output.append("return 0;};");

        return output.toString();
    }

    private String stdlib() {
        URL test = getResource("stdtrinity.c");
        try {
            return com.google.common.io.Resources.toString(test, Charsets.UTF_8);
        } catch (IOException e) {
            System.err.println("error loading stdlib");
        }
        return "";
    }

    /**
     * Emit initialization code for vector and matrix literals in {@code ctx} expression.
     *
     * @param ctx parse tree rule to search
     */
    private void emitDependencies(ParserRuleContext ctx) {
        Iterable<StaticMatrix> matrices = ctx.accept(dependencyVisitor);

        // Initialize matrix arrays
        if (matrices != null) {
            for (StaticMatrix staticMatrix : matrices) {
                // Declare array
                //emitter.emit("float " + staticMatrix.id + "[" + staticMatrix.items.size() + "];");
                // TODO: free these
                emitter.emit("float* " + staticMatrix.id + " = malloc(" + staticMatrix.size + "*sizeof(float));");

                // TODO: this could be implemented as a visitor.
                // Init array elements from expressions or ranges
                int i = 0;
                for (TrinityParser.VectorContext vector : staticMatrix.rows) {
                    if (vector.exprList() != null) {
                        // Initialize each array element with expression.
                        for (TrinityParser.ExprContext expr : vector.exprList().expr()) {
                            emitter.emit(staticMatrix.id + "[" + i++ + "]=");
                            expr.accept(this);
                            emitter.emit(";");
                        }
                    } else if (vector.range() != null) {
                        // Initialize range of array elements with for loop.
                        int from = new Integer(vector.range().NUMBER(0).getText());
                        int to = new Integer(vector.range().NUMBER(1).getText());

                        String incId = UniqueId.next();
                        String valId = UniqueId.next();

                        emitter.emit("int " + incId + ";");
                        emitter.emit("int " + valId + ";");

                        String init = incId + "=" + i + "," + valId + "=" + from;
                        i += Math.abs(to - from) + 1;
                        String cond = incId + "<" + i;
                        String inc = incId + "++," + valId + (from < to ? "++" : "--");
                        emitter.emit("for(" + init + ";" + cond + "; " + inc + "){");
                        emitter.emit(staticMatrix.id + "[" + incId + "]=" + valId + ";");
                        emitter.emit("}");
                    }
                }

                assert i == staticMatrix.size;
            }
        }
    }

    @Override
    public Void visitProg(TrinityParser.ProgContext ctx) {
        // Reset scopeDepth and StringBuilders
        scopeDepth = 0;
        mainBody.setLength(0);
        funcBody.setLength(0);
        globals.setLength(0);

        emitter = new Emitter(mainBody);

        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstDecl(TrinityParser.ConstDeclContext ctx) {
        emitDependencies(ctx.semiExpr());
        if (scopeDepth == 0) {
            emitter.setContext(globals);

            ctx.type().accept(this);
            emitter.emit("_" + ctx.ID().getText());
            emitter.emit(";");

            emitter.restoreContext();

        } else {
            ctx.type().accept(this);
        }
        emitter.emit("_" + ctx.ID().getText());
        emitter.emit("=");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionDecl(TrinityParser.FunctionDeclContext ctx) {
        emitter.setContext(funcBody);

        ctx.type().accept(this);
        emitter.emit("_" + ctx.ID().getText());
        emitter.emit("(");
        if (ctx.formalParameters() != null) {
            ctx.formalParameters().accept(this);
        }
        emitter.emit("){");
        ctx.block().accept(this);
        emitter.emit("}");

        emitter.restoreContext();
        return null;
    }

    @Override
    public Void visitFormalParameters(TrinityParser.FormalParametersContext ctx) {
        ctx.formalParameter(0).accept(this);
        for (int i = 1; i < ctx.formalParameter().size(); i++) {
            emitter.emit(",");
            ctx.formalParameter(i).accept(this);
        }
        return null;
    }

    @Override
    public Void visitFormalParameter(TrinityParser.FormalParameterContext ctx) {
        ctx.type().accept(this);
        emitter.emit("_" + ctx.ID().getText());
        return null;
    }

    @Override
    public Void visitPrimitiveType(TrinityParser.PrimitiveTypeContext ctx) {
        if (ctx.getText().equals("Boolean")) {
            emitter.emit("bool ");
        } else /* Scalar */ {
            emitter.emit("float ");
        }
        return null;
    }

    @Override
    public Void visitVectorType(TrinityParser.VectorTypeContext ctx) {
        emitter.emit("float* ");
        return null;
    }

    @Override
    public Void visitMatrixType(TrinityParser.MatrixTypeContext ctx) {
        emitter.emit("float* ");
        return null;
    }

    @Override
    public Void visitBlock(TrinityParser.BlockContext ctx) {
        scopeDepth++;
        for (TrinityParser.StmtContext stmt : ctx.stmt()) {
            stmt.accept(this);
        }
        if (ctx.returnStmt() != null) {
            ctx.returnStmt().accept(this);
        }
        scopeDepth--;
        return null;
    }

    @Override
    public Void visitReturnStmt(TrinityParser.ReturnStmtContext ctx) {
        emitDependencies(ctx.semiExpr());
        emitter.emit("return ");
        ctx.semiExpr().accept(this);
        return null;
    }

    @Override
    public Void visitSemiExpr(TrinityParser.SemiExprContext ctx) {
        visitChildren(ctx);
        emitter.emit(";");
        return null;
    }

    @Override
    public Void visitForLoop(TrinityParser.ForLoopContext ctx) {
        emitDependencies(ctx.expr());
        MatrixType matrix = (MatrixType) ctx.expr().t;
        String incId = UniqueId.next();

        // TODO: refactor
        int size;
        String type, indexing;

        if (matrix.getRows() != 1) {
            // Vector in matrix
            size = matrix.getRows();
            type = "float*";
            indexing = "+IDX2R(" + incId + ",0," + matrix.getCols() + ")"; // pointer arithmetic
        } else {
            // Scalar in vector
            size = matrix.getCols();
            type = "float";
            indexing = "[" + incId + "]";
        }

        emitter.emit("int " + incId + ";");
        emitter.emit("for(" + incId + "=0;" + incId + "<" + size + "; " + incId + "++){");

        // Current scalar/vector being iterated
        emitter.emit(type + " _" + ctx.ID().getText() + "=");
        ctx.expr().accept(this); // matrix/vector pre-initialized id (ref).
        emitter.emit(indexing + ";");

        // For loop body
        ctx.block().accept(this);

        emitter.emit("}");

        return null;
    }

    @Override
    public Void visitIfStatement(TrinityParser.IfStatementContext ctx) {
        // Initialize any matrix dependencies in the if expressions
        for (int i = 0; i < ctx.expr().size(); i++) {
            emitDependencies(ctx.expr(i));
        }

        // If block
        emitter.emit("if(");
        ctx.expr(0).accept(this);
        emitter.emit("){");
        ctx.block(0).accept(this);

        // Else If block
        int i;
        for (i = 1; i < ctx.expr().size(); i++) {
            emitter.emit("}else if(");
            ctx.expr(i).accept(this);
            emitter.emit("){");
            ctx.block(i).accept(this);
        }

        // Else block
        if (ctx.block(i) != null) {
            emitter.emit("}else{");
            ctx.block(i).accept(this);
        }

        emitter.emit("}");
        return null;
    }

    @Override
    public Void visitBlockStatement(TrinityParser.BlockStatementContext ctx) {
        emitter.emit("{");
        ctx.block().accept(this);
        emitter.emit("}");
        return null;
    }

    @Override
    public Void visitPrintStatement(TrinityParser.PrintStatementContext ctx) {
        emitDependencies(ctx.semiExpr().expr());
        Type expType = ctx.semiExpr().expr().t;
        if (expType instanceof PrimitiveType) {
            if (((PrimitiveType) expType).getPType() == EnumType.SCALAR) {
                emitter.emit("print_s(");
                ctx.semiExpr().expr().accept(this);
                emitter.emit(");");
            } else {
                emitter.emit("print_b(");
                ctx.semiExpr().expr().accept(this);
                emitter.emit(");");
            }
        } else if (expType instanceof MatrixType) {
            emitter.emit("print_m(");
            ctx.semiExpr().expr().accept(this);
            emitter.emit("," + ((MatrixType) expType).getRows());
            emitter.emit("," + ((MatrixType) expType).getCols());
            emitter.emit(");");
        } else {
            emitter.emit("printf(" + expType.toString() + ");");
        }
        return null;
    }

    @Override
    public Void visitOr(TrinityParser.OrContext ctx) {
        ctx.expr(0).accept(this);
        emitter.emit("||");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitExponent(TrinityParser.ExponentContext ctx) {
        if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
            emitter.emit("pow(");
            ctx.expr(0).accept(this);
            emitter.emit(",");
            ctx.expr(1).accept(this);
            emitter.emit(")");
        } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
            emitter.emit("mfexpo(");
            ctx.expr(0).accept(this);
            emitter.emit("," + ((MatrixType) ctx.expr(0).t).getRows());
            emitter.emit(",");
            ctx.expr(1).accept(this);
            emitter.emit(")");
        }

        return null;
    }

    @Override
    public Void visitParens(TrinityParser.ParensContext ctx) {
        emitter.emit("(");
        ctx.expr().accept(this);
        emitter.emit(")");
        return null;
    }

    @Override
    public Void visitTranspose(TrinityParser.TransposeContext ctx) {
        MatrixType matrix = (MatrixType) ctx.expr().t;
        emitter.emit("transpose(");
        ctx.expr().accept(this);
        emitter.emit("," + matrix.getRows() + "," + matrix.getCols() + ")");
        return null;
    }

    @Override
    public Void visitMultiplyDivide(TrinityParser.MultiplyDivideContext ctx) {
        if (ctx.op.getText().equals("*")) {
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emitter.emit("*");
                ctx.expr(1).accept(this);
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof MatrixType) {
                MatrixType matrix1 = (MatrixType) ctx.expr(0).t;
                MatrixType matrix2 = (MatrixType) ctx.expr(1).t;
                if (matrix1.getRows() == 1 && matrix2.getRows() == 1) {
                    emitter.emit("dotProduct(");
                    ctx.expr(0).accept(this);
                    emitter.emit(",");
                    ctx.expr(1).accept(this);
                    emitter.emit("," + matrix1.getCols());
                    emitter.emit(")");
                } else {
                    emitter.emit("mmmult(");
                    ctx.expr(0).accept(this);
                    emitter.emit("," + matrix1.getRows() + "," + matrix1.getCols() + ",");
                    ctx.expr(1).accept(this);
                    emitter.emit("," + matrix2.getRows() + "," + matrix2.getCols() + ")");
                }
            } else if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof MatrixType) {
                emitter.emit("fmmult(");
                ctx.expr(0).accept(this);
                emitter.emit(",");
                ctx.expr(1).accept(this);
                emitter.emit("," + ((MatrixType) ctx.expr(1).t).getRows());
                emitter.emit("," + ((MatrixType) ctx.expr(1).t).getCols());
                emitter.emit(")");
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
                emitter.emit("fmmult(");
                ctx.expr(1).accept(this);
                emitter.emit(",");
                ctx.expr(0).accept(this);
                emitter.emit("," + ((MatrixType) ctx.expr(0).t).getRows());
                emitter.emit("," + ((MatrixType) ctx.expr(0).t).getCols());
                emitter.emit(")");
            } // TODO: smarter way to destinguish between Matrix*Primitive and Primitive*Matrix
        } else {
            // Op can only be "*" which we have done or "/" hence the else
            if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
                ctx.expr(0).accept(this);
                emitter.emit("/");
                ctx.expr(1).accept(this);
            } else if (ctx.expr(0).t instanceof MatrixType && ctx.expr(1).t instanceof PrimitiveType) {
                MatrixType matrix = (MatrixType) ctx.expr(0).t;
                emitter.emit("mfdiv(");
                ctx.expr(1).accept(this);
                emitter.emit(",");
                ctx.expr(0).accept(this);
                emitter.emit("," + matrix.getRows() + "," + matrix.getCols() + ")");
            }
        }
        return null;
    }

    @Override
    public Void visitDoubleIndexing(TrinityParser.DoubleIndexingContext ctx) {
        // TODO: bounds check
        emitter.emit("_" + ctx.ID().getText() + "[IDX2T((int)(");
        ctx.expr(0).accept(this);
        emitter.emit("),(int)(");
        ctx.expr(1).accept(this);
        emitter.emit(")," + ctx.dims.getCols() + ")]");
        return null;
    }

    @Override
    public Void visitSingleIndexing(TrinityParser.SingleIndexingContext ctx) {
        if (ctx.t instanceof MatrixType) {
            // Vector in matrix
            emitter.emit("_" + ctx.ID().getText() + "+IDX2T((int)(");
            ctx.expr().accept(this);
            emitter.emit("),1," + ctx.dims.getCols() + ")");
        } else {
            // Scalar in vector
            emitter.emit("_" + ctx.ID().getText() + "[(int)(");
            ctx.expr().accept(this);
            emitter.emit(")-1]");
        }
        return null;
    }

    @Override
    public Void visitNot(TrinityParser.NotContext ctx) {
        emitter.emit("!");
        ctx.expr().accept(this);
        return null;
    }

    @Override
    public Void visitRelation(TrinityParser.RelationContext ctx) {
        ctx.expr(0).accept(this);
        emitter.emit(ctx.op.getText());
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitIdentifier(TrinityParser.IdentifierContext ctx) {
        emitter.emit("_" + ctx.ID().getText());
        return null;
    }

    @Override
    public Void visitNumber(TrinityParser.NumberContext ctx) {
        emitter.emit(ctx.NUMBER().getText());
        return null;
    }

    @Override
    public Void visitAnd(TrinityParser.AndContext ctx) {
        ctx.expr(0).accept(this);
        emitter.emit("&&");
        ctx.expr(1).accept(this);
        return null;
    }

    @Override
    public Void visitNegate(TrinityParser.NegateContext ctx) {
        if (ctx.expr().t instanceof PrimitiveType) {
            emitter.emit("-");
            ctx.expr().accept(this);
        } else if (ctx.expr().t instanceof MatrixType) {
            MatrixType matrix = (MatrixType) ctx.expr().t;
            emitter.emit("fmmult(-1,");
            ctx.expr().accept(this);
            emitter.emit("," + matrix.getRows() + "," + matrix.getCols() + ")");
        }
        return null;
    }

    @Override
    public Void visitFunctionCall(TrinityParser.FunctionCallContext ctx) {
        emitter.emit("_" + ctx.ID().getText());
        emitter.emit("(");
        if (ctx.exprList() != null) {
            ctx.exprList().accept(this);
        }
        emitter.emit(")");
        return null;
    }

    @Override
    public Void visitEquality(TrinityParser.EqualityContext ctx) {
        // Expect both operands to have same type
        if (ctx.expr(0).t instanceof PrimitiveType) {
            // Both a Scalar or Boolean
            ctx.expr(0).accept(this);
            // Use same operator as trinity (== or !=)
            emitter.emit(ctx.op.getText());
            ctx.expr(1).accept(this);
        } else if (ctx.expr(0).t instanceof MatrixType) {
            MatrixType matrix = (MatrixType) ctx.expr(1).t;
            if (ctx.op.getText().equals("!=")) {
                emitter.emit("!");
            }
            emitter.emit("mmeq(");
            ctx.expr(0).accept(this);
            emitter.emit(",");
            ctx.expr(1).accept(this);
            emitter.emit("," + matrix.getRows() + "," + matrix.getCols() + ")");
        }

        return null;
    }

    @Override
    public Void visitBoolean(TrinityParser.BooleanContext ctx) {
        emitter.emit(ctx.BOOL().getText());
        return null;
    }

    @Override
    public Void visitAddSubtract(TrinityParser.AddSubtractContext ctx) {
        boolean addition = ctx.op.getText().equals("+");
        if (ctx.expr(0).t instanceof PrimitiveType && ctx.expr(1).t instanceof PrimitiveType) {
            ctx.expr(0).accept(this);
            emitter.emit(addition ? "+" : "-");
            ctx.expr(1).accept(this);
        } else {
            MatrixType matrix = (MatrixType) ctx.expr(0).t;
            emitter.emit(addition ? "mmadd(" : "mmsubt(");
            ctx.expr(0).accept(this);
            emitter.emit(",");
            ctx.expr(1).accept(this);
            emitter.emit("," + matrix.getRows() + "," + matrix.getCols() + ")");
        }
        return null;
    }

    @Override
    public Void visitExprList(TrinityParser.ExprListContext ctx) {
        ctx.expr(0).accept(this);
        for (int i = 1; i < ctx.expr().size(); i++) {
            emitter.emit(",");
            ctx.expr(i).accept(this);
        }
        return null;
    }

    @Override
    public Void visitMatrixLiteral(TrinityParser.MatrixLiteralContext ctx) {
        // Emit identifier to matrix array initialized with the dependency visitor.
        emitter.emit(ctx.ref);
        return null;
    }

    @Override
    public Void visitVectorLiteral(TrinityParser.VectorLiteralContext ctx) {
        // Emit identifier to vector array initialized with the dependency visitor.
        emitter.emit(ctx.ref);
        return null;
    }

    @Override
    public Void visitRange(TrinityParser.RangeContext ctx) {
        // Ranges are generated by emitDependencies().
        System.err.println("INTERNAL ERROR: visitRange should not be called.");
        return null;
    }

    @Override
    public Void visitVector(TrinityParser.VectorContext ctx) {
        // Vectors are generated by emitDependencies(), and referenced by visitVectorLiteral().
        System.err.println("INTERNAL ERROR: visitVector should not be called.");
        return null;
    }

    @Override
    public Void visitMatrix(TrinityParser.MatrixContext ctx) {
        // Matrices are generated by emitDependencies(), and referenced by visitMatrixLiteral().
        System.err.println("INTERNAL ERROR: visitMatrix should not be called.");
        return null;
    }

    @Override
    public Void visitSingleExpression(TrinityParser.SingleExpressionContext ctx) {
        emitDependencies(ctx.semiExpr());
        return null;
    }

    @Override
    public Void visitConstDeclaration(TrinityParser.ConstDeclarationContext ctx) {
        visitChildren(ctx);
        return null;
    }

}
