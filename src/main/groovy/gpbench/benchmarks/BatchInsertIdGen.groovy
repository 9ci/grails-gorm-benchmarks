package gpbench.benchmarks

import gpbench.CityDao
import gpbench.CityIdGen
import gpbench.CityIdGenDao
import grails.transaction.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class BatchInsertIdGen extends GparsDaoBenchmark {

	CityIdGenDao cityIdGenDao

	BatchInsertIdGen(boolean databinding = true, boolean validate = true, String bindingMethod = 'grails') {
		super(databinding, validate, bindingMethod)
	}

	@Override
	void cleanup() {
		super.cleanup()
		jdbcTemplate.execute("DELETE FROM city_id_gen")
	}

	@Override
	def execute() {
		assert CityIdGen.count() == 0
		insertGpars(cities, cityDao)
		assert CityIdGen.count() == 115000
	}

	@Transactional
	void insertRow(Map row, CityDao dao) {
		cityIdGenDao.insert(row, [validate:validate, bindingMethod:bindingMethod ])
	}

	@Override
	String getDescription() {
		return "BatchInsertIdGen: bindingMethod=${bindingMethod} validate=${validate}"
	}
}
