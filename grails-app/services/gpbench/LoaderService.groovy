package gpbench

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.dao.DaoUtil
import grails.plugins.csv.CSVMapReader
import grails.transaction.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.PoisonPill
import org.grails.datastore.gorm.GormEntity
import org.grails.web.json.JSONObject
import org.hibernate.SessionFactory
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.http.HttpServletRequest
import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSet

import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.dataflow.Dataflow.operator
import gorm.tools.jdbc.ScrollableQuery
import gorm.tools.jdbc.GrailsParameterMapRowMapper

class LoaderService {
	private static int POOL_SIZE = 9
	static transactional = false

	SessionFactory sessionFactory

	def persistenceInterceptor

	DataSource dataSource
	JdbcTemplate jdbcTemplate

	RegionDao regionDao
	CityDao cityDao
	CountryDao countryDao
	GrailsApplication grailsApplication

	boolean mute = false

	def batchSize = 50 //this should match the hibernate.jdbc.batch_size in datasources

	void runBenchMark() {

		//warmup()
		//load_rows_scrollable_resultset(true) //insert million records with databinding

		//if you want to run the above benchmarks, comment the below all
		// otherwise because of some issues, there;s deadlock and it fails.

		println "--- Environment info ---"
		println "Max memory: " + (Runtime.getRuntime().maxMemory() / 1024 )+ " KB"
		println "Total Memory: " + (Runtime.getRuntime().totalMemory() / 1024 )+ " KB"
		println "Free memory: " + freeMemory
		println "Available processors: " + Runtime.getRuntime().availableProcessors()
		println "Autowire enabled: " + System.getProperty("autowire.enabled", "true")

		mute = true
		println "--- Warming up JVM --- "
		//warmup by running some benchmarks
		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		mute = false
		println "Running Benchmarks"
		println "#### With pool size of $POOL_SIZE"


		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		runImport('batched_transactions', true, true, true) //batched - databinding, typeless map
		runImport('batched_transactions', false, false, true) //batched - without databinding, typed map

		runImport('GPars_single_rec_per_thread_transaction', true, true) //databinding, typeless map
		runImport('GPars_single_rec_per_thread_transaction', false, false) //without databinding, typed map

		runImport('single_transaction', true, true) //databinding, typeless map
		runImport('single_transaction', false, false) //without databinding, typed map

		runImport('commit_each_save', true, true) //databinding, typeless map
		runImport('commit_each_save', false, false) //without databinding, typed map

		/*
		println "#### With pool size of $POOL_SIZE"
		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"


		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"

		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"


		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"

		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"

		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation

		POOL_SIZE++
		println "#### With pool size of $POOL_SIZE"

		runImport('GPars_batched_transactions_per_thread', true, true, true) //batched - databinding, typeless map
		runImport('GPars_batched_transactions_per_thread', false, false, true) //batched - without databinding, typed map

		runImport('GPars_batched_transactions_without_dao', false, true, true) //without dao
		runImport('GPars_batched_transactions_without_validation', false, true, true) //without validation
		runImport('GPars_batched_transactions_without_binding_validation', false, false, true) //without validation
		*/

	}

	String getFreeMemory() {
		(Runtime.getRuntime().freeMemory() / 1024 ) + " KB"
	}


