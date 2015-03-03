import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class Trinity {
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if (args.length > 0) inputFile = args[0];
        InputStream is = System.in;
        if (inputFile != null) is = new FileInputStream(inputFile);
        ANTLRInputStream input = new ANTLRInputStream(is);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();
        System.out.println(tree.toStringTree(parser));
    }
}