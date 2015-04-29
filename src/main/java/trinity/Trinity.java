package trinity;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import trinity.CustomExceptions.ParseException;
import trinity.CustomExceptions.TypeCheckException;
import trinity.visitors.CodeGenerationVisitor;
import trinity.visitors.PrettyPrintVisitor;
import trinity.visitors.TypeVisitor;
import static com.google.common.io.Files.getNameWithoutExtension;

import java.io.PrintWriter;
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

        @Parameter(names = {"-i", "--indent"}, description = "Indentation width")
        private int indentation = 4;

        @Parameter(names = {"-g", "--go"}, description = "Keep-on-trucking on error")
        private boolean notFailOnError;

        @Parameter(names = {"-f", "--format"}, description = "Format the generated c code using indent")
        private boolean formatc;

        @Parameter(names = {"-c", "--ccompiler"}, description = "Name of c compiler command")
        private String ccompiler = "cc";
    }

    public static void main(String[] args) throws Exception {
        JCommander jc = new JCommander(options, args);

        //TODO: remove me son
        //options.files.add("src/test/resources/trinity/tests/parsing-tests-edit.tri");
        options.files.add("src/test/resources/trinity/tests/simple.tri");

        if (options.files.size() != 1) {
            jc.usage();
            System.exit(1);
        }
        String file = options.files.get(0);
        String filename = getNameWithoutExtension(file);
        Path filePath = Paths.get(file);

        try {
            byte[] encoded = Files.readAllBytes(filePath);

            String is = new String(encoded, Charset.defaultCharset());

            if (options.prettyPrint) {
                prettyPrint(is, options.indentation);
            } else {
                String out = compile(is);
                System.out.println(out);
                PrintWriter pw = new PrintWriter(filename + ".c");
                pw.println(out);
                pw.flush();
                if(options.formatc) {
                    Process process = new ProcessBuilder("indent",filename+".c").start();
                    if(process.waitFor() != 0) {
                        System.err.println("error running indent, do you have it installed?");
                    }
                }

                Process process = new ProcessBuilder(options.ccompiler,filename + ".c").start();
                if(process.waitFor() != 0) {
                    System.err.println("Error compiling c code");
                }
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

    private static String compile(String is) throws Exception {
        ParseTree tree = parse(is);

        ErrorReporter reporter = new StandardErrorReporter(!options.notFailOnError, is);
        SymbolTable table = new HashSymbolTable();

        TypeVisitor typeChecker = new TypeVisitor(reporter, table);

        typeChecker.visit(tree);
        if (reporter.getErrorAmount() > 0) {
            throw new TypeCheckException("To many type errors aborting");
        }

        CodeGenerationVisitor generator = new CodeGenerationVisitor();
        String out = generator.generate(tree);

        return out;
    }

    private static ParseTree parse(String is) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(is);
        trinity.TrinityLexer lexer = new TrinityLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TrinityParser parser = new TrinityParser(tokens);
        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() != 0) {
            throw new ParseException("Input contains syntax errors.");
        }

        return tree;

    }

    private static void prettyPrint(String is, int indentation) throws Exception {
        ParseTree tree = parse(is);
        PrettyPrintVisitor prettyPrinter = new PrettyPrintVisitor(indentation);
        prettyPrinter.visit(tree);
    }
}
