
import gpbench.City
import gpbench.CityDao
import grails.compiler.GrailsCompileStatic

//import groovy.transform.CompileStatic

@GrailsCompileStatic
class Loader {
    String bindingMethod
    CityDao dao

    String insertRow(Map row) {
        if (bindingMethod == 'grails'){
            City city = new City()
            city.properties = row
            city.persist()
        }
        else {
            dao.insert(row, [validate:true, bindingMethod:bindingMethod])
        }

    }
}

new Loader(bindingMethod: bindingMethod, dao: dao)

