package gpbench


class CityAuditStampFields {

	transient auditTrailHelper

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	static belongsTo = [region:Region, country:Country]

	Long createdBy
	Date createdDate
	Long editedBy
	Long editedDate

	static mapping = {
		cache true
	}

	static constraints = {
		name blank: false, nullable: false
		shortCode blank: false, nullable: false
		latitude nullable: false, scale: 4, max:90.00
		longitude nullable: false, scale: 4, max:380.00
		region nullable: false

		country nullable: false

		createdBy nullable:true,display:false,editable:false,bindable:false
		createdDate nullable:true,display:false,editable:false,bindable:false
		editedBy nullable:true,display:false,editable:false,bindable:false
		editedDate nullable:true,display:false,editable:false,bindable:false
	}

	String toString() { name }

}
