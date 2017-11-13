package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsBatchInsertBenchmark extends BaseBatchInsertBenchmark implements GparsBenchmark, BatchInsertBenchmark {

	GparsBatchInsertBenchmark(boolean databinding) {
		super(databinding)
	}

	@Override
	def execute() {
		assert City.count() == 0
		insert(cities, cityDao)
		assert City.count() == 115000
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void insert(List<List<Map>> batchList, CityDao dao) {
		GParsPool.withPool(poolSize) {
			batchList.eachParallel { List<Map> batch ->
				insertBatch(batch, dao)
			}
		}
	}

	@Transactional
	void insertBatch(List<Map> batch, CityDao dao) {
		for (Map record : batch) {
			try {
				if (useDatabinding) dao.insert(record)
				else dao.insertWithSetter(record)
			}catch (Exception e) {
				e.printStackTrace()
			}
		}

		DaoUtil.flushAndClear()
	}

	@Override
	String getDescription() {
		return "GparsBatchInsert:databinding=${useDatabinding}"
	}
}
