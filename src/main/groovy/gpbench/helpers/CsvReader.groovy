package gpbench.helpers

import grails.core.GrailsApplication
import grails.plugins.csv.CSVMapReader
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

@Component
@CompileStatic
class CsvReader extends RecordsLoader {

	@Autowired
	GrailsApplication grailsApplication

	@CompileStatic(TypeCheckingMode.SKIP)
	List<Map> load(String file) {

		List<Map> results = []
		Resource resource = grailsApplication.mainContext.getResource("classpath:${file}.csv")
		assert resource.exists(), "File $file does not exist"

		CSVMapReader reader = new CSVMapReader(new InputStreamReader(resource.inputStream))
		reader.each { Map m ->
			//need to convert to grails parameter map, so that it can be binded
			m = toGrailsParamsMap(m)
			results.add(m)
		}

		return results

	}

	@CompileDynamic
	private GrailsParameterMap toGrailsParamsMap(Map<String, String> map) {
		HttpServletRequest request = new MockHttpServletRequest()
		GrailsParameterMap gmap = new GrailsParameterMap(request)
		gmap.updateNestedKeys(map)
		return gmap
	}

}
