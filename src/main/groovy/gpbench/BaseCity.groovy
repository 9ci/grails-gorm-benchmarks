package gpbench

import gorm.AuditStamp
import groovy.transform.CompileStatic


//@AuditStamp
//@CompileStatic
@CompileStatic
trait BaseCity {
	//transient loaderService

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country

}
