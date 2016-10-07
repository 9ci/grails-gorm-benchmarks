package gpbench

import grails.converters.JSON
import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.http.HttpServletRequest
import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSet
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

import static grails.async.Promises.task
import static groovyx.gpars.GParsPool.withPool

class LoaderService {
	static transactional = false

	SessionFactory sessionFactory

	def persistenceInterceptor

	DataSource dataSource
	JdbcTemplate jdbcTemplate

	RegionService regionService
	CityService cityService
	CountryService countryService

	def batchSize = 50 //this should match the hibernate.jdbc.batch_size in datasources

	void runBenchMark() {
		println "Running benchmark"

		load_rows_scrollable_resultset(true) //insert million records with databinding

		//if you want to run the below benchmarks, comment the above one
		// otherwise because of some issues, there;s deadlock and it fails.

		/*
		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map


		println "############"
		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, true, true) //batched - databinding, typed map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_single_rec_per_thread_transaction', true, true) //databinding, typeless map
		runImport('GPars_single_rec_per_thread_transaction', false, true) //databinding, typed map
		runImport('GPars_single_rec_per_thread_transaction', false, false) //without databinding, typed map

		runImport('single_transaction', true, true) //databinding, typeless map
		runImport('single_transaction', false, true) //databinding, typed map
		runImport('single_transaction', false, false) //without databinding, typed map

		runImport('commit_each_save', true, true) //databinding, typeless map
		runImport('commit_each_save', false, true)  //databinding, typed map
		runImport('commit_each_save', false, false) //without databinding, typed map

		runImport('batched_transactions', true, true, true) //batched - databinding, typeless map
		runImport('batched_transactions', false, true, true) //batched - databinding, typed map
		runImport('batched_transactions', false, false, true) //batched - without databinding, typed map
		*/

	}

	void runImport(String method, boolean csv, boolean databinding, boolean batched = false) {
		String extension = csv ? 'csv' : 'json'

		List countries = loadRecordsFromFile("Country.${extension}")
		List regions = loadRecordsFromFile("Region.${extension}")
		List cities = loadRecordsFromFile("City.${extension}")
		if(batched) {
			countries = batchChunks(countries, batchSize)
			regions = batchChunks(regions, batchSize)
			cities = batchChunks(cities, batchSize)
		}

		String desc = method + ':' + (databinding ? "with-databinding" : 'without-databinding')
		desc = desc +  ':' + (csv ? 'typeless-map' : 'typed-map')

		try {
			Long startTime = logBenchStart(desc)

			"${method}"("Country", countries, databinding)
			"${method}"("Region", regions, databinding)
			"${method}"("City", cities, databinding)

			logBenchEnd(desc, startTime)
			println '---------------------'

		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			truncateTables()
		}

	}


	@Transactional
	void single_transaction(String name, List<Map> rows, boolean useDataBinding) {
		def service = getService(name)
		rows.eachWithIndex { Map row, index ->
			insertRecord(service, row, useDataBinding)
			if (index % batchSize == 0) cleanUpGorm()
		}
	}

	void commit_each_save(String name, List<Map> rows, boolean dataBinding) {
		def service = getService(name)
		rows.eachWithIndex { Map row, index ->
			insertRecord(service, row, dataBinding)
			if (index % batchSize == 0) cleanUpGorm()
		}
	}


	void batched_transactions(String name, List<List<Map>> rows, boolean useDataBinding) {
		def service = getService(name)

		rows.each { List rowSet ->
			City.withTransaction {
				rowSet.each{ Map row ->
					insertRecord(service, row, useDataBinding)
				}
			}
			cleanUpGorm()
		}
	}

	void GPars_single_rec_per_thread_transaction(String name, List<Map> rows, boolean useDataBinding) {
		def service = getService(name)

		withPool(4){
			rows.eachWithIndexParallel { Map row, int index ->
				withPersistence {
					insertRecord(service, row, useDataBinding)
					if (index % batchSize == 0) cleanUpGorm()
				}

			}
		}
	}

