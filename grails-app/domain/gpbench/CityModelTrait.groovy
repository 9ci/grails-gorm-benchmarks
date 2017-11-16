package gpbench


class CityModelTrait implements CityModel{

	static belongsTo = [Region, Country]

	static mapping = {
		cache true
	}

	static constraints = {
		importFrom(CityBaseline)
	}

	String toString() { name }

}
