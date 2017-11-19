package gpbench

import grails.compiler.GrailsCompileStatic
import org.grails.datastore.gorm.GormEnhancer

/**
 * Baseline stock grails domain. no DAO or anything else should be attached to this.
 * only Grails AST should have touched this.
 */
@GrailsCompileStatic
class CityFatAssoc {
	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

    String state
    String countryName

	Date dateCreated
	Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
	Long dateCreatedUser
	Long lastUpdatedUser

    String name2
    String shortCode2
    String state2
    String countryName2

    BigDecimal latitude2
    BigDecimal longitude2

    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

	static belongsTo = [region:Region, country:Country, region2:Region, country2:Country, region3:Region, country3:Country]

	static mapping = {
		cache true
	}

	static constraints = {
        importFrom(CityFatAssocDynamic)
	}

	String toString() { name }

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