	void GPars_batched_transactions_per_thread(String name, List<List<Map>> rows, boolean useDataBinding) {
		//println "Gparse batched called"
		def service = getService(name)

		withPool(4){
			rows.eachParallel { List batchList ->
				City.withTransaction {
					//println "Inserting batch $name : size - ${batchList.size()}"
					batchList.each{ Map row ->
						insertRecord(service, row, useDataBinding)
					}
					//println "Batch finish"
					cleanUpGorm()
				}

			}
		}

	}

	List<Map> loadRecordsFromFile(String fileName) {
		List<Map> result = []
		File file = new File("resources/${fileName}")
		if(fileName.endsWith("csv")) {
			def reader = file.toCsvMapReader()
			reader.each { Map m ->
				//need to convert to grails parameter map, so that it can be binded. because csv is all string:string
				m = toGrailsParamsMap(m)
				result.add(m)
			}
		} else {
			String line
			file.withReader { Reader reader ->
				while (line = reader.readLine()) {
					JSONObject json = JSON.parse(line)
					result.add json
				}
			}
		}

		return result
	}


	@CompileStatic
	public void load_rows_scrollable_resultset(boolean useDataBinding) {
		println "load_rows_scrollable_resultset"
		//first insert the million rows into test table if not already inserted.
		insertCity1MRows()

		//we need to insert countries and regions so that we can load cities later.
		List countries = loadRecordsFromFile("Country.json")
		List regions = loadRecordsFromFile("Region.json")

		println "prepare country/city"
		GPars_batched_transactions_per_thread("country", batchChunks(countries, batchSize), false)
		GPars_batched_transactions_per_thread("region", batchChunks(regions, batchSize), false)

		//can hold 10 elements (each element is a batch of 50 records)
		BlockingQueue queue = new ArrayBlockingQueue(3)

		List batch4 = []

		//start the consumer thread
		startGparsConsumer("city", queue, useDataBinding)

		String message = "Insert 1 million city using scrollable resultset: databinding = $useDataBinding"
		Long start = logBenchStart(message)
		RowMapper<Map> mapper = new GrailsParameterMapRowMapper()
		String q = "select * from city1M"
		ScrollableQuery query = new ScrollableQuery(q, mapper, dataSource)

		int index = 0
		query.eachBatch(50) { List<Map> batch ->
			batch4.add(batch)
			if (batch4.size() == 4) {
				index++
				//println "put batch $index"
				queue.put(batch4)
				batch4 = []
			}
		}

		//there could be results left
		if (batch4.size() > 0) {
			queue.put(batch4)
		}

		//put false to stop the consumer
		queue.put(false)

		println "City count is ${City.count()}"
		logBenchEnd(message, start)

		println "Truncating tables"
		truncateTables()
	}

	@CompileStatic
	void startGparsConsumer(String name, BlockingQueue queue, boolean useDataBinding = true) {
		Consumer consumer = new Consumer(queue)
		int index = 0
		task {
			consumer.start { List batch ->
				//index++
				//println "Got batch $index of size ${batch.size()}"
				GPars_batched_transactions_per_thread(name, batch, useDataBinding)
			}
		}
	}

	public void load_rows_with_manual_paging() {
		Sql sql = new Sql(dataSource)

		int index = 0
		int limit = 10000
		int offset = 0
		String query = "select * from city1M limit ? offset ?"

		Long start = logBenchStart("Load 1 million rows using manual paging with limit offset")
		int count = jdbcTemplate.queryForLong("select count(*) FROM city1M")

		while(offset < (count)) {
			sql.query(query, [limit, offset]) { ResultSet r ->
				while(r.next()) {
					index ++
				}
			}

			offset = offset + limit
		}

		println "Loaded $index rows"
		logBenchEnd("Load 1 million rows using manual paging with limit offset", start)
	}

