import gpbench.City

//import groovy.transform.CompileStatic

//@GrailsCompileStatic
class Loader {
    String dataBinder

    String insertRow(Map row) {
        City.dao.create(row, [dataBinder:dataBinder])
    }
}

new Loader(dataBinder: dataBinder)

