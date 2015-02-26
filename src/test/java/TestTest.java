import org.junit.*;

import static org.junit.Assert.*;

public class TestTest {

    @org.junit.Test
    public void testAdd() throws Exception {
        assertEquals(5, Test.add(2, 3));
    }
}