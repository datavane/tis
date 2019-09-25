import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class TestThreadPoolExecutor extends TestCase {

	public void testPool() throws Exception {
//		ThreadPoolExecutor es = new BlockThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
//				new LinkedBlockingQueue<Runnable>(3));
//		// es.setRejectedExecutionHandler(new CallerRunsPolicy());
//
//		for (int i = 0; i < 100; i++) {
//			final int ii = i;
//			es.execute(() -> {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					throw new RuntimeException();
//				}
//				System.out.println("executor:" + ii);
//			});
//		}
//
//		Thread.sleep(10000);

	}

	
}
