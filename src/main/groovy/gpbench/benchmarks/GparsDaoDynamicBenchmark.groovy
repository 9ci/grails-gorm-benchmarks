package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import gpbench.CityDynamic
import gpbench.CityDynamicDao
import gpbench.GparsLoadService
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool

/**
 *Fuly dynamic compile with liberal use of defs and no typing
 */
class GparsDaoDynamicBenchmark extends BaseBatchInsertBenchmark  {

	GparsLoadService gparsLoadService
	CityDynamicDao cityDynamicDao

	GparsDaoDynamicBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}

	@Override
	def execute() {
		assert CityDynamic.count() == 0
		def args = [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ]
		gparsLoadService.insertGpars(cities, args){ row, zargs ->
			cityDynamicDao.insert(row, zargs)
		}
		//cityDynamicDao.insertGpars(cities, [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ])
		assert CityDynamic.count() == 115000
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city_dynamic")
	}
}
