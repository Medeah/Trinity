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

    @Test
    public void stdlib() throws Exception {
        assertEquals("1.000000\n", getOutput("print abs(-1);"));
        assertEquals("1.000000\n", getOutput("print abs(1);"));
        assertEquals("1.000000\n", getOutput("print round(1.23);"));
        assertEquals("1.000000\n", getOutput("print floor(1.23);"));
        assertEquals("2.000000\n", getOutput("print ceil(1.23);"));
        assertEquals("0.841471\n", getOutput("print sin(1);"));
        assertEquals("0.540302\n", getOutput("print cos(1);"));
        assertEquals("1.557408\n", getOutput("print tan(1);"));
        assertEquals("0.523599\n", getOutput("print asin(0.5);"));
        assertEquals("1.047198\n", getOutput("print acos(0.5);"));
        assertEquals("0.785398\n", getOutput("print atan(1);"));
        assertEquals("2.302585\n", getOutput("print log(10);"));
        assertEquals("2.000000\n", getOutput("print log10(100);"));
        assertEquals("10.000000\n", getOutput("print sqrt(100);"));
        assertEquals("1.414214\n", getOutput("print sqrt(2);"));
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
        assertEquals("[1.000000, 2.000000, 3.000000]\n[4.000000, 5.000000, 6.000000]\n", getOutput("Matrix[2,3] m = [1,2,3][4,5,6]; print m;"));
        assertEquals("true\n", getOutput("Boolean b = true; print b;"));
    }

    @Ignore
    public void negation() throws Exception {
        assertEquals("-1.000000\n", getOutput("Scalar m = 1; Scalar n = -m; print n;"));
        assertEquals("3.000000\n", getOutput("Scalar m= -1; print -m;"));
        assertEquals("[-1.000000, -2.000000, -3.000000, -4.000000]\n", getOutput("Vector[4] v = [1, 2, 3, 4]; Vector[4] o = -v; print o;"));
        assertEquals("[-1.000000, -2.000000]\n[-3.000000, -4.000000]\n[-5.000000, -1.000000]\n[-6.000000, -2.000000]\n", getOutput("Matrix[4,2] M = [1, 2][3, 4][5, 6][7, 8]; print -M;"));
    }

    @Ignore
    public void multiplication() throws Exception {
        assertEquals("[2, 4, 6, 8, 10]\n", getOutput("Vector[5] v = [1, 2, 3, 4, 5]; Scalar j = 2; print v*j;"));
    }

    @Test
    public void matrixTranspose() throws Exception {

    }



}