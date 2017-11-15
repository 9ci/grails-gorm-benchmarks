package gpbench.benchmarks

import gorm.tools.GormUtils
import gpbench.CityBaseline
import gpbench.Country
import gpbench.GparsLoadService
import gpbench.Region
import grails.plugin.dao.DaoUtil
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
class GparsBaselineBenchmark extends BaseBatchInsertBenchmark {

	GparsBaselineBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}

	@Override
	def execute() {
		assert CityBaseline.count() == 0
		//insertGpars(cities)
		def args = [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ]
		gparsLoadService.insertGpars(cities, args){ Map row, Map zargs ->
			insertRow(row)
		}
		assert CityBaseline.count() == 115000
	}

	void insertRow(Map row) {
		if (bindingMethod == 'grails') {
			CityBaseline city = new CityBaseline(row)
			city.save(failOnError:true, validate:validate)
		}
		else {
			CityBaseline city = bindWithCopyDomain(row)
			city.save(failOnError:true, validate:validate)
		}
	}

	@CompileDynamic
	void bindGrails(city, row){
		city.properties = row
	}

	CityBaseline bindWithCopyDomain(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		CityBaseline c = new CityBaseline()
		GormUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city_baseline")
	}

}
