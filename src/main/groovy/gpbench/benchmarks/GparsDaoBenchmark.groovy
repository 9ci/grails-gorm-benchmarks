package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsDaoBenchmark extends BaseBatchInsertBenchmark  {

	boolean validate = true
	String bindingMethod //= 'grails'

	GparsDaoBenchmark(boolean databinding = true, boolean validate = true, String bindingMethod = 'grails') {
		super(databinding)
		this.validate = validate
		this.bindingMethod = bindingMethod
	}

	@Override
	def execute() {
		assert City.count() == 0
		cityDao.insertGpars(cities, [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ])
		assert City.count() == 115000
	}

	@CompileDynamic
	void insertGpars(List<List<Map>> batchList, CityDao dao) {
		GParsPool.withPool(poolSize) {
			batchList.eachParallel { List<Map> batch ->
				insertBatch(batch, dao)
			}
		}
	}

	@Transactional
	void insertBatch(List<Map> batch, CityDao dao) {
		for (Map record : batch) {
			insertRow(record, dao)
		}
		DaoUtil.flushAndClear()
	}

	//@Transactional
	void insertRow(Map row, CityDao dao) {
		dao.insert(row, [validate:validate, bindingMethod:bindingMethod ])
	}

	@Override
	String getDescription() {
		return "GparsBatchInsert: bindingMethod=${bindingMethod} validate=${validate}"
	}
}
