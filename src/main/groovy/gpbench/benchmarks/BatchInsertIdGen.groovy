package gpbench.benchmarks

import gpbench.CityIdGen
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
class BatchInsertIdGen extends GparsBatchInsertBenchmark {

	boolean validate = true
	String bindingMethod //= 'grails'

	BatchInsertIdGen(boolean databinding = true, boolean validate = true, String bindingMethod = 'grails') {
		super(databinding, validate, bindingMethod)
	}

	@Override
	def execute() {
		assert CityIdGen.count() == 0
		insert(cities, cityDao)
		assert CityIdGen.count() == 115000
	}

	@Transactional
	void insertRow(Map row, CityDao dao) {
		dao.insert(row, [validate:validate, bindingMethod:bindingMethod ])
	}

	@Override
	String getDescription() {
		return "BatchInsertIdGen: bindingMethod=${bindingMethod} validate=${validate}"
	}
}
