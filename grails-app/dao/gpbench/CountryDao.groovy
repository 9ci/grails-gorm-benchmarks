package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.NotTransactional
import grails.transaction.Transactional

@Transactional
class CountryDao extends GormDaoSupport<Country> {
	Class domainClass = Country

	void beforeInsertSave(Country country, Map params) {
		country.id = params.id as Long
	}

	@NotTransactional
	Country bind(Map row) {
		Country c = new Country()
		c.id = row.id as Long
		DomainUtils.copyDomain(c, row)
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
