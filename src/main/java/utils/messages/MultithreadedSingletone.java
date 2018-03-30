package utils.messages;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public abstract class MultithreadedSingletone<T> {
	public static final UUID SERVICE_UUID = UUID.randomUUID();

	public class Worker implements Runnable {
		@Override public void run() {
			while(true) {
				try {
					final T task = queue.pollLast(9999, TimeUnit.DAYS);
					if (Objects.nonNull(task)) {
						processQueue(task);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	private final ExecutorService executor;
	private final LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>();

	public abstract void processQueue(T element);

	public MultithreadedSingletone() {
		int threads = Runtime.getRuntime().availableProcessors();
		if (threads <= 0) throw new IllegalStateException("cannot get CPUs count");
		executor = Executors.newFixedThreadPool(threads);
		execute(threads);
	}

	public MultithreadedSingletone(int threads) {
		executor = Executors.newFixedThreadPool(threads);
		execute(threads);
	}

	private void execute(int threads) {
		for (int i=0; i<threads; i++) {
			final Worker runnable = new Worker();
			final Thread thread = new Thread(runnable);
			executor.submit(thread);
		}
	}

	public void pushTask(T t) {
		queue.add(t);
	}

	public void cancelTask(T t) {
		queue.remove(t);
	}

	public void disposeInstance() {
		executor.shutdownNow();
	}
}
