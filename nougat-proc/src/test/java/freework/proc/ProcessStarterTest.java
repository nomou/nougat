package freework.proc;

import org.junit.Test;
import org.jvnet.winp.WinProcess;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 */
public class ProcessStarterTest {
    @Test
    public void test() throws IOException, InterruptedException {
        Collections x;
        /*
        final ProcessStarter starter = new ProcessStarter("bin");
        final int exitCode = starter.execute();
        System.out.println(exitCode);
        */

        final Iterator<Integer> it = new Iterator<Integer>() {
            final Iterator<WinProcess> all = WinProcess.all().iterator();

            @Override
            public boolean hasNext() {
                return all.hasNext();
            }

            @Override
            public Integer next() {
                return all.next().getPid();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        Iterator<Cmdline> cmd = new Iterator<Cmdline>() {
            final Iterator<Integer> all = it;

            @Override
            public boolean hasNext() {
                return all.hasNext();
            }

            @Override
            public Cmdline next() {
                try {
                    return Cmdline.resolve(all.next());
                } catch (final IOException e) {
                    // throw new IllegalStateException(e);
                    return null;
                }
            }

            @Override
            public void remove() {

            }
        };

//        final Cmdline cmdline = ProcessUtils.getCmdline(6644);
//        System.out.println(cmdline);
        while (cmd.hasNext()) {
            final Cmdline cmdline = cmd.next();
            System.out.println(cmdline);
        }
    }
}