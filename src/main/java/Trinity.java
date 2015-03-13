import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Trinity {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            showUsage();
        }

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
            String is =  new String(encoded, Charset.defaultCharset());
            String out = compile(is);
            System.out.println(out);
        } catch (NoSuchFileException test) {
            System.out.println("file not found: " + test.getMessage());
            showUsage();
        }
    }

    private static void showUsage() {
        System.out.println("Usage: Trinity <source file>");
        System.exit(1);
    }

    private static String compile(String is) {
        ANTLRInputStream input = new ANTLRInputStream(is);
        TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();
        return tree.toStringTree(parser);
    }
}