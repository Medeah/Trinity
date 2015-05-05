package trinity.tests;

import com.google.common.io.CharStreams;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import trinity.Trinity;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileSystems;

import static java.nio.file.Files.delete;
import static org.junit.Assert.assertEquals;

public class CodeGenerationVisitorTest {

    private static String getOutput(String input) throws Exception {
        String out = Trinity.compiles(input);
        PrintWriter pw = new PrintWriter("test.c");
        pw.println(out);
        pw.flush();

        Process process = new ProcessBuilder("gcc", "test.c", "-lm").start();
        if (process.waitFor() != 0) {
            throw new Exception("Error compiling c code");
        }

        Process prog = new ProcessBuilder("./a.out").start();
        if (prog.waitFor() != 0) {
            throw new Exception("program exited with non zero exit code");
        }

        InputStreamReader inr = new InputStreamReader(prog.getInputStream());
        return CharStreams.toString(inr);
    }

    @AfterClass
    public static void removeFiles() throws Exception {
        delete(FileSystems.getDefault().getPath("test.c"));
        delete(FileSystems.getDefault().getPath("a.out"));
    }

    @Test
    public void parseReturn() throws Exception {
        assertEquals("1.000000\n", getOutput("print 1;"));
    }

    //TODO : mange flere tests
    @Ignore
    public void sin() throws Exception {
        assertEquals("1.000000\n", getOutput("print sin(1);"));
    }

    @Test
    public void forLoop() throws Exception {
        assertEquals("1.000000\n2.000000\n3.000000\n4.000000\n", getOutput("for Scalar s in [1,2,3,4] do print s; end"));
        assertEquals("[1.000000, 2.000000]\n[3.000000, 4.000000]\n", getOutput("for Vector[2] v in [1,2][3,4] do print v; end"));
        assertEquals("1.000000\n2.000000\n3.000000\n4.000000\n", getOutput("for Vector[2] v in [1,2][3,4] do for Scalar s in v do print s; end end"));

        assertEquals("4.000000\n6.000000\n", getOutput("Vector[2] v = [4,6]; for Scalar s in v do print s; end"));
    }
    @Test
    public void declarations() throws Exception {
        assertEquals("4.000000\n", getOutput("Scalar s = 4; print s;"));
        assertEquals("[4.000000, 6.000000]\n", getOutput("Vector[2] v = [4,6]; print v;"));
        assertEquals("[1.000000, 3.000000, 5.000000]\n[2.000000, 4.000000, 6.000000]\n", getOutput("Matrix[2,3] m = [1,2,3][4,5,6]; print m;"));
        assertEquals("true\n", getOutput("Boolean b = true; print b;"));

    }



}