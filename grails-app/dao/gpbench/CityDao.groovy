package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional

@Transactional
class CityDao extends GormDaoSupport<City> {
	Class domainClass = City

	@Override
	void beforeInsertSave(City city, Map params) {
		//city.id = params.id as Long
	}

	@NotTransactional
	City bind(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		City c = new City()
		DomainUtils.copyDomain(c, row)
		c.region = r
		c.country = country
		return c
	}

	@GrailsCompileStatic()
	public City insertWithSetter(Map row) {
		City c = bind(row)
		c.save(failOnError:true)
		return c
	}

	public City insertWithoutValidation(Map row) {
		City c = bind(row)
		c.save(false)
		return c
	}

}
