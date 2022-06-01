package freework.proc;

import org.junit.Test;

import java.io.IOException;

public class HandleTest {

    @Test
    public void testHandle() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        Process proc = builder.command("vim", "--help").start();
        Handle handle = Handle.of(proc);
        System.out.println(handle.isAlive());
        System.out.println(handle.info());
//        System.out.println(proc.isAlive());

        handle.kill();

        System.out.println(proc.waitFor());
    }

}