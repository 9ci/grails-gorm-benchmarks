package gpbench

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders


class CityAuditStampManual {
	//transient springSecurityService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	static belongsTo = [region:Region, country:Country]

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static mapping = {
		cache true
	}

	static constraints = {
		importFrom(City)
	}

	String toString() { name }

//	def beforeValidate(){
//		lastUpdatedUser = lastUpdatedUser?: springSecurityService.principal.id
//		dateCreatedUser = dateCreatedUser?: springSecurityService.principal.id
//	}

	def beforeInsert() {
		dateCreatedUser = SecUtil.userId
	}

	def beforeUpdate() {
		lastUpdatedUser = SecUtil.userId
	}

}
