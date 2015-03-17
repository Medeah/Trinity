import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashSymbolTableTest {

    SymbolTable tab;
    Type scalar;
    Type vector;

    @Before
    public void initialize() {
        tab = new HashSymbolTable();
        scalar = new Type("Scalar");
        vector = new Type("Vector");
    }

    @Test
    public void testGetCurrentScopeLevel() throws Exception {
        assertEquals(1, tab.getCurrentScopeLevel());
        tab.openScope();
        assertEquals(2, tab.getCurrentScopeLevel());
        tab.closeScope();
        assertEquals(1, tab.getCurrentScopeLevel());
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
        assertEquals(null, tab.retrieveSymbol("y"));
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
}