import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeVisitorTest {

    private ParseTree createParseTree(String str) {
        ANTLRInputStream input = new ANTLRInputStream(str);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);

        return parser.prog();
    }

    TypeVisitor vis;
    ParseTree tree;
    TestErrorReporter er;
    SymbolTable tab;

    @Before
    public void initialize() {
        er = new TestErrorReporter();
        tab = new HashSymbolTable();
        vis = new TypeVisitor(er, tab);
    }

    @Test
    public void testConstDecl() throws Exception {
        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Boolean b = true;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.BOOLEAN);
        tree = createParseTree("Scalar s = 1;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.SCALAR);
        tree = createParseTree("Vector v = [1,2];");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.VECTOR);
        tree = createParseTree("Matrix m = [1,2][3,4];");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.MATRIX);
    }

    @Test
    public void testSimpleBooleanConstDeclErrorReporting() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Boolean b = 1;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.BOOLEAN
                        + " but got "
                        + Type.TrinityType.SCALAR));

        tree = createParseTree("Boolean b = [1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.BOOLEAN
                        + " but got "
                        + Type.TrinityType.VECTOR));

        tree = createParseTree("Boolean b = [1][1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.BOOLEAN
                        + " but got "
                        + Type.TrinityType.MATRIX));
    }

    @Test
    public void testSimpleScalarConstDeclErrorReporting() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Scalar s = false;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.SCALAR
                        + " but got "
                        + Type.TrinityType.BOOLEAN));

        tree = createParseTree("Scalar s = [1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.SCALAR
                        + " but got "
                        + Type.TrinityType.VECTOR));

        tree = createParseTree("Scalar s = [1][1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.SCALAR
                        + " but got "
                        + Type.TrinityType.MATRIX));
    }

    @Test
    public void testSimpleVectorConstDeclErrorReporting() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Vector v = false;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.VECTOR
                        + " but got "
                        + Type.TrinityType.BOOLEAN));

        tree = createParseTree("Vector v = 1;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.VECTOR
                        + " but got "
                        + Type.TrinityType.SCALAR));

        tree = createParseTree("Vector v = [1][1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.VECTOR
                        + " but got "
                        + Type.TrinityType.MATRIX));
    }

    @Test
    public void testSimpleMatrixConstDeclErrorReporting() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Matrix m = false;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.MATRIX
                        + " but got "
                        + Type.TrinityType.BOOLEAN));

        tree = createParseTree("Matrix m = 1;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.MATRIX
                        + " but got "
                        + Type.TrinityType.SCALAR));

        tree = createParseTree("Matrix m = [1];");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(er.getErrorAmount() - 1)
                .equals("TYPE ERROR: Expected type "
                        + Type.TrinityType.MATRIX
                        + " but got "
                        + Type.TrinityType.VECTOR));
    }

    @Test
    public void testComplexConstDecl() throws Exception {
        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Boolean b = [5,4][1,5] <= [3,7][8,8] and ([4] == [7]) or 3 != 2;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.BOOLEAN);

        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Scalar s = 1 + 2 * (5 + 2) / 4;");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.SCALAR);

        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Vector v = [1,2] + [2,3] * ([5,4] + [2,1]);");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.VECTOR);

        // Check if the given ConstDecl have same types on LHS and RHS...
        tree = createParseTree("Matrix m = [1,2][2,1] + [2,3][4,5] * ([5,4][2,6] + [2,1][7,2]);");
        assertEquals(tree.getChild(0).getChild(0).accept(vis).getType(), Type.TrinityType.MATRIX);
    }

    @Test
    public void testWeirdConstDecl() throws Exception {
        // Check if the correct ErrorReport is thrown...
        tree = createParseTree("Boolean b = 4 <= (8 and 4) == 7 or 3 != 2;");
        tree.getChild(0).getChild(0).accept(vis);
        assertTrue(er.getError(0).equals("ERROR: Type error at AND."));
    }

}
