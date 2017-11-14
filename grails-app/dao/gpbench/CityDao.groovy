package gpbench

import grails.compiler.GrailsCompileStatic
import grails.transaction.Transactional

@Transactional
@GrailsCompileStatic
class CityDao extends CityDaoBase<City> {
	//Class domainClass = City

	CityDao() {
		super(City)
	}
}
