
GORM : batch importing large datasets and a performance benchmarking app
===================================

Summary
--------
The application runs large batch inserts (115K records) in different ways. The goal is to decide the optimum way to run large batch inserts with Gorm.

questions we want a good benchmark to constantly measure and answer

- fastest way to persist/insert gorm
- show advantages of using multi-core (shown)
- does binding slow it down (yes), why, can it be optimized, best alternative ….
- does valiation slow it down (yes), why …. can it be optimized
- does auditstamp slow it down (yes), can it be optimized
- do daos slow it down ……
- does differerent id generateord such as BatchIdGenerator slow it down or speed it up
- does @compileStatic speed things up, where does it matter? does it help to have it on the domains?

How to run the benchmarks
-------
- There is a script ```run-benchmarks.sh``` which will run the benchmarks
- Run ```./run-benchmarks.sh```
- See the script for more details.


Changing default pool size
----
By default benchmarks uses default gpars pool size which is (availableProcessors + 1) which can be modified by passing system property ```gpars.poolsize=xx```
Example: ```java -Dgpars.poolsize=5 -jar grails-gpars-batch-load-benchmark-0.1.war```

The Bemchmarks
-------
- GparsBatchInsertBenchmark - Runs the batches in parallel, each batch with the same size as jdbc batch size (50).
- GparsBatchInsertWithoutDaoBenchmark - Same as above, but without using dao for inserting records.
- GparsBatchInsertWithoutValidation - Runs the batches with gpars but with grails domain validation tunred off during save using ```domain.save(false)```
- GparsThreadPerTransactionBenchmark - Runs the inserts in parallel with one thread per transaction.
- BatchInsertWithDataFlawQueueBenchmark - Runs the batch inserts in parallel using Gprase dataflow operator [see](http://www.gpars.org/webapp/quickstart/index.html#__strong_dataflow_concurrency_strong)
- SimpleBatchInsertBenchmark - Batch inserts but without gpars parallelism.
- CommitEachSaveBenchmark - Insert each record in individual transactions.
- OneBigTransactionBenchmark - Inserts all records within a single big transaction.
- DataFlawQueueWithScrollableQueryBenchmark : same as BatchInsertWithDataFlawQueueBenchmark but the data is being loaded from another table simultanously using scrollable resultset.

Note: All of above benchmarks are run with and without data binding, and you will see the results for both.


My Bench Mark Results and details
-------

* 115k CSV records on a MacBook pro 2.5 GHz Intel Core i7. 2048 mb ram was given to the VM and these were run using ```java -jar grails-gpars-batch-load-benchmark-0.1.war```
* All of these have jdbc.batch_size = 50 and use the principals from #2 above and flush/clear every 50 rows
* The winner seems to be gpars and batched (smaller chunks) transactions


**Results with Gparse pool size 8**

|                      | All records in single transaction | Commit each record | Batched Transaction - Without Gpars  | Batched Transactions - With Gpars  | Gpars single transaction per thread  |
|----------------------|-----------------------------------|--------------------|--------------------------------------|------------------------------------|--------------------------------------|
| With data binding    | 40.807                           | **81.521**          | 43.569                              |  12.32                              | 22.372                               |



**Results for Gparse batched with different pool sizes**

| Pool size                             |  2 threads | 3 threads | 4 threads | 5 threads | 6 threads | 7 threads | 8 threads | 9 threads | 10 threads | 11 threads | 12 threads |
|---------------------------------------|------------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|------------|------------|------------|
| With data binding    | 24.522     | 22.473    | 16.063    | 17.363    | 16.698    | 14.53     | 12.32     | 12.012    | 12.145     | 14.785     | 14.081     |
| Without data binding | 12.302     | 12.593    | 9.52      | 8.586     | 8.509     | 7.46      | 6.842     | 6.27      | 6.696      | 6.896      | 7.074      |
| No validation                    | 15.335     | 17.588    | 9.906     | 10.3      | 10.724    | 9.489     | 7.993     | 8.112     | 8.203      | 9.069      | 9.032      |
| No validation & No data binding    | 10.619     | 9.311     | 7.088     | 7.59      | 7.997     | 8.088     | 6.558     | 5.896     | 5.683      | 6.223      | 6.594      |


| Gpars Benchs      | time |
|-------------------|------|
|with databinding   | 12.32  |
|no binding         | 6.842 |
|No autowire        | 12.969 |
|no validation      | 7.993 |
|no binding, no autowire,  no validation | 6.221 |
|grails date stamp fields | 14.726 |
|audit-trail stamp fields (user and dates)| 21.728 |
|no dao            | 10.603 |
|DataflowQueue (CED Way) | 14.6 |

CPU Load during Gparse batch insert
--------
It can be seen that cpu load goes highest during Gparse batch insert
  
![Image of Yaktocat](./cupload.png)


**Note:** 
- All Numbers are in Seconds.
- Domain autowiring, validation and databinding are enabled, unless explicitly specified.
- One service is injected in each of the domains.
- H2 In memory database is used.

System specs
------------
- Macbook Pro Intel(R) Core(TM) i7-4870HQ CPU @ 2.50GHz
- Gparse pool size of 9
- GRAILS_OPTS="-Xmx2048M -server -XX:+UseParallelGC"


Conclusions
-------

The key conclusions Are as below

1. Gparse with batch insert is the optimum way to do large batch inserts.
2. Gparse batch insert along with data binding and validation disabled has best performance. 
3. Inserting each record in seperate transaction has worst performance
4. Grails databinding almost doubles the time required for inserts.
5. use small transaction batches and keep them the same size as the jdbc.batch_size. DO NOT (auto)commit on every insert
6. JDBC Batch size of 50 Gave the best results, as batch size goes higher, performance started to degrade.
7. Disabling validation improves performance eg. ```domain.save(false)```
8. Grails Date stamp fields does not have any noticeable effect on performance.
9. AuditTrail stamp affects performance (see below for details)
10. Did not see any noticeable difference if Domain autowiring is enabled or disabled. (Domain with dependency on one service).
11. Dao does not have any major noticable effect on performance.
12. Disabling validation has slight performance benifits but not significant (see below for details)
13. Using custom (idgenerator)[https://yakworks.github.io/gorm-tools/id-generation/] does not have any noticable effect  
14. From above table, it can be seen that 
   Going from 2 cores to 4 improves numbers significantly
   Going from 4 cores to 8 numbers improves slowly
   from pool size 9 onward, performance starts degrading   


Optimum setting for Gpars pool size.
----
It is observed that 9 core gave the best results for (i7-4870HQ)[https://ark.intel.com/products/83504] which has four physical cores.
But the system shows the OS and Java total of 8 cores and uses Hyper threading.

Gparse will utilize and benefit if it is given 8 cores even if there are just four physical cores and four virtual cores. 

The default Gpars pool size is Runtime.getRuntime().availableProcessors() + 1 see [here](https://github.com/vaclav/GPars/blob/master/src/main/groovy/groovyx/gpars/util/PoolUtils.java#L43)
And this indeed gives better performance. 

As per Gpars performance tips [here](http://www.gpars.org/1.0.0/guide/guide/tips.html)
> In many scenarios changing the pool size from the default value may give you performance benefits. Especially if your tasks perform IO operations, like file or database access, networking and such, increasing the number of threads in the pool is likely to help performance.

Effect of databinding on performance
---
As it can be seen from above results. Databinding has huge overhead on performance, especially when doing huge batch inserts.
The overhead is caused by iterating over each property of the domain for every instance that needs to be bind, calling type conversion system
and other stuff done by GrailsWebDataBinder.

Effect of Validation
----
Disabling validation has slight performance benefit. That is because Grails validation (GrailsDomainClassValidator) has to iterate over each constrained property of domain class 
and invoke validators on it for every instance. 

Effect of AuditTrail 
----
Audit trail has noticeable effect on performance. Thats because audit trail plugin hooks into validation and gets called every time when the domain is validated. 
It does some reflection to check for properties/value and checks in hibernate persistence context if the instance is being inserted or being updated. All this makes it little slower.
 
 
More background and reading
---------------

Here are a 2 links you should read that will give you some background information on processing large bulk data batches.
read up through 13.2
<http://docs.jboss.org/hibernate/core/3.3/reference/en/html/batch.html>
and read this entire post
<http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/>

Thomas Lin setup a test for some processing for GPars
<http://fbflex.wordpress.com/2010/06/11/writing-batch-import-scripts-with-grails-gsql-and-gpars/>

and the gpars docs
<http://gpars.org/guide/index.html>

[GPars]: http://gpars.org/guide/index.html
[SimpleJdbc Example]: http://www.brucephillips.name/blog/index.cfm/2010/10/28/Example-Of-Using-Spring-JDBC-Execute-Batch-To-Insert-Multiple-Rows-Into-A-Database-Table
[Zach]:http://grails.1312388.n4.nabble.com/Grails-Hang-with-Bulk-Data-Import-Using-GPars-td3410441.html
