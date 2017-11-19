package bugwork

import groovy.transform.CompileStatic

@CompileStatic
trait CityTraitFat implements CityTrait, CityTrait2, CityTrait3{
    String name
    String shortCode
    String state
    String countryName

    BigDecimal latitude
    BigDecimal longitude

}
