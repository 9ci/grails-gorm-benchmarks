package gpbench

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class CityAuditStampManual {
	//transient springSecurityService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

    String state
    String countryName

	static belongsTo = [region:Region, country:Country]

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

	static mapping = {
		cache true
	}

    static constraints = {
        importFrom(CityBaseline)
    }

	String toString() { name }

    def beforeValidate(){
        dateCreatedUser = dateCreatedUser?:SecUtil.userId
        lastUpdatedUser = lastUpdatedUser?:SecUtil.userId
            dateCreated = dateCreated?: new Date()
        lastUpdated = lastUpdated?: new Date()
    }

	def beforeInsert() {
		dateCreatedUser = SecUtil.userId
	}

	def beforeUpdate() {
		lastUpdatedUser = SecUtil.userId
	}

}
