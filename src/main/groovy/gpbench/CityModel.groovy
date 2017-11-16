package gpbench

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic

import javax.persistence.Transient

//@AuditStamp
//@CompileStatic
//@CompileStatic
trait CityModel {

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

}
