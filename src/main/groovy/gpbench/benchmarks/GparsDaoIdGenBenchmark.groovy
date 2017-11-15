package gpbench.benchmarks

import gpbench.City
import gpbench.CityIdGen
import gpbench.CityIdGenDao
import grails.transaction.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsDaoIdGenBenchmark extends GparsDaoBenchmark {

	CityIdGenDao cityIdGenDao
	GparsDaoIdGenBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}
	@Override
	def execute() {
		assert CityIdGen.count() == 0
		cityIdGenDao.insertGpars(cities, [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ])
		assert CityIdGen.count() == 115000
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city_id_gen")
	}
}
