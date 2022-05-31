package freework.proc.handle;

import org.junit.Test;

import static org.junit.Assert.*;

public class HandleTest {

    @Test
    public void testHandle() {
        Handle.Info info = Handle.current().info();
        System.out.println(info);
    }

}