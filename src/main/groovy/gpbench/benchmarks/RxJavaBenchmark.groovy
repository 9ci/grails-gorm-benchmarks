package gpbench.benchmarks

import gpbench.CityBaseline
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class RxJavaBenchmark extends GparsBaselineBenchmark {

	RxJavaBenchmark(Class domain) {
		super(domain)
	}

	@Override
	def execute() {
		Flowable<List<Map>> stream = Flowable.fromIterable(cities)
		stream.parallel().runOn(Schedulers.computation()).map({ List<Map> batch ->
			//println "${batch.size()} Thread : " + Thread.currentThread().name
			insertBatch(batch)
			return true
		}).sequential().blockingForEach({  })
	}

	@Transactional
	@CompileStatic(TypeCheckingMode.SKIP)
	void insertBatch(List<Map> batch) {
		for(Map row : batch) {
			insertRow(row)
		}

		DaoUtil.flushAndClear()
	}

}
