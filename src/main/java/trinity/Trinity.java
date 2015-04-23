package trinity;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import trinity.visitors.PrettyPrintVisitor;
import trinity.visitors.TypeVisitor;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Trinity {
    static CommandLineOptions options = new CommandLineOptions();

    private static class CommandLineOptions {
        @Parameter(description = "filename")
        private List<String> files = new ArrayList<String>();

        @Parameter(names = {"-p", "--pretty"}, description = "Pretty Print mode")
        private boolean prettyPrint;

        @Parameter(names = {"-g", "--go"}, description = "Keep-on-trucking on error")
        private boolean notFailOnError;
    }

    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander(options, args);

        if (options.files.size() == 0) {
            jc.usage();
            System.exit(1);
        }
        Path filePath = Paths.get(options.files.get(0));

        try {
            byte[] encoded = Files.readAllBytes(filePath);

            String is = new String(encoded, Charset.defaultCharset());

            if(options.prettyPrint) {
                prettyPrint(is);
            } else {
                compile(is);
                //System.out.println(out);
            }

        } catch (NoSuchFileException ex) {
            System.out.println("File not found: " + ex.getMessage());
            System.exit(1);
            //jc.usage();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

    }

    private static void compile(String is) throws Exception {
        ParseTree tree = parse(is);

        ErrorReporter reporter = new StandardErrorReporter(!options.notFailOnError, is);
        SymbolTable table = new HashSymbolTable();

        TypeVisitor typeChecker = new TypeVisitor(reporter, table);

        typeChecker.visit(tree);
        if (reporter.getErrorAmount() > 0) {
            // TODO: custom ex ?
            throw new Exception("To many type errors aborting");
        }

        //return tree.toStringTree(parser);
    }


    private static ParseTree parse(String is) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(is);
        trinity.TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            // TODO: custom ex ?
            throw new Exception("wtf");
        }

        return tree;

    }

    private static void prettyPrint(String is) throws Exception {
        ParseTree tree = parse(is);
        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor();
        prettyPrinter.visit(tree);
    }
}