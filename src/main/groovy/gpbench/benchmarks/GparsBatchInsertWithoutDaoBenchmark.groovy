package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * Runs batch inserts in parallel without using dao.
 */

//@CompileStatic
class GparsBatchInsertWithoutDaoBenchmark extends GparsBatchInsertBenchmark {

	GparsBatchInsertWithoutDaoBenchmark(boolean databinding = true, boolean validate = true) {
		super(databinding,validate)
	}


	@Transactional
	void insertRow(Map row, CityDao dao) {
		if (useDatabinding) {
			City city = new City()
			city.properties = row
			city.save(failOnError:true, validate:validate)
		}
		else {
			City city = dao.bindWithSetters(row)
			city.save(failOnError:true, validate:validate)
		}
	}

	@Override
	String getDescription() {
		return "GparsBatchInsertWithoutDao:databinding=${useDatabinding}, validation:${validate}"
	}
}
