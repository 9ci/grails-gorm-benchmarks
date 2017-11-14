package gpbench.benchmarks

import gorm.tools.GormUtils
import gpbench.CityBaseline
import gpbench.Country
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
class GparsBaselineBenchmark extends GparsDaoBenchmark {

	boolean validate = true
	String bindingMethod //= 'grails'

	GparsBaselineBenchmark(boolean databinding = true, boolean validate = true, String bindingMethod = 'grails') {
		super(databinding)
		this.validate = validate
		this.bindingMethod = bindingMethod
	}

	@Override
	def execute() {
		assert CityBaseline.count() == 0
		insertGpars(cities)
		//cityDao.insertGpars(cities, [validate:validate, bindingMethod:bindingMethod ])
		assert CityBaseline.count() == 115000
	}

	@CompileDynamic
	void insertGpars(List<List<Map>> batchList) {
		GParsPool.withPool(poolSize) {
			batchList.eachParallel { List<Map> batch ->
				insertBatch(batch)
			}
		}
	}

	@Transactional
	void insertBatch(List<Map> batch) {
		for (Map record : batch) {
			insertRow(record)
		}
		DaoUtil.flushAndClear()
	}

	//@Transactional
	void insertRow(Map row) {
		if (useDatabinding) {
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

	@Override
	String getDescription() {
		return "GparsBaselineBenchmark: databinding=${useDatabinding}, validation:${validate}"
	}
}
