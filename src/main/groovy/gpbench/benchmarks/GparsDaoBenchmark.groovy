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

	@Override
	String getDescription() {
		return "GparsDaoBenchmark: bindingMethod=${bindingMethod} validate=${validate}"
	}
}