	void runImport(String method, boolean csv, boolean databinding, boolean batched = false) {
		String extension = csv ? 'csv' : 'json'

		List countries = loadRecordsFromFile("Country.${extension}")
		List regions = loadRecordsFromFile("Region.${extension}")
		List cities = loadRecordsFromFile("City100k.${extension}")
		if(batched) {
			countries = batchChunks(countries, batchSize)
			regions = batchChunks(regions, batchSize)
			cities = batchChunks(cities, batchSize)
		}

		String desc = method + ':' + (databinding ? "with-databinding" : 'without-databinding')

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

		withPool(POOL_SIZE){
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

		withPool(POOL_SIZE){
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

	void GPars_batched_transactions_without_validation(String name, List<List<Map>> rows, boolean useDataBinding) {
		withPool(POOL_SIZE){
			rows.eachParallel { List batchList ->
				City.withTransaction {
					batchList.each{ Map row ->
						insertRecordWithoutValidation(name, row)
					}
					cleanUpGorm()
				}
			}
		}

	}

	void GPars_batched_transactions_without_dao(String name, List<List<Map>> rows, boolean useDataBinding) {
		withPool(POOL_SIZE){
			rows.eachParallel { List batchList ->
				City.withTransaction {
					batchList.each{ Map row ->
						insertRecordWithoutDao(name, row)
					}
					cleanUpGorm()
				}
			}
		}

	}

	void GPars_batched_transactions_without_binding_validation(String name, List<List<Map>> rows, boolean useDataBinding) {
		def service = getService(name)

		withPool(POOL_SIZE){
			rows.eachParallel { List batchList ->
				City.withTransaction {
					batchList.each{ Map row ->
						insertRecordWithoutValidationAndDataBinding(service, row)
					}
					cleanUpGorm()
				}
			}
		}

	}

	List<Map> loadRecordsFromFile(String fileName) {
		List<Map> result = []
		Resource resource = grailsApplication.mainContext.getResource("classpath:$fileName")
		assert resource.exists()

		if(fileName.endsWith("csv")) {
			def reader = new CSVMapReader(new InputStreamReader(resource.inputStream))
			reader.each { Map m ->
				//need to convert to grails parameter map, so that it can be binded. because csv is all string:string
				m = toGrailsParamsMap(m)
				result.add(m)
			}
		} else {
			String line
			resource.inputStream.withReader { Reader reader ->
				while (line = reader.readLine()) {
					JSONObject json = JSON.parse(line)
					result.add json
				}
			}
		}

		return result
	}


	@CompileStatic
	public void load_rows_scrollable_resultset(final boolean useDataBinding) {
		println "load_rows_scrollable_resultset"

		prepare1MRowsForInsert()

		String message = "Insert 1 million city using scrollable resultset: databinding = $useDataBinding"
		Long start = logBenchStart(message)

		RowMapper<Map> mapper = new GrailsParameterMapRowMapper()
		String q = "select * from city1M"
		ScrollableQuery query = new ScrollableQuery(mapper, dataSource, 50)

		DataflowQueue queue = new DataflowQueue()

		//start the dataflow operator with 4 threads. This is our consumer.
		def op1 = operator(inputs: [queue], outputs: [], maxForks:POOL_SIZE) {List<Map> batch ->
			City.withTransaction {
				batch.each { Map row ->
					insertRecord(cityDao, row, useDataBinding)
				}
				cleanUpGorm()
			}
		}


		final int MAX_QUEUE_SIZE = 10
		query.eachBatch(q, 50) { List<Map> batch ->
			while (queue.length() > MAX_QUEUE_SIZE) {
				//queue has 10 batches stocked, yield, let consumer consume few before we put more.
				Thread.yield()
			}
			queue << batch
		}

		//give operator a poision pill, so it will stop after finishing whatever batches are still in queue (cold shutdown).
		queue << PoisonPill.instance

		op1.join() //wait for operator to finish

		println "City count is ${City.count()}"
		logBenchEnd(message, start)

		println "Truncating tables"
		truncateTables()
	}


	public void prepare1MRowsForInsert() {
		//first insert the million rows into test table if not already inserted.
		insertCity1MRows()

		//we need to insert countries and regions so that we can load cities later.
		List countries = loadRecordsFromFile("Country.json")
		List regions = loadRecordsFromFile("Region.json")

		GPars_batched_transactions_per_thread("country", batchChunks(countries, batchSize), false)
		GPars_batched_transactions_per_thread("region", batchChunks(regions, batchSize), false)
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
		Resource resource = grailsApplication.mainContext.getResource("classpath:test-tables.sql")
		assert resource.exists()
		Connection connection = sessionFactory.currentSession.connection()
		ScriptUtils.executeSqlScript(connection, resource)

		//check if records are already inserted
		int count = jdbcTemplate.queryForObject("select count(*) FROM city1M", Long)
		if(count > 0) return

		println "Preparing test table with million rows."

		List<Map> cityRecords = loadRecordsFromFile("City100k.csv")
		String query = "insert into city1M (name, latitude, longitude, shortCode, `country.id`, `region.id`) values (?, ?, ?, ?, ?, ?)"
		Sql sql = new Sql(dataSource)

		cityRecords.each { Map m ->
				List params = [m.name, m.latitude as Float, m.longitude as Float, m.shortCode, m['country.id'] as Long, m['region.id'] as Long]
				sql.execute query, params
		}
		//logBenchEnd(message, start)
	}

	def cleanUpGorm() {
		DaoUtil.flushAndClear()
	}

	def truncateTables() {
		//println "Truncating tables"
		jdbcTemplate.update("DELETE FROM origin")
		jdbcTemplate.update("DELETE FROM city")
		jdbcTemplate.update("DELETE FROM region")
		jdbcTemplate.update("DELETE FROM country")
		//jdbcTemplate.update("RESET QUERY CACHE") //reset mysql query cache to try and be fair
		//println "Truncating tables complete"

	}

	//creates an array list of lists with max size of batchSize. 
	//If I pass in a batch size of 3 it will convert [1,2,3,4,5,6,7,8] into [[1,2,3],[4,5,6],[7,8]]
	//see http://stackoverflow.com/questions/2924395/groovy-built-in-to-split-an-array-into-equal-sized-subarrays
	//and http://stackoverflow.com/questions/3147537/split-collection-into-sub-collections-in-groovy
	List batchChunks(List theList, batchSize) {
		if (!theList) return [] //return and empty list if its already empty
		def batchedList = theList.collate(batchSize)
		return batchedList    
	}
	
	Long logBenchStart(desc) {
		def msg = "***** Starting $desc"
		//log.info(msg)
		//println msg
		return new Long(System.currentTimeMillis())
	}
	
	void logBenchEnd(String desc, Long startTime){
		def elapsed = (System.currentTimeMillis() - startTime)/1000
		def msg = "***** Finshed $desc in $elapsed seconds, free Memory: " + freeMemory
		if(!mute) println msg
	}

	def getService(String name) {
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		return this."${name}Dao"
	}

	void insertRecord(def service, Map row, boolean useDataBinding) {
		try {
			if (useDataBinding) {
				service.insert(row) //regular insert
			} else {
				service.insertWithSetter(row)
			}
		}catch (Exception e) {
			println row
			e.printStackTrace()
		}
	}

	void insertRecordWithoutDao(String domain, Map row) {
		GormEntity entity = Class.forName("gpbench.$domain").newInstance()
		entity.properties = row
		entity.id = row['id'] as Long
		entity.save()
	}


	void insertRecordWithoutValidation(String domain, Map row) {
		GormEntity entity = Class.forName("gpbench.$domain").newInstance()
		entity.properties = row
		entity.id = row['id'] as Long
		entity.save(false)
	}

	void insertRecordWithoutValidationAndDataBinding(def service, Map row) {
		service.insertWithoutValidation(row)
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
