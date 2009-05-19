/**
 * 
 */
package com.mtgi.analytics.test;

import static org.junit.Assert.assertFalse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A test worker process, which spawns multiple threads to invoke
 * the runnable method of a {@link TestCase} and communicate the
 * resulting measurements back via an instance of {@link TestCallback}.
 * This class is serializable so that it can be passed between separate
 * processes over a TCP connection.
 */
public class TestProcess implements Serializable {
	
	private static final long serialVersionUID = -7983427510103113889L;

	private int testThreads;
	private int testLoop;
	private TestCase job;
	
	private transient TestCallback listener;

	public TestProcess(int testThreads, int testLoop, TestCase job) {
		super();
		this.testThreads = testThreads;
		this.testLoop = testLoop;
		this.job = job;
	}

	/**
	 * Set the callback used to receive test measurements.  Presumably
	 * this will be used to communicate results back to the master test
	 * process.
	 */
	public void setCallback(TestCallback cb) {
		this.listener = cb;
	}
	
	/**
	 * Execute the test, sending measurements back to the {@link #setCallback(TestCallback) listener}
	 * as they become available.  This method returns when all test threads have
	 * executed.
	 */
	public void run() throws Throwable {
		
		if (listener == null)
			throw new IllegalStateException("No callback to receive test data.");
		
		job.setUp();
		try {
			//run once and gc to establish steady state.
			job.run();
			System.gc();
			System.gc();
			Thread.sleep(100);
			
			//set up the test iteration.
			Semaphore in = new Semaphore(0),
					  out = new Semaphore(0);
			TestThread[] threads = new TestThread[testThreads];
			for (int t = 0; t < testThreads; ++t) {
				threads[t] = new TestThread(in, out);
				threads[t].start();
			}
			
			//turn 'em loose.
			in.release(testThreads);
			//wait for finish.
			out.acquire(testThreads);
	
			//verify that all of the test threads are actually finished
			for (TestThread t : threads) {
				t.join(600000);
				t.assertDone();
			}
		} finally {
			job.tearDown();
		}
	}
	
	/**
	 * Test process main method.  Takes a single command line argument, a network
	 * port number on which this process can communicate with the test driver.  
	 * The child process immediately connects to this port and reads a
	 * serialized instance of {@link TestProcess}, which is then executed.  
	 * Measurements made during the test execution are sent back to the test driver 
	 * using the same network connection.  After the test has completed, a negative 
	 * measurement value is written back to the test driver as a signal that all measurements
	 * are finished, and the process exits with a status of zero (indicating success) 
	 * or 1 (indicating failure).
	 */
	public static void main(String[] argv) {
		try {
			//connect back to driver process using provided port number
			final int port = Integer.parseInt(argv[0]);
			final Socket sock = new Socket("localhost", port);
			final ExecutorService ioQueue = Executors.newSingleThreadExecutor();
			try {
				final InputStream in = sock.getInputStream();
				final ObjectInputStream oin = new ObjectInputStream(in);
				final DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				//receive the test job from the driver process
				TestProcess job = (TestProcess)oin.readObject();

				//configure callback to send measurements back to the driver.
				//do socket I/O on a background thread to avoid interrupted the test run.
				job.setCallback(new TestCallback() {
					public void add(final long measure) {
						assert measure >= 0 : "Measurement cannot take less than 0ns";
						ioQueue.execute(new Runnable() {
							public void run() {
								try {
									out.writeLong(measure);
								} catch (IOException ioe) {
									//terminate as soon as we get an I/O error with the server.
									ioe.printStackTrace(System.err);
									System.exit(1);
								}
							}
						});
					}
				});
				
				//execute the test case.
				
				job.run();
				
				//do server EOF handshake
				ioQueue.submit(new Callable<Boolean>() {
					public Boolean call() {
						try {
							//tell server we're done
							out.writeLong(-1L);
							out.flush();
							//read ack back
							if (in.read() == 1)
								return true;
						} catch (IOException ioe) {
							ioe.printStackTrace(System.err);
						}
						return false;
					}
				}).get();
				
			} finally {
				sock.close();
			}
			
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			//any exceptions raised result in error termination code
			System.exit(1);
		}
		System.exit(0);
	}
	
	/**
	 * Interface used by {@link TestProcess} to send test measurements back to
	 * the test driver process.
	 * @see AbstractPerformanceTestCase
	 */
	public interface TestCallback {
		/** Receive a single test measurement. */
		public void add(long measurement);
	}
	
	/**
	 * Repeatedly invokes a {@link TestCase} and measure its resource consumption.
	 * Uses shared semaphores to coordinate timing with other test threads owned by the
	 * same process.
	 */
	private class TestThread extends Thread {
		
		private Throwable error;
		private Semaphore in, out;
		private InstrumentedTestCase job;
		
		public TestThread(Semaphore in, Semaphore out) {
			this.in = in;
			this.out = out;
			//instrument the test job if necessary
			this.job = InstrumentedTestCaseDecorator.instrument(TestProcess.this.job);
		}

		/**
		 * Raise an assertion error if the test thread has not exited
		 * or raised an error during execution.
		 */
		public void assertDone() throws Throwable {
			if (error != null)
				throw error;
			assertFalse("thread " + getName() + " complete", isAlive());
		}
		
		@Override
		public void run() {
			try {
				//wait for the process to signal a start
				in.acquire(1);
				for (int i = 0; i < testLoop; ++i) {
					try {
						job.run();
					} finally {
						listener.add(job.getLastRuntimeNanos());
					}
				}
			} catch (Throwable e) {
				error = e;
			} finally {
				//signal thread exit.
				out.release(1);
			}
		}
	}
}