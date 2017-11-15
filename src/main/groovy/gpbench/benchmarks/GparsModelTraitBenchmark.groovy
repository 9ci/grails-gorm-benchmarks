package gpbench.benchmarks

import gorm.tools.GormUtils
import gpbench.CityBaseline
import gpbench.CityModelTrait
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
 * Tests Domain that extends/implements a trait for it properties
 */
@CompileStatic
class GparsModelTraitBenchmark extends BaseBatchInsertBenchmark {

	GparsModelTraitBenchmark(String bindingMethod = 'grails', boolean validate = true) {
		super(bindingMethod,validate)
	}

	@Override
	def execute() {
		assert CityModelTrait.count() == 0
		def args = [poolSize:poolSize, validate:validate, bindingMethod:bindingMethod ]
		gparsLoadService.insertGpars(cities, args){ Map row, Map zargs ->
			insertRow(row)
		}
		assert CityModelTrait.count() == 115000
	}

	//@Transactional
	void insertRow(Map row) {
		if (bindingMethod == 'grails') {
			CityModelTrait city = new CityModelTrait(row)
			city.save(failOnError:true, validate:validate)
		}
		else {
			CityModelTrait city = bindWithCopyDomain(row)
			city.save(failOnError:true, validate:validate)
		}
	}

	CityModelTrait bindWithCopyDomain(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		CityModelTrait c = new CityModelTrait()
		GormUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

	//@Transactional
	void cleanup() {
		jdbcTemplate.execute("DELETE FROM city_model_trait")
	}

}
