package gpbench

import groovy.transform.CompileStatic

import java.util.concurrent.BlockingQueue

@CompileStatic
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

		println "Stopping consumer thread"
	}

}
