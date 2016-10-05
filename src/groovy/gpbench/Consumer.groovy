package gpbench

import java.util.concurrent.BlockingQueue

class Consumer {
	private BlockingQueue queue

	public Consumer(BlockingQueue queue) {
		this.queue = queue
	}

	public void start(Closure cl) {
		def taken
		while((taken = queue.take()) instanceof List) {
			cl(taken)
		}
	}

}
