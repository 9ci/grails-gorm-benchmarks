package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.transaction.Transactional

/**
 * Baseline benchmark with grails out of the box
 */

//@CompileStatic
class GparsBaselineBenchmark extends GparsDaoBenchmark {

	GparsBaselineBenchmark(boolean databinding = true, boolean validate = true) {
		super(databinding,validate)
	}

	@Override
	def execute() {
		assert City.count() == 0
		insertGpars(cities, cityDao)
		assert City.count() == 115000
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
