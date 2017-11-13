package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts but without gpars.
 */
@CompileStatic
class SimpleBatchInsertBenchmark extends BaseBatchInsertBenchmark implements BatchInsertBenchmark {

	SimpleBatchInsertBenchmark(boolean databinding) {
		super(databinding)
	}

	@Override
	def execute() {
		assert City.count() == 0
		insert(cities, cityDao)
		assert City.count() == 115000
	}

	void insert(List<List<Map>> batchList, CityDao dao) {
		for (List<Map> batch : batchList) {
			insertBatch(batch, dao)
			DaoUtil.flushAndClear()
		}
	}

	@Transactional
	void insertBatch(List<Map> batch, CityDao dao) {
		for (Map record : batch) {
			try {
				if (useDatabinding) dao.insert(record)
				else dao.insertWithSetter(record)
			} catch (Exception e) {
				e.printStackTrace()
			}
		}
	}

	@Override
	String getDescription() {
		return "SimpleBatchInsert without gpars: databinding=${useDatabinding}"
	}
}
