package gpbench.benchmarks

import gpbench.CityIdGen
import gpbench.CityDao
import grails.transaction.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class BatchInsertIdGen extends GparsDaoBenchmark {

	boolean validate = true
	String bindingMethod //= 'grails'

	BatchInsertIdGen(boolean databinding = true, boolean validate = true, String bindingMethod = 'grails') {
		super(databinding, validate, bindingMethod)
	}

	@Override
	def execute() {
		assert CityIdGen.count() == 0
		insertGpars(cities, cityDao)
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
