package gpbench

import gorm.tools.GormUtils
import gorm.tools.dao.DefaultGormDao
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

@Transactional
@GrailsCompileStatic
class CountryDao extends DefaultGormDao<Country> {
	Class domainClass = Country

	void beforeCreate(Country country, Map params) {
		country.id = params.id as Long
	}

	@NotTransactional
	Country bind(Map row) {
		Country c = new Country()
		c.id = row.id as Long
		GormUtils.copyDomain(c, row)
		return c
	}

	@GrailsCompileStatic
	public Country insertWithSetter(Map row) {
		Country c = bind(row)
		c.save(failOnError:true)
		return c
	}

	@GrailsCompileStatic
	public Country insertWithoutValidation(Map row) {
		Country c = bind(row)
		c.save(false)
		return c
	}

}
