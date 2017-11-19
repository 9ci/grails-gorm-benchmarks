package bugwork

import groovy.transform.CompileStatic

@CompileStatic
trait CityTrait {
    String name
    String shortCode
    String state
    String countryName

    BigDecimal latitude
    BigDecimal longitude

}
