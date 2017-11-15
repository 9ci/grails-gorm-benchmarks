package gpbench

import groovy.transform.CompileStatic

//@AuditStamp
//@CompileStatic
@CompileStatic
trait CityModel {
	//transient loaderService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

}
