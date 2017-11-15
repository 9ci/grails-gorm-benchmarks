package gpbench.benchmarks

import gpbench.City
import grails.core.GrailsApplication
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class RxJavaBenchmark extends GparsBaselineBenchmark {

	RxJavaBenchmark(boolean databinding = true) {
		super(databinding)
	}

	@Override
	def execute() {
		assert City.count() == 0
		Flowable<List<Map>> stream = Flowable.fromIterable(cities)
		stream.parallel().runOn(Schedulers.computation()).map({ List<Map> batch ->
			//println "${batch.size()} Thread : " + Thread.currentThread().name
			insertBatch(batch)
			return true
		}).sequential().blockingForEach({  })

		assert City.count() == 115000
	}

	@Transactional
	void insertRow(Map row) {
		cityDao.insert(row)
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city")
	}

	@Override
	String getDescription() {
		return "RxJava: databinding=${useDatabinding}, validation:${validate}"
	}
}
