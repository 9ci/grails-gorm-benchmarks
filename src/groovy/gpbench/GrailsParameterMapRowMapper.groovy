package gpbench

import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.http.HttpServletRequest

@CompileStatic
class GrailsParameterMapRowMapper extends ColumnMapRowMapper {

	protected Map<String, Object> createColumnMap(int columnCount) {
		HttpServletRequest request = new MockHttpServletRequest()
		return new GrailsParameterMap(request)
	}

}
