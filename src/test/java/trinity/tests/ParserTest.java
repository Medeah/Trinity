package trinity.tests;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import trinity.*;

import static org.junit.Assert.*;

public class ParserTest {

    //TODO: spørg mathias haha
    @Test
    public void correctSyntax_parseFile() throws Exception  {
        InputStream is = this.getClass().getResourceAsStream("/trinity/tests/parsing-tests.tri");
        TrinityParser parser = createParser(is);

        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void correctSyntax_numbers() throws Exception  {
        assertTrue(canParse("0;"));
        assertTrue(canParse("1;"));
        assertTrue(canParse("-12;"));
        assertTrue(canParse("1.23;"));
        assertTrue(canParse("1.002;"));
        assertTrue(canParse("2.43E-9;"));
        assertTrue(canParse("1.13e3;"));
        assertTrue(canParse("-1e9;"));
    }

    @Test
    public void correctSyntax_Dimensions() throws Exception  {
        assertTrue(canParse("Vector[1] v = [1,2,3];"));
        assertTrue(canParse("Matrix[2,3] m = [1,2,3][1,2,4];"));
    }



    @Test
    public void wrongSyntax_numbers() throws Exception  {
        assertFalse(canParse("00;"));
        assertFalse(canParse("01;"));
        assertFalse(canParse("01.2;"));
    }

    @Test
    public void wrongSyntax_NoDimensions() throws Exception  {
        assertFalse(canParse("Vector v = [1,2,3];"));
        assertFalse(canParse("Matrix m = [1,2,3][1,2,4];"));
    }

    @Test
    public void correctSyntax_lastLineComment() throws Exception  {
        assertTrue(canParse("#comment"));
    }

    @Test
    public void badSyntax_noSemiColon() throws Exception  {
        assertFalse(canParse("Scalar a = 1"));
        assertFalse(canParse("func()"));
        assertFalse(canParse("3 + 3"));
    }

    @Test
    public void badSyntax_wrongType() throws Exception  {
        assertFalse(canParse("Matrixx m = [1][1];"));
        assertFalse(canParse("YOLOSWAG m = [1][1];"));
        assertFalse(canParse("m = 1;"));
        assertFalse(canParse("Scalar Scalar s = 1;"));
        assertFalse(canParse("Bool m() do end"));
        assertFalse(canParse("Boolean m(Scala r) do end"));
    }

    @Test
    public void badSyntax_wrongVectorMatrix() throws Exception  {
        assertFalse(canParse("[1,2,3;"));
        assertFalse(canParse("1,3];"));
        assertFalse(canParse("[1,2,];"));
        assertFalse(canParse("[1]4];"));
        assertFalse(canParse("[[1][4];"));
    }

    @Test
    public void badSyntax_wrongFunctionCall() throws Exception  {
        assertFalse(canParse("f(1,);"));
        assertFalse(canParse("f(,2,3);"));
    }

    @Test
    public void badSyntax_missingParamType() throws Exception  {
        assertFalse(canParse("Matrix m(var) do end"));
    }


    // Utility functions
    public Boolean canParse(String syntax) throws IOException {
        TrinityParser parser = createParser(syntax);
        parser.prog();
        int lol = parser.getNumberOfSyntaxErrors();
        return 0 == parser.getNumberOfSyntaxErrors();
    }

    public TrinityParser createParser(ANTLRInputStream input) throws IOException {
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        return parser;
    }

    public TrinityParser createParser(String string) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(string);
        return createParser(input);
    }

    public TrinityParser createParser(InputStream is) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(is);
        return createParser(input);
    }

}