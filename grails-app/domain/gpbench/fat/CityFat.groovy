package gpbench.fat

import gorm.tools.beans.DateUtil
import gpbench.Country
import gpbench.Region
import gpbench.model.CityTrait
import gpbench.model.CityTrait2
import gpbench.model.CityTrait2Constraints
import gpbench.model.CityTrait3
import gpbench.model.CityTrait3Constraints
import gpbench.model.CityTraitConstraints
import gpbench.model.CityTraitFat
import gpbench.model.CityTraitFatConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import gpbench.model.DatesTrait
import gpbench.model.DatesTraitConstraints
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.grails.datastore.gorm.GormEnhancer

/**
 *
 */
@GrailsCompileStatic
class CityFat implements CityTraitFat, DateUserStamp{

	static belongsTo = [region:Region, country:Country,
                        region2:Region, country2:Country,
                        region3:Region, country3:Country]


    //@CompileStatic(TypeCheckingMode.SKIP)
	static constraints = {
        importFrom(CityTraitFatConstraints)
        importFrom DateUserStampConstraints
	}

    void setPropsFast(Map row) {
        this.name = row['name']
        this.shortCode = row['shortCode']
        this.state = row['state']
        this.countryName = row['countryName']
        this.latitude = row['latitude'] as BigDecimal
        this.longitude = row['longitude'] as BigDecimal

        this.name2 = row['name2']
        this.shortCode2 = row['shortCode2']
        this.state2 = row['state2']
        this.countryName2 = row['countryName2']
        this.latitude2 = row['latitude2'] as BigDecimal
        this.longitude2 = row['longitude2'] as BigDecimal

        this.name3 = row['name3']
        this.shortCode3 = row['shortCode3']
        this.state3 = row['state3']
        this.countryName3 = row['countryName3']
        this.latitude3 = row['latitude3'] as BigDecimal
        this.longitude3 = row['longitude3'] as BigDecimal
        //this.properties = row
        date1 = DateUtil.parseJsonDate(row['date1'] as String)
        date2 = DateUtil.parseJsonDate(row['date2'] as String)
        date3 = DateUtil.parseJsonDate(row['date3'] as String)
        date4 = DateUtil.parseJsonDate(row['date4'] as String)

        setAssociation("region", Region, row)
        setAssociation("country", Country, row)
        setAssociation("region2", Region, row)
        setAssociation("country2", Country, row)
        setAssociation("region3", Region, row)
        setAssociation("country3", Country, row)
        //println latitude3
    }

    void setAssociation(String key, Class assocClass, Map row){
        if(row[key] && row[key]['id']){
            this[key] = GormEnhancer.findStaticApi(assocClass).load(row[key]['id'] as Long)
        }
    }

}
