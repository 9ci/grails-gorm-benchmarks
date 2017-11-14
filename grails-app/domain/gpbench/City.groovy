package gpbench

import gorm.AuditStamp
import groovy.transform.CompileStatic


//@AuditStamp
//@CompileStatic
class City extends BaseCity {

	static mapping = {
		table 'city'
		cache true
	}

}
