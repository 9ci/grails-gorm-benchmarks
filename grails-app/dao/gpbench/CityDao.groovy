package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional

@Transactional
class CityDao extends GormDaoSupport<City> {
	Class domainClass = City

	@Override
	void beforeInsertSave(City city, Map params) {
		//city.id = params.id as Long
	}

	@GrailsCompileStatic()
	public City insertWithSetter(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		City c = new City()
		DomainUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		c.save(failOnError: true)
		return c
	}

}
