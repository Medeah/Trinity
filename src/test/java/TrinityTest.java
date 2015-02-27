import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class TrinityTest {

    @Test
    public void testAdd() throws Exception {
        assertEquals(5, Trinity.add(2,3));
    }

    @Test
    public void testParser() throws Exception  {
        //TODO: figure out how to import files instead lol
        String prog = "# Parser tests\n\n# Types and declarations (and comments)\nBoolean t = true;\nScalar s = 1;\nVector v = [1,5,6];\nMatrix m = [1,2][4,5];\n\n# Expressions\nScalar s = 1 + 2 * (3 + 4) / 1.5 - s;\n\n# Loops and enumeration\nfor Scalar s in [1..9] by 2 do\n    print(s);\nend\n\nfor Scalar s in v do\n    print(s);\nend\n\nfor Vector v in [1,2,3] do print(0); end\n\n# Conditionals\nif m == v * v then\n    Matrix x = [1][0];\nelseif 1 + 2 != 4 or 5 == 2 + 2 then\n    print(m);\nelseif true and false then\n    print(m);\nelse\n    Scalar leet = 1337;\nend\n\n# Function declaration\nMatrix f(Scalar s1, Scalar s2) do\n    Matrix m = [s1][s2];\n    return m;\nend\n\n# Functions calls\nMatrix n = f(5,20 +3);\nfire(param, param, gideon, 2+2, xz/lol-(os),arnejohn);\n";

        InputStream is = new ByteArrayInputStream(prog.getBytes(StandardCharsets.UTF_8));

        ANTLRInputStream input = new ANTLRInputStream(is);
        LibExprLexer lexer = new LibExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LibExprParser parser = new LibExprParser(tokens);

        ParseTree tree = parser.prog();

        System.out.println(tree.toStringTree(parser));
    }

}