package com.github.sarxos.webcam;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebcamProcessor
 */
public class WebcamProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamProcessor.class);

	/**
	 * Thread doing supersync processing.
	 *
	 * @author sarxos
	 */
	protected static final class ProcessorThread extends Thread {

		private static final AtomicInteger N = new AtomicInteger(0);

		public ProcessorThread(Runnable r) {
			super(r, String.format("atomic-processor-%d", N.incrementAndGet()));
		}
		public void run() {}
	}

	/**
	 * Thread factory for processor.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class ProcessorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new ProcessorThread(r);
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Heart of overall processing system. This class process all native calls wrapped in tasks, by
	 * doing this all tasks executions are super-synchronized.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class AtomicProcessor implements Runnable {

		private SynchronousQueue<WebcamTask> inbound = new SynchronousQueue<WebcamTask>(true);
		private SynchronousQueue<WebcamTask> outbound = new SynchronousQueue<WebcamTask>(true);

		/**
		 * Process task.
		 *
		 * @param task the task to be processed
		 * @throws InterruptedException when thread has been interrupted
		 */
		public void process(WebcamTask task) throws InterruptedException {
			inbound.put(task);

			Throwable t = outbound.take().getThrowable();
			if (t != null) {
				throw new WebcamException("Cannot execute task", t);
			}
		}

		@Override
		public void run() {

			WebcamTask t = null;

			try {
				while (true) {
					(t = inbound.take()).handle();
				}
			} catch (InterruptedException e) {
				System.out.println("The thread has been interrupted");
			} catch (Throwable e) {
				if (t != null) {
					t.setThrowable(e);
				}
			} 
		}
	}

	/**
	 * Is processor started?
	 */
	private static final AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Execution service.
	 */
	private static ExecutorService runner = null;

	/**
	 * Static processor.
	 */
	private static final AtomicProcessor processor = new AtomicProcessor();

	/**
	 * Singleton instance.
	 */
	private static final WebcamProcessor INSTANCE = new WebcamProcessor();;

	private WebcamProcessor() {
	}

	/**
	 * Process single webcam task.
	 *
	 * @param task the task to be processed
	 * @throws InterruptedException when thread has been interrupted
	 */
	public void process(WebcamTask task) throws InterruptedException {

		if (started.compareAndSet(false, true)) {
			runner = Executors.newSingleThreadExecutor(new ProcessorThreadFactory());
			runner.execute(processor);
		}

		if (!runner.isShutdown()) {
			processor.process(task);
		} else {
			throw new RejectedExecutionException("Cannot process because processor runner has been already shut down");
		}
	}

	public void shutdown() {
		if (started.compareAndSet(true, false)) {

			LOG.debug("Shutting down webcam processor");

			runner.shutdown();

			LOG.debug("Awaiting tasks termination");

			try {boolean isRunnerTerminated = runner.isTerminated();
				while (isRunnerTerminated) {
					runner.awaitTermination(100, TimeUnit.MILLISECONDS);
					isRunnerTerminated = runner.isTerminated();
				}
			} catch (InterruptedException e) {
				return;
			}

			runner.shutdownNow();
		}

		LOG.debug("All tasks has been terminated");
	}


	public static  WebcamProcessor getInstance() {
		synchronized (INSTANCE) {
			return INSTANCE;
		}
	
	}
}
