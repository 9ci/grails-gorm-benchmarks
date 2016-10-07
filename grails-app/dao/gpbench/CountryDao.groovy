package gpbench

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional
import org.springframework.jdbc.core.JdbcTemplate

class CountryDao extends GormDaoSupport {
	Class domainClass = Country

	def beforeInsertSave(country, params) {
		country.id = params.id as Long
	}


	@GrailsCompileStatic
	public void insertWithSetter(Map row) {
		Country c = new Country()
		c.id = row.id as Long
		DomainUtils.copyDomain(c, row)
		c.save(failOnError: true)
	}


}
