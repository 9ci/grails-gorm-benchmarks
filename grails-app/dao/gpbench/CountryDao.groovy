package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional

@Transactional
class CountryDao extends GormDaoSupport<Country> {
	Class domainClass = Country

	void beforeInsertSave(Country country, Map params) {
		country.id = params.id as Long
	}


	@GrailsCompileStatic
	public Country insertWithSetter(Map row) {
		Country c = new Country()
		c.id = row.id as Long
		DomainUtils.copyDomain(c, row)
		c.save(failOnError: true)
		return c
	}


}
