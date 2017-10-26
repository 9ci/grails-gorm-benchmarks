
GORM : batch importing large datasets and a performance benchmarking app
===================================	 

Summary
--------

We have a new client that will be importing large payment data files into out application and it appears that this topic ([Zach][]) has some common mind share right now. Over the years I have seem many recommendations for hibernate performance tuning with large sets of data and most recently for GORM as well. What I haven't seen are any sample straight forward benchmark apps to fiddle with.
So I jammed the beginnings of one together and here it is. 

How to run the benchmarks
-------
- Make sure mysql is running
- Create a database with name gpbench
- Run the application with command ```grails run-app ``` or ```grails run-app -Dgrails.env=production``` or just deploy the war to tomcat.
- Benchmarks are run from the BootStrap.groovy. You will see the results on console. 


The Bemchmarks
-------
1. GPars_batched_transactions_per_thread : Runs the batches in parallel, each batch being the same size as jdbc batch size (50).
2. GPars_single_rec_per_thread_transaction : Gpars Single thread par transaction
3. single_transaction : Insert all records in a single transaction and commit at the end. No Gpars.
4. commit_each_save: Insert each record in individual transactions and commit. No Gpars.
5. batched_transactions: Run batch insert as 1) but without Gparse.

Note: All of above benchmarks are run with and without data binding, and you will see the results for both.

By default, all beanchmarks are run with Gorm domain autowiring enabled. 
If you want to see effect of autowiring domains, just set gorm autowire to false in application.yml

Key conclusions
-------

The 4 key factors I have discovered so far to really speed things up are..

1. Use GPars so you are not firing on the proverbial 1 cylinder. 
2. follow the concepts in the hibernate suggestions here in http://docs.jboss.org/hibernate/core/3.3/reference/en/html/batch.html for chaps 13.1 and 13.2 and set your jdbc.batch_size then go to Ted's article here http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/
3. use small transaction batches and keep them the same size as the jdbc.batch_size. DO NOT (auto)commit on every insert
4. JDBC Batch size of 50 Gave the best results, as batch size goes higher, performance started to degrade.
4. Don't use GORM data binding if you can avoid it (it almost takes double time).
  * DON"T do this -> new SomeGormClass(yourPropSettings) or someGormInstance.properties = [name:'jim',color:'red'] or use bindData()
  * DO explicitly set the values on the fields or your gorm object -> someGormInstance.name = 'jim' ...etc
5. Disabling domain autowiring improves performance. Autowiring adds 10 to 20 seconds overhead for 115K records insert.

My Bench Mark Results and details
-------

* 115k CSV records on a MacBook pro 2.5 GHz Intel Core i7. 2048 mb ram was given to the VM and these were run using grails run-app -Dgrails.env=production
* I'm using MySql as the DB and its installed on my mac too so GPars can't really get all the cores
* all of these have jdbc.batch_size = 50 and use the principals from #2 above and flush/clear every 50 rows
* The test where the batch insert happen in a single transaction can't be tested with GPars since a single transaction can't span multiple threads
* the winner seems to be gpars and batched (smaller chunks) transactions


|                      | All records in single transaction | Commit each record | Batched Transaction - Without Gpars  | Batched Transactions - With Gpars  | Gpars single transaction per thread  |
|----------------------|-----------------------------------|--------------------|--------------------------------------|------------------------------------|--------------------------------------|
| With data binding    | 169.3                             | **358.5**              | 144.423                          | 52.962                             | 105.595                              |
| Without data binding | 95.867                            | 120.361            | 74.225                               | **28.886**                         | 90.627                               |
|                      |                                   |                    |                                      |                                    |                                      |

**Note:** All Numbers are in Seconds. 

| gpars benchs      | time |
|-------------------|------|
|with databinding| 52.962 |
|no binding | 28.886 |
|no autowire | xxx |
|no validation | xxx |
|no binding, no autowire,  no validation | xxxxx |
|grails date stamp fields | xxx |
|audit-trail stamp fields (user and dates)| xxx |
|no dao | xxx |
|DataflowQueue (CED Way) | xxx |

**Note:** unless noted, validation, ajutowiring and databinding are enabled

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
