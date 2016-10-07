package gpbench

import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.springframework.jdbc.core.RowMapper

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.Statement

/**
 * Executes the given query with scrollable/streaming forward only result
 */
@CompileStatic
class ScrollableQuery {

	private String query
	private DataSource dataSource
	private RowMapper rowMapper

	public ScrollableQuery(String query, RowMapper mapper, DataSource dataSource) {
		this.query = query
		this.dataSource = dataSource
		this.rowMapper = mapper
	}


	//Executes this query and calls closure with a map for each record.
	public void eachRow(Closure cl) {
		Sql sql = prepareSql()
		int index = 0

		sql.query(query) { ResultSet r ->
			while(r.next()) {
				index++
				def row = rowMapper.mapRow(r, index)
				cl.call(row)
			}
		}
	}

	//calls the given closure with a list of batchSize
	public void eachBatch(int batchSize, Closure cl) {
		List batch = []
		this.eachRow { def row ->
			batch.add(row)
			if((batch.size() == batchSize)) {
				cl.call(batch)
				batch = []
			}
		}

		//there could be remaning rows
		if(batch.size() > 0) cl.call(batch)
	}

	protected Sql prepareSql() {
		Sql sql = new Sql(dataSource)
		sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY
		sql.withStatement { Statement stmt -> stmt.fetchSize = Integer.MIN_VALUE }
		return sql
	}

}
