package trinity.tests;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import org.junit.Ignore;
import trinity.*;
import trinity.CustomExceptions.ParseException;
import trinity.visitors.TypeVisitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeVisitorTest {

    private boolean typeCheck(String str) throws Exception {
        ErrorReporter er = new StandardErrorReporter(false, str);
        SymbolTable tab = new HashSymbolTable();
        TypeVisitor typeVisitor = new TypeVisitor(er, tab);

        ANTLRInputStream input = new ANTLRInputStream(str);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);

        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            throw new ParseException("Invalid test.");
        }

        tree.accept(typeVisitor);

        return er.getErrorAmount() == 0;
    }

    @Test
    public void testConstDecl_Empty() throws Exception {
        assertTrue(typeCheck(""));
    }

    @Test
    public void testSimpleDcl1() throws Exception {
        assertTrue(typeCheck("Scalar q = 2;"));
    }

    @Test
    public void testSimpleDcl2() throws Exception {
        assertFalse(typeCheck("Vector[1] q = [3,3,4];"));
    }

    @Test
    public void testConstDecl_VectorDimensions() throws Exception {
        assertTrue(typeCheck("Vector[3] q = [3,3,4];"));
    }

    @Test
    public void testBooleanAndLogicalExpressions() throws Exception {
        assertTrue(typeCheck("Boolean b = true;"));
        assertTrue(typeCheck("Boolean b = true or false;"));
        assertTrue(typeCheck("Boolean b = 4 <= 4 + 1 and 3 != 2;"));
        assertFalse(typeCheck("Boolean b = 4 <= (8 and 4) == 7 or 3 != 2;"));
        assertTrue(typeCheck("Boolean b = [2,2] == [2,2] and [1,2][3,4] == [3,4][5,5];"));

        assertTrue(typeCheck("Boolean b = 1 == 2 and 1 < 2 and 1 > 2;"));
        assertTrue(typeCheck("Boolean b = 1 != 2 or true != false;"));
        assertTrue(typeCheck("Boolean b = 1 <= 2 and 1 >= 2;"));
        assertTrue(typeCheck("Boolean b = !true and !(1==2);"));
        assertFalse(typeCheck("Boolean b = !2;"));
        assertFalse(typeCheck("Boolean b = ![2];"));
    }

    @Test
    public void testConstDecl_MatrixDimensions() throws Exception {
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4];"));
        assertTrue(typeCheck("Matrix[2,3] m = [1,2,5][3,4,6];"));
    }

    @Test
    public void testConstDecl_MatrixDimensions2() throws Exception {
        assertFalse(typeCheck("[1,2][3];"));
        assertFalse(typeCheck("[1,2,5][3,4];"));
    }

    @Test
    public void testConstDecl_MatrixVectorDeclaration() throws Exception {
        assertFalse(typeCheck("Matrix[2,2] m = [1,2];"));
    }

    @Test
    public void testConstDecl_ArithmeticExpresssions() throws Exception {
        assertTrue(typeCheck("Scalar q = 2 * 2;"));
        assertTrue(typeCheck("Scalar s = 1 + 2 * (5 + 2) / -4;"));
        assertTrue(typeCheck("Vector[2] v = [1,2] + [2,3] - ([5,4] + [2,1]);"));
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][2,1] + [2,3][4,-5] * ([5,4][2,6] + [2,1][7,2]);"));
        assertTrue(typeCheck("Matrix[2,2] m = -[1,2][2,1];"));
        assertFalse(typeCheck("-true;"));
    }

    @Test
    public void testVectorMatrixMultiplication() throws Exception {
        assertFalse(typeCheck("Scalar s = [2,3] * [2,3,4];"));
        assertTrue(typeCheck("Scalar s = [2,3,4] * [2,3,4];"));
        assertTrue(typeCheck("Matrix[3,3] m = [2,3][4,5][5,6] * [2,3,4][6,7,8];"));
        assertFalse(typeCheck("[2,3][4,5][5,6] * [2,3,4][6,7,8][1,2,3];"));
    }

    @Test
    public void testIndexing() throws Exception {
        assertTrue(typeCheck("Vector[3] v = [1,2,3]; Scalar s = v[2];"));
        assertFalse(typeCheck("Matrix[2,3] v = [1,2,3][3,3,3]; Scalar s = v[2];"));
        assertTrue(typeCheck("Matrix[2,3] m = [1,2,3][3,3,3]; Scalar s = m[1,2];"));
        assertTrue(typeCheck("Matrix[2,3] m = [1,2,3][3,3,3]; Vector[3] v = m[2];"));
        assertTrue(typeCheck("Vector[3] v = [1,2,3]; Scalar s = v[1,2];"));
    }

    @Test
    public void matrixMultiplication() throws Exception {
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4] * 3;"));
        assertTrue(typeCheck("Matrix[2,2] m = 68717418 * [1,2][3,4];"));
        assertTrue(typeCheck("Matrix[2,3] m = [1,2][3,4] * [3,4,4][1,2,3];"));
    }

    @Test
    public void matrixAddition() throws Exception {
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4] + [1,2][3,4];"));
        assertFalse(typeCheck("Matrix[2,2] m = [1,2][3,4] + [1,2];"));
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4] - [1,2][3,4];"));
        assertFalse(typeCheck("Matrix[2,2] m = [1,2][3,4] - [1,2];"));
    }

    @Test
    public void testRange() throws Exception {
        assertTrue(typeCheck("Vector[5] m = [1..5];"));
        assertTrue(typeCheck("Matrix[2,3] m = [3..5][2..4];"));
        assertFalse(typeCheck("Vector[3] m = [1..2];"));
        assertFalse(typeCheck("Matrix[2,4] m = [1..3][3..5];"));
    }

    @Test
    public void testTranspose() throws Exception {
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4]';"));
        assertTrue(typeCheck("Matrix[2,3] m = [1,2,5][3,4,6]; Matrix[3,2] n = m';"));
        assertTrue(typeCheck("Vector[3] v = [1,2,3]; Matrix[3,1] m = v';"));
        assertTrue(typeCheck("Matrix[3,1] m = [1][2][3]; Vector[3] v = m'; Matrix[1,3] x = m';"));
        assertTrue(typeCheck("Matrix[3,2] m = [1,2][3,4][5,6]'';"));
        assertTrue(typeCheck("Matrix[1,1] m = [5]';"));
        assertFalse(typeCheck("Scalar s = 5';"));
        assertFalse(typeCheck("Matrix[3,2] m = ([1,2][3,4][5,6])';"));
    }

    @Test
    public void testExponent() throws Exception {
        assertTrue(typeCheck("Matrix[2,2] m = [1,2][3,4]^2;"));
        assertFalse(typeCheck("Matrix[3,2] m = [1,2][3,4][3,4]^2;"));
        assertFalse(typeCheck("Matrix[2,3] m = [1,2,4][3,4,6]^2;"));
        assertTrue(typeCheck("Scalar s = 3^4;"));
    }

    @Test
    public void testFunctionDeclaration() throws Exception {
        assertTrue(typeCheck("Boolean x() do end"));
        assertTrue(typeCheck("Scalar add (Scalar a, Scalar b) do return a + b; end"));
        assertTrue(typeCheck("Scalar add (Scalar a, Scalar b) do return a + b; end Scalar s = add(2,2);"));
        assertFalse(typeCheck("Scalar add (Scalar a, Scalar b) do return a + b; end Scalar s = add(2);"));
        assertFalse(typeCheck("Scalar x (Scalar s) do Scalar s = 3; return s; end"));
        assertTrue(typeCheck("Scalar s() do return 3; end Scalar t = s();"));
        assertTrue(typeCheck("Vector[3] mat(Vector[3] a, Vector[3] b, Vector[3] c) do return [a[1], b[2], c[3]]; end Vector[3] v = mat([3,4,5], [5,6,7], [1,8,7]);"));
        assertFalse(typeCheck("Scalar s = 2; s();"));
        //TODO: right now this is valid since parameters exists in body scope?
        //assertFalse(typeCheck("Scalar s (Scalar s) do return s; end Scalar t = s(1);"));
    }

    @Test
    public void testFunctionCall() throws Exception {
        assertTrue(typeCheck("Boolean x(Scalar s, Vector[2] v) do return true; end x(1, [1,2]);"));
        assertFalse(typeCheck("Boolean x(Scalar s, Vector[2] v) do return true; end x(true, 1);"));
    }

    @Test
    public void testReturnTypes() throws Exception {
        assertTrue(typeCheck("Boolean x() do return true; end"));
        assertTrue(typeCheck("Scalar x () do return 2; end"));
        assertTrue(typeCheck("Vector[2] x () do return [2,3]; end"));
        assertTrue(typeCheck("Matrix[2,2] x () do return [1,2][3,4]; end"));
        assertFalse(typeCheck("Boolean x() do return 1; end"));
        assertFalse(typeCheck("Scalar x () do return false; end"));
        assertFalse(typeCheck("Vector[1] x () do return false; end"));
        assertFalse(typeCheck("Matrix[2,2] x () do return false; end"));
        assertTrue(typeCheck("Boolean x() do if true then return true; elseif 1==1 then return true; else return false; end end"));
        assertFalse(typeCheck("Boolean x() do if true then return true; else return 1; end end"));
        assertTrue(typeCheck("Boolean x() do for Scalar s in [1..3] do return true; end end"));
    }

    @Test
    public void forLoopTest() throws Exception {
        assertTrue(typeCheck("Vector[3] v = [1,2,3]; for Scalar s in v do 1+1; end"));
        assertTrue(typeCheck("Matrix[3,2] m = [1,2][3,4][5,6]; for Vector[2] v in m do 1+1; end"));
        assertTrue(typeCheck("Matrix[3,2] m = [1,2][3,4][5,6]; for Vector[2] v in m do for Scalar s in v do 1+1; end end"));
        assertTrue(typeCheck("for Scalar s in [1,2,3,4,5,6,7,8,9] do 1+1; end"));
        assertTrue(typeCheck("for Scalar s in [6] do 1+1; end"));

        assertFalse(typeCheck("for Scalar s in [6] do 1+false; end"));

        assertFalse(typeCheck("for Scalar s in 3 > 4 do 1+1; end"));
        assertFalse(typeCheck("for Scalar s in 6 do 1+1; end"));
        assertFalse(typeCheck("for Scalar s in false do 1+1; end"));
        assertFalse(typeCheck("Matrix[3,2] m = [1,2][3,4][5,6]; for Vector[3] v in m do 1+1; end")); // Rows not cols
        assertFalse(typeCheck("Matrix[3,2] m = [1,2][3,4][5,6]; for Vector[3] v in m do for Scalar s in v do 1+1; end end")); // Rows not cols
    }

    @Test
    public void ifBirdsCouldFly() throws Exception {
        assertTrue(typeCheck("if true then 1+2; end"));
        assertTrue(typeCheck("if false then 1+3; elseif true then 1+2; end"));
        assertTrue(typeCheck("if false then 1+2; elseif true then 1+2; else 1+2; end"));
        assertTrue(typeCheck("if false then 1+2; else 1+2; end"));
        assertTrue(typeCheck("if true or false then 123+345; end"));
        assertTrue(typeCheck("if true and false then 123+345; end"));
        assertTrue(typeCheck("if false then 1+2; elseif false then 3+3; elseif true then 1+1; end "));
        assertTrue(typeCheck("Scalar s = 2; if s == 2 then 1+1; end"));
        assertTrue(typeCheck("Scalar s = 3; if s != 2 then 1+1; end"));
        assertTrue(typeCheck("Scalar s = 3; if s > 2 then 1+1; end"));
        assertTrue(typeCheck("Scalar s = 25; if (s/5) == 5 then 1+1; end"));


        assertFalse(typeCheck("if false then 1+2; elseif false then 3+3; elseif true then 1+true; end "));
        assertFalse(typeCheck("if false then true+false; end"));
        assertFalse(typeCheck("if true or false then [1,2,4][6,7,8][3,7,6]+345; end"));
        assertFalse(typeCheck("if true and false then 1+1; else 123+[1234,56,78]+false; end"));
        assertFalse(typeCheck("if 2 then 1+1; end"));
        assertFalse(typeCheck("if 2+3 then 1+1; end"));

    }

    @Test
    public void overflowStuff() throws Exception {
        //TODO: fuck
        //assertFalse(typeCheck("Scalar a = " + Float.MAX_VALUE + 1 + ";"));
        assertFalse(typeCheck("Vector[" + Integer.MAX_VALUE + 1 + "] a() do end"));
        assertFalse(typeCheck("Matrix[" + Integer.MAX_VALUE + 1 + ",2] a() do end"));
        assertTrue(typeCheck("Vector[" + Integer.MAX_VALUE + "] a() do end"));
        assertTrue(typeCheck("Matrix[" + Integer.MAX_VALUE + ",2] a() do end"));
    }

    @Test
    public void WeirdStuff() throws Exception {
        assertFalse(typeCheck("Boolean b = true;\n" +
                "\n" +
                "do\n" +
                "    return 1;\n" +
                "end\n" +
                "\n" +
                "Scalar s = 1;"));
    }



    /*@Test
    public void testTypeInference() throws Exception {
        // if you want it, then you should have put a ring on it?
        assertTrue(typeCheck("Matrix m = [1,2][3,4][5,6]; Matrix[3,2] n = m;"));
        assertFalse(typeCheck("Matrix m = [1,2][3,4]; Matrix[3,2] n = m;"));
        assertTrue(typeCheck("Vector v = [1,2,3,4]; Vector[4] w = v;"));
        assertFalse(typeCheck("Vector v = [1,2,3,4]; Vector[3] w = v;"));
    }*/


}
