package trinity.tests;

import trinity.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import trinity.CustomExceptions.SymbolAlreadyDefinedException;
import trinity.CustomExceptions.SymbolNotFoundException;
import trinity.types.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashSymbolTableTest {

    SymbolTable tab;
    Type scalar;
    Type vector;
    Type matrix;
    Type bool;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void initialize() {
        tab = new HashSymbolTable();
        scalar = new PrimitiveType(EnumType.SCALAR);
        vector = new MatrixType(1, 2);
        matrix = new MatrixType(2, 2);
        bool = new PrimitiveType(EnumType.BOOLEAN);
    }

    @Test
    public void testGetCurrentScopeLevel() throws Exception {
        assertEquals(1, tab.getCurrentScopeDepth());
        tab.openScope();
        assertEquals(2, tab.getCurrentScopeDepth());
        tab.closeScope();
        assertEquals(1, tab.getCurrentScopeDepth());
    }

    @Test
    public void testEnterSymbol() throws Exception {
        tab.enterSymbol("x", scalar);
        assertEquals(scalar, tab.retrieveSymbol("x"));
    }

    @Test
    public void testRetrieveSymbol() throws Exception {
        tab.enterSymbol("x", scalar);

        assertEquals(scalar, tab.retrieveSymbol("x"));
        exception.expect(SymbolNotFoundException.class);
        tab.retrieveSymbol("y");
    }

    @Test
    public void testOpenScope() throws Exception {
        tab.enterSymbol("x", scalar);
        tab.openScope();
        assertEquals(scalar, tab.retrieveSymbol("x"));
        tab.enterSymbol("x", vector);
        assertEquals(vector, tab.retrieveSymbol("x"));
    }

    @Test
    public void testCloseScope() throws Exception {
        tab.enterSymbol("x", scalar);
        tab.openScope();
        tab.enterSymbol("x", vector);
        assertEquals(vector, tab.retrieveSymbol("x"));
        tab.closeScope();
        assertEquals(scalar, tab.retrieveSymbol("x"));
    }

    @Test
    public void testDeclaredLocally() throws Exception {
        tab.enterSymbol("x", scalar);
        tab.openScope();
        tab.enterSymbol("y", vector);

        assertFalse(tab.declaredLocally("x"));
        assertTrue(tab.declaredLocally("y"));

        tab.closeScope();
        assertTrue(tab.declaredLocally("x"));
    }

    @Test
    public void testDeclaredLocally2() throws Exception {
        tab.openScope();
        tab.enterSymbol("x", scalar);
        assertTrue(tab.declaredLocally("x"));
        tab.closeScope();
        tab.openScope();
        assertFalse(tab.declaredLocally("x"));
        assertFalse(tab.declaredLocally("z"));
    }

    @Test
    public void testNesting() throws Exception {
        tab.openScope();
        tab.enterSymbol("x", scalar);
        tab.openScope();
        tab.enterSymbol("x", vector);
        tab.openScope();
        tab.enterSymbol("x", matrix);
        assertEquals(matrix, tab.retrieveSymbol("x"));
        tab.closeScope();
        assertEquals(vector, tab.retrieveSymbol("x"));
        tab.closeScope();
        assertEquals(scalar, tab.retrieveSymbol("x"));
        tab.closeScope();

        exception.expect(SymbolNotFoundException.class);
        tab.retrieveSymbol("x");
    }

    @Test
    public void testSymbolAlreadyDefined() throws SymbolAlreadyDefinedException {
        tab.enterSymbol("x", matrix);

        exception.expect(SymbolAlreadyDefinedException.class);

        tab.enterSymbol("x", matrix);
    }
}