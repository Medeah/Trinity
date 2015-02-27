import org.junit.Test;

import static org.junit.Assert.*;

public class TrinityTest {

    @Test
    public void testAdd() throws Exception {
        assertEquals(5, Trinity.add(2,3));
    }
}