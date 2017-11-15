package gpbench


class CityAuditStampManual {

	transient auditTrailHelper
	transient disableAuditTrailStamp = true

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	static belongsTo = [region:Region, country:Country]

	Long createdBy
	Date createdDate

	Long editedBy
	Date editedDate

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

	def beforeValidate(){
		initFields()
	}

	def beforeInsert() {
		initFields()
	}

	void initFields() {
		auditTrailHelper.setDateField(this, 'createdDate')
		auditTrailHelper.setDateField(this, 'editedDate')

		auditTrailHelper.setUserField(this, 'createdBy')
		auditTrailHelper.setDateField(this, 'editedBy')
	}

}