	List insertCity1MRows() {
		//first create the test table if does nto exist
		Resource resource = new FileSystemResource("resources/test-tables.sql");
		Connection connection = sessionFactory.currentSession.connection()
		ScriptUtils.executeSqlScript(connection, resource)

		//check if records are already inserted
		int count = jdbcTemplate.queryForLong("select count(*) FROM city1M")
		if(count > 0) return

		println "Preparing test table with million rows."

		File file = new File("resources/City100k.csv")
		String query = "insert into city1M (name, latitude, longitude, shortCode, `country.id`, `region.id`) values (?, ?, ?, ?, ?, ?)"
		Sql sql = new Sql(dataSource)

		//String message = "Insert million records with direct insert queries, no hibernate/grails"
		//Long start = logBenchStart(message)
		for(int i in (1..10)) {
			def reader = file.toCsvMapReader()
			reader.each { Map m ->
				List params = [m.name, m.latitude as Float, m.longitude as Float, m.shortCode, m['country.id'] as Long, m['region.id'] as Long]
				sql.execute query, params
			}
		}

		//logBenchEnd(message, start)
	}

	def cleanUpGorm() {
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
		def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
		propertyInstanceMap.get().clear()
	}

	def truncateTables() {
		//println "Truncating tables"
		jdbcTemplate.update("DELETE FROM origin")
		jdbcTemplate.update("DELETE FROM city")
		jdbcTemplate.update("DELETE FROM region")
		jdbcTemplate.update("DELETE FROM country")
		jdbcTemplate.update("RESET QUERY CACHE") //reset mysql query cache to try and be fair
		//println "Truncating tables complete"

	}

	//creates an array list of lists with max size of batchSize. 
	//If I pass in a batch size of 3 it will convert [1,2,3,4,5,6,7,8] into [[1,2,3],[4,5,6],[7,8]]
	//see http://stackoverflow.com/questions/2924395/groovy-built-in-to-split-an-array-into-equal-sized-subarrays
	//and http://stackoverflow.com/questions/3147537/split-collection-into-sub-collections-in-groovy
	List batchChunks(theList, batchSize) {
		if (!theList) return [] //return and empty list if its already empty
		
		def batchedList = []
		int chunkCount = theList.size() / batchSize

		chunkCount.times { chunkNum ->
			def start = chunkNum * batchSize 
			def end = start + batchSize - 1
			batchedList << theList[start..end]    
		}

		if (theList.size() % batchSize){
			batchedList << theList[chunkCount * batchSize..-1]
		}
		return batchedList    
	}
	
	Long logBenchStart(desc) {
		def msg = "***** Starting $desc"
		log.info(msg)
		println msg
		return new Long(System.currentTimeMillis())
	}
	
	void logBenchEnd(String desc, Long startTime){
		def elapsed = (System.currentTimeMillis() - startTime)/1000
		def msg = "***** Finshed $desc in $elapsed seconds"
		println msg
	}

	def getService(String name) {
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		return this."${name}Service"
	}

	void insertRecord(def service, Map row, boolean useDataBinding) {
		try {
			if (useDataBinding) {
				service.insertWithDataBinding(row)
			} else {
				service.insertWithSetter(row)
			}
		}catch (Exception e) {
			println row
			e.printStackTrace()
		}
	}

	GrailsParameterMap toGrailsParamsMap(Map<String, String> map) {
		try {
			HttpServletRequest request = new MockHttpServletRequest()
			GrailsParameterMap gmap = new GrailsParameterMap(request)
			gmap.updateNestedKeys(map)
			return gmap
		}catch (Exception e) {
			e.printStackTrace()
		}
	}

	void withPersistence(closure){
		persistenceInterceptor.init()
		try {
			closure()
		}
		finally {
			persistenceInterceptor.flush()
			persistenceInterceptor.destroy()
		}
	}

}
