
GORM : batch importing large datasets and a performance benchmarking app
===================================

Summary
--------

We have a new client that will be importing large payment data files into out application and it appears that this topic ([Zach][]) has some common mind share right now. Over the years I have seem many recommendations for hibernate performance tuning with large sets of data and most recently for GORM as well. What I haven't seen are any sample straight forward benchmark apps to fiddle with.
So I jammed the beginnings of one together and here it is. 

How to run the benchmarks
-------
- build runnable war using command, ```gradle assemble```
- Go to build/lib directory, Run the application with command ```java -jar grails-gpars-batch-load-benchmark-0.1.war``` 
- Benchmarks are run from the BootStrap.groovy. You will see the results on console. 


The Bemchmarks
-------
1. GPars_batched_transactions_per_thread : Runs the batches in parallel, each batch being the same size as jdbc batch size (50).
2. GPars_single_rec_per_thread_transaction : Gpars Single thread par transaction
3. single_transaction : Insert all records in a single transaction and commit at the end. No Gpars.
4. GPars_batched_transactions_without_validation : Gparse batched, with data binding, without validation
5. GPars_batched_transactions_without_binding_validation: Gparse without data binding, without validation
4. commit_each_save: Insert each record in individual transactions and commit. No Gpars.
5. batched_transactions: Run batch insert as 1) but without Gparse.

Note: All of above benchmarks are run with and without data binding, and you will see the results for both.

**By default, all benchmarks are run with Gorm domain autowiring enabled.** 
If you want to see effect of autowiring domains, just set gorm autowire to false in application.yml


My Bench Mark Results and details
-------

* 115k CSV records on a MacBook pro 2.5 GHz Intel Core i7. 2048 mb ram was given to the VM and these were run using ```java -jar grails-gpars-batch-load-benchmark-0.1.war```
* all of these have jdbc.batch_size = 50 and use the principals from #2 above and flush/clear every 50 rows
* The test where the batch insert happen in a single transaction can't be tested with GPars since a single transaction can't span multiple threads
* the winner seems to be gpars and batched (smaller chunks) transactions


|                      | All records in single transaction | Commit each record | Batched Transaction - Without Gpars  | Batched Transactions - With Gpars  | Gpars single transaction per thread  |
|----------------------|-----------------------------------|--------------------|--------------------------------------|------------------------------------|--------------------------------------|
| With data binding    | 53.156                           | **111.44**          | 43.746                              | 22.257                             | 26.549                               |
| Without data binding | 24.662                            | 48.716             | 21.786                               | **8.224**                          | 15.863                               |
|                      |                                   |                    |                                      |                                    |                                      |


| gpars benchs      | time |
|-------------------|------|
|with databinding   | 22.257  |
|no binding         | 8.224 |
|With autowire        | 24.094 |
|no validation      | 10.341 |
|no binding, no autowire,  no validation | 6.472 |
|grails date stamp fields | 22.823 |
|audit-trail stamp fields (user and dates)| 16.001 |
|no dao            | 22.542 |
|DataflowQueue (CED Way) 1 Million records | 16.285 |



CPU Load during Gparse batch insert
--------
It can be seen that cpu load goes highest during Gparse batch insert
  
![Image of Yaktocat](./cupload.png)


**Note:** 
- All Numbers are in Seconds.
- Domain autowiring is disabled (As per Grails 3.x Default), validation and databinding are enabled, unless explicitely specified
- One service is injected in each of three domains For the benchmark whith autowiring enabled.
- H2 In memory database is used.

System specs
------------
- Macbook Pro 2.5 GHz Intel Core i7 Quad core, 16 GB RAM
- Gparse pool size of 8
- GRAILS_OPTS="-Xmx2048M -server -XX:+UseParallelGC"


Conclusions
-------

The key conclusions as per my observation are as below

1. Gparse with batch insert has the best performance 
3. use small transaction batches and keep them the same size as the jdbc.batch_size. DO NOT (auto)commit on every insert
4. JDBC Batch size of 50 Gave the best results, as batch size goes higher, performance started to degrade.
4. Don't use GORM data binding if you can avoid it (it almost takes double time).
5. Disabling validation improves performance eg. ```domain.save(false)```
6. Grails Date stamp fields, or audit stamp doesn't have any noticeable effects on performance. That is probably the Event listeners gets called regardless if domain has date stamp fields or not.
7. I did not see any noticeable difference if Domain autowiring is enabled or disabled. (Domain with dependency on one service).
   It made just 2 to 3 seconds difference for 115K records.
8. Using Gpars pool size of 4 on a quad core system seems optimal, Smaller pool size takes longer time, but higher pool size doesnt make any noticeable improvements.



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
