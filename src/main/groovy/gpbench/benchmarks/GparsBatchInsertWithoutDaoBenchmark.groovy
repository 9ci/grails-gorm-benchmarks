package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
import grails.plugin.dao.DaoUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * Runs batch inserts in parallel without using dao.
 */

@CompileStatic
class GparsBatchInsertWithoutDaoBenchmark extends GparsBatchInsertBenchmark {

	boolean validate

	GparsBatchInsertWithoutDaoBenchmark(boolean databinding, boolean validate) {
		super(databinding)
		this.validate = validate
	}

	@Transactional
	@CompileStatic(TypeCheckingMode.SKIP)
	void insertBatch(List<Map> batch, CityDao dao) {
		for (Map record : batch) {
			try {
				if (useDatabinding) {
					City city = new City()
					city.properties = record
					city.save(validate)
				}
				else {
					City city = dao.bind(record)
					city.save(validate)
				}
			}catch (Exception e) {
				e.printStackTrace()
			}
		}

		DaoUtil.flushAndClear()
	}

	@Override
	String getDescription() {
		return "GparsBatchInsertWithoutDao:databinding=${useDatabinding}, validation:${validate}"
	}
}
