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

	GparsDaoBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}

	@Override
	def execute() {
		assert City.count() == 0
		cityDao.insertGpars(cities, [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ])
		assert City.count() == 115000
	}

}
