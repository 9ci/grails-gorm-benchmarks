
import gpbench.City

class Loader {
    String insertRow(Map row) {
        City city = new City()
        city.properties = row
        city.save(failOnError:true)
    }
}

new Loader()

