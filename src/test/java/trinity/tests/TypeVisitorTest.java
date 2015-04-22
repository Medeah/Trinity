package trinity.tests;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Before;
import org.junit.Test;
import trinity.*;
import trinity.types.EnumType;
import trinity.types.PrimitiveType;
import trinity.types.Type;
import trinity.visitors.TypeVisitor;


import static org.junit.Assert.*;

public class TypeVisitorTest {

    private boolean TypeCheck(String str) throws Exception {
        ErrorReporter er = new StandardErrorReporter(false);
        SymbolTable tab = new HashSymbolTable();
        TypeVisitor typeVisitor = new TypeVisitor(er, tab);

        ANTLRInputStream input = new ANTLRInputStream(str);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);

        ParseTree tree = parser.prog();

        int lol = parser.getNumberOfSyntaxErrors();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            //TODO: custom excpe..
            throw new Exception("Parse error, invalid test.");
        }

        tree.accept(typeVisitor);

        return er.getErrorAmount() == 0;
    }

    private final Type bool = new PrimitiveType(EnumType.BOOLEAN);
    private final Type scal = new PrimitiveType(EnumType.SCALAR);

    @Test
    public void testSimpleDcl1() throws Exception {
        assertTrue(TypeCheck("Scalar q = 2;"));
    }

    @Test
    public void testSimpleDcl2() throws Exception {
        assertFalse(TypeCheck("Vector[1] q = [3,3,4];"));
    }

    @Test
    public void testConstDecl_VectorDimensions() throws Exception {
        assertTrue(TypeCheck("Vector[3] q = [3,3,4];"));
    }

    @Test
    public void testConstDecl_Boolean() throws Exception {
        assertTrue(TypeCheck("Boolean b = true;"));
        assertTrue(TypeCheck("Boolean b = true or false;"));
        assertTrue(TypeCheck("Boolean b = 4 <= 4 + 1 and 3 != 2;"));
        assertFalse(TypeCheck("Boolean b = 4 <= (8 and 4) == 7 or 3 != 2;"));
        assertTrue(TypeCheck("Boolean b = [2,2] == [2,2] and [1,2][3,4] == [3,4][5,5];"));
    }

    @Test
    public void testConstDecl_MatrixDimensions() throws Exception {
        assertTrue(TypeCheck("Matrix[2,2] m = [1,2][3,4];"));
        assertTrue(TypeCheck("Matrix[2,3] m = [1,2,5][3,4,6];"));
    }

    @Test
    public void testConstDecl_MatrixVectorDeclaration() throws Exception {
        assertFalse(TypeCheck("Matrix[2,2] m = [1,2];"));
    }

    @Test
    public void testConstDecl_ArithmeticExpresssions() throws Exception {
        assertTrue(TypeCheck("Scalar q = 2 * 2;"));
        assertTrue(TypeCheck("Scalar s = 1 + 2 * (5 + 2) / 4;"));
        assertTrue(TypeCheck("Vector[2] v = [1,2] + [2,3] - ([5,4] + [2,1]);"));
        assertTrue(TypeCheck("Matrix[2,2] m = [1,2][2,1] + [2,3][4,5] * ([5,4][2,6] + [2,1][7,2]);"));
    }

    @Test
    public void testVectorMatrixMultiplication() throws Exception {
        assertFalse(TypeCheck("Scalar s = [2,3] * [2,3,4];"));
        assertTrue(TypeCheck("Scalar s = [2,3,4] * [2,3,4];"));
        assertTrue(TypeCheck("Matrix[3,3] m = [2,3][4,5][5,6] * [2,3,4][6,7,8];"));
        assertFalse(TypeCheck("[2,3][4,5][5,6] * [2,3,4][6,7,8][1,2,3];"));
    }

    @Test
    public void testIndexing() throws Exception {
        assertTrue(TypeCheck("Vector[3] v = [1,2,3]; Scalar s = v[2];"));
        assertFalse(TypeCheck("Matrix[2,3] v = [1,2,3][3,3,3]; Scalar s = v[2];"));
        assertTrue(TypeCheck("Matrix[2,3] m = [1,2,3][3,3,3]; Scalar s = m[1,2];"));
        assertTrue(TypeCheck("Matrix[2,3] m = [1,2,3][3,3,3]; Vector[3] v = m[2];"));
        assertTrue(TypeCheck("Vector[3] v = [1,2,3]; Scalar s = v[1,2];"));

    }

    @Test
    public void matrixArithmetic() throws Exception {
        assertTrue(TypeCheck("Matrix[2,2] m = [1,2][3,4] * 3;"));
        assertTrue(TypeCheck("Matrix[2,2] m = 68717418 * [1,2][3,4];"));
        assertTrue(TypeCheck("Matrix[2,3] m = [1,2][3,4] * [3,4,4][1,2,3];"));
    }



}
