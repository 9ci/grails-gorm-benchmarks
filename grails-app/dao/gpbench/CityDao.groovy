package gpbench

import gorm.tools.GormUtils
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@Transactional
@GrailsCompileStatic
class CityDao extends GormDaoSupport<City> {
	Class domainClass = City

	@NotTransactional
	@CompileStatic(TypeCheckingMode.SKIP)
	City bind(Map row) {
		Region r = Region.load(row['region']['id'] as Long)
		Country country = Country.load(row['country']['id'] as Long)

		City c = new City()
		GormUtils.copyDomain(c, row)
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
