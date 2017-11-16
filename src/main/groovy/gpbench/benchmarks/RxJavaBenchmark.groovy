package gpbench.benchmarks

import grails.gorm.transactions.Transactional
import grails.plugin.dao.DaoUtil
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class RxJavaBenchmark<T> extends GparsBaselineBenchmark<T> {

	RxJavaBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
		super(clazz, bindingMethod,validate)
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
