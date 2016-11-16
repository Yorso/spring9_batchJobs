package com.jorge.batch;

import java.util.Date;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.jorge.model.User;
import com.jorge.processor.UserProcessorIncrementAge;
import com.jorge.task.Task1;

@Configuration
@EnableBatchProcessing // Spring configuration class for Spring Batch
					   // The @EnableBatchProcessing annotation enables Spring Batch and
				       // provides reasonable defaults for batch jobs, which can be overridden if necessary (the default
					   // JobLauncher object, the default TransactionManager object, and so on)
@EnableScheduling // Scheduling a job
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobs;
	
	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private JobLauncher jobLauncher;
	

	/*****************
	 * Jobs and Steps*
	 *****************/
	/**
	 * Generate a JAR file (execute this command at spring9_batchJobs root):
	 * 		mvn clean compile assembly:single
	 * 
	 * Execute the job by running the JAR file generated in the target folder, with the class where the
	 * job is defined ( BatchConfig ) and the job name ( job1 ) as arguments:
	 * 		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1
	 *
	 * A job can be executed only once for a given set of parameters. To be able to execute the job again,
	 * just add a parameter using the parameterName=parameterValue syntax:
	 *		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1 p=1
	 *		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1 p=2
	 *		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1 p=3
	 *
	 * When testing and debugging the job, you can use a Unix timestamp to automatically get a different
	 * parameter value each time:
	 * 		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1 p=`date +'%s'`
	 * 
	 * A job can be also be executed directly without having to generate a JAR file first:
	 * 		mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job1 p=4"
	 * 
	 */
	//Define the step1 bean, which will execute our code, from the Task1 class
	@Bean
	public Step step1(){
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting step1 in BatchConfig.java");
		return steps.get("step1")
		.tasklet(new Task1()) // We defined a job1 job executing the step1 step, which will call the execute() method in the Task1 class.
		.build();
	}
		
	//Define the job1 bean that will execute step1
	@Bean
	public Job job1(){
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting job1 in BatchConfig.java");
		return jobs.get("job1")
		.start(step1())
		//.next(step2()) // This is to execute more than one step. If we try to execute step2() this way, we get this error:
						 // nested exception is java.lang.IllegalArgumentException: Path must not be null
						 // It means it can't read parameter "file=CSV/input_data.txt", so in reader method csvFilePath would be null and crash
		.build();
	}
	
	/**
	 * Executing a system command
	 * 
	 * A step can consist of just an execution of a system command. Spring Batch provides a convenient
	 * class for this, SystemCommandTasklet
	 * 
	 * Add a SystemCommandTasklet bean. Declare the system
	 * command to be executed (here, we used the touch Unix command to create an empty file), the
	 * directory to execute it from, and the maximum time allowed for its execution
	 * 
	 * The SystemCommandTasklet class will execute a command from the working directory and kill the
	 * process if it exceeds the timeout value.
	 * 
	 * For a more advanced use of system commands (for example, to get the output of the system command)
	 * extend SystemCommandTasklet and override its execute() method
	 * 
	 */
	// IT DOESN'T WORK!!!!
	/*@Bean
	public SystemCommandTasklet task1() {
		SystemCommandTasklet tasklet = new SystemCommandTasklet();
		
		tasklet.setCommand("touch test.txt");
		tasklet.setWorkingDirectory("/home/jorge");
		tasklet.setTimeout(5000);
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting 'SystemCommandTasklet task1()' in BatchConfig.java => COMMAND: touch test.txt");
		
		return tasklet;
	}*/
	
	/**
	 * Scheduling a job
	 * 
	 * Some jobs need to be executed regularly-every night, every hour, and so on. Spring makes this easy
	 * with the @Scheduled annotation.
	 * 
	 * The job will start getting executed again and again with a 10-second (10000 ms) interval as soon as
	 * the web application is deployed. The job parameter with the new Date() value is used to set a
	 * different parameter value for each launch.
	 * 
	 * The fixedDelay attribute sets a delay of 10 seconds after a job has finished its execution before
	 * launching the next one. To actually run a job every 10 seconds, use fixedRate :
	 * 		@Scheduled(fixedRate=10000)
	 * 		public void runJob1() throws Exception {
	 * 			...
	 * 		}
	 * 
	 * It's also possible to use a regular cron expression:
	 * 		@Scheduled(cron="* /5 * * * *")
	 * 		public void runJob1() throws Exception {
	 * 			...
	 * 		}
	 * 
	 */
	@Scheduled(fixedDelay=10000)
	public void runJob1() throws Exception {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": Launching scheluded job");
		
		//This executes the task (Task1) referred in step1() (... .tasklet(new Task1() ...)) each 10 seconds
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addDate("d", new Date());
		jobLauncher.run(job1(), jobParametersBuilder.toJobParameters()); 
		
	}
	
	/**
	 * Creating a read/process/write step
	 * 
	 * A read/process/write step is a common type of step where some data is read somewhere, processed
	 * in some way, and finally, saved somewhere else. we'll read a CSV file of users,
	 * increment their age, and save the modified users in a database
	 * 
	 * Execute the job with the path to the CSV file as parameter:
	 * 		mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job2 file=CSV/input_data.txt"
	 * 
	 * Then, if you want to execute with same parameter (file=CSV/input_data.txt) you get this error:
	 * 		SEVERE: Job Terminated in error: A job instance already exists and is complete for parameters={file=CSV/input_data.txt}.  If you want to run this job again, change the parameters.
	 * How can I fix it?
	 * 
	 * 
	 */
	// This method generates an User object from a line in the CSV file
	private LineMapper<User> lineMapper() {
		DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		
		lineTokenizer.setNames(new String[]{"firstName","age"});
		lineTokenizer.setIncludedFields(new int[]{0,1});
		lineMapper.setLineTokenizer(lineTokenizer);
		
		BeanWrapperFieldSetMapper<User> fieldSetMapper = new
		BeanWrapperFieldSetMapper<User>();
		
		fieldSetMapper.setTargetType(User.class);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: generating an User object from a line in the CSV file");
		
		return lineMapper;
	}
	
	/**
	 *  READ/PROCESS/WRITE STEP FOR FILES. Comment other reader methods
	 *  
	 *  This will read a CSV file (whose path is the file path of the CSV file), and use the previously
	 *  defined LineMapper object to generate users
	 *  
	 *  FlatFileItemWriter<User> writer method needs this reader method to write an output file
	 *  
	 *  JdbcBatchItemWriter<User> writer method needs this reader method to write in DB.JdbcBatchItemWriter<User> writer method 
	 *  can use StaxEventItemReader<User> reader method to get data from a xml file and write in DB
	 *  
	 */
	/*@Bean
	@StepScope // Necessary to allow to access the job parameters. Otherwise, they are executed too early in the job initialization process
	//@Value("#{jobParameters[file]}")
	public FlatFileItemReader<User> reader(@Value("#{jobParameters[file]}") String csvFilePath) {
		FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
		
		reader.setLineMapper(lineMapper());
		reader.setResource(new PathResource(csvFilePath));
		//reader.setResource(new PathResource("CSV/input_data.txt"));
		reader.setLinesToSkip(1);
		reader.setEncoding("utf-8");
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: reading a CSV file and use the previously defined LineMapper object to generate users");
		
		return reader;
	}*/
	
	/**
	 * READ/PROCESS/WRITE STEP FOR XML FILES. Comment other reader methods
	 * 
	 * XStreamMarshaller generates a User automatically for each person's record. This is configured with the following line:
	 * 		marshaller.setAliases(Collections.singletonMap("person", User.class));
	 * 
	 * Note that the User fields have to match the XML fields ( firstName and age ).
	 * 
	 * Execution:
	 * 		mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job2 file=xml/input_data.xml"
	 * 
	 * JdbcBatchItemWriter<User> writer method needs this reader method to write in DB. JdbcBatchItemWriter<User> writer method 
	 * can use FlatFileItemReader<User> reader method to get data from a file and write in DB
	 * 
	 */
	/*@Bean
	@StepScope
	public StaxEventItemReader<User> reader(@Value("#{jobParameters[file]}") String xmlFilePath) {
		StaxEventItemReader<User> reader = new StaxEventItemReader<User>();
		
		//reader.setResource(new PathResource(xmlFilePath));
		reader.setResource(new PathResource("CSV/output_data.txt"));
		reader.setFragmentRootElementName("person");XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(Collections.singletonMap("person", User.class));
		reader.setUnmarshaller(marshaller);
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: read an XML file and use the previously defined LineMapper object to generate users");
		
		return reader;
	}*/
	
	/**
	 * Reading from a database and writing in a file
	 * 
	 * This recipe shows you how to read data from a database as part of a read/process/write step.
	 * 
	 * A SQL query is executed to get users from the database. BeanPropertyRowMapper generates User
	 * objects from the result. Note that the SQL result's columns ( first_name , age ) have to match the User
	 * fields ( firstName and age ). If the database table has different column names, use SQL aliases to
	 * ensure that:
	 * 		SELECT name1 as first_name, the_age as age FROM user
	 * 
	 * Execute:
	 * 		mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job2 fileOut=CSV/output_data.txt"
	 */
	@Bean
	@StepScope
	public JdbcCursorItemReader<User> reader() {
		JdbcCursorItemReader<User> reader = new JdbcCursorItemReader<User>();
		
		reader.setDataSource(dataSource());
		reader.setSql("SELECT first_name, age FROM user");
		reader.setRowMapper(new BeanPropertyRowMapper<User>(User.class));
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: reading from a database and writing in a file");
		
		return reader;
	}
	
	// READ/PROCESS/WRITE STEP
	@Bean
	public ItemProcessor<User,User> processor() {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: calling  processor to increment age");
		
		return new UserProcessorIncrementAge();
	}
	
	// READ/PROCESS/WRITE STEP: Comment FlatFileItemWriter<User> writer method
	// This method will take a User object and save it in the DATABASE
	/*@Bean
	@StepScope // Necessary to allow to access the job parameters. Otherwise, they are executed too early in the job initialization process
	public JdbcBatchItemWriter<User> writer(){
		JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<User>();
		
		writer.setDataSource(dataSource());
		writer.setSql("INSERT INTO user (first_name, age) " + "VALUES ( :firstName, :age)");
		
		ItemSqlParameterSourceProvider<User> paramProvider = new BeanPropertyItemSqlParameterSourceProvider<User>();
		
		writer.setItemSqlParameterSourceProvider(paramProvider);
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: writing age increment in DB");
		
		return writer;
	}*/
	
	/**
	 *  READ/PROCESS/WRITE STEP: Comment JdbcBatchItemWriter<User> writer method
	 *  
	 *  Generating a CSV file: Write a CSV out file (output_data.txt) 
	 *  reading a CSV input file (input_data.txt) as part of a read/process/write step.
	 *  
	 *  This method will get the fields of a User object, build a comma-separated line with them, and write the line to a CSV file
	 *  
	 *  We need FlatFileItemReader<User> reader method to write output file
	 *  
	 *  Execute the job with the path to the output CSV file as a parameter:
	 *  	mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job2 file=CSV/input_data.txt fileOut=CSV/output_data.txt"
	 *  
	 *  BeanWrapperFieldExtractor extracts the declared fields ( firstName and age ) object. 
	 *  DelimitedLineAggregator builds a comma-separated line with them.
	 *  FlatFileItemWriter writes the line to the file.
	 */
	@Bean
	@StepScope
	public FlatFileItemWriter<User> writer(@Value("#{jobParameters[fileOut]}") String csvFilePath) {
		BeanWrapperFieldExtractor<User> fieldExtractor = new BeanWrapperFieldExtractor<User>();
		
		fieldExtractor.setNames(new String[]{"firstName","age"});
		
		DelimitedLineAggregator<User> lineAggregator = new DelimitedLineAggregator<User>();
		
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);
		
		FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>();
		
		writer.setLineAggregator(lineAggregator);
		writer.setResource(new PathResource(csvFilePath));
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: storing data in an output file");
		
		return writer;
	}
	
	// READ/PROCESS/WRITE STEP 
	@Bean
	public Step step2(){
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting step2 in BatchConfig.java");
		
		return steps.get("step2")
		.<User,User>chunk(1) // Allows the data to be processed and saved by groups (in chunks). This is more efficient for large sets of data
		//.reader(reader(null))
		.reader(reader()) // JdbcCursorItemReader<User> reader method. Comment other reader methods
		.processor(processor())
		//.writer(writer()) // JdbcBatchItemWriter<User> writer method (write in DB). comment FlatFileItemWriter<User> writer method and the line below
		.writer(writer(null)) // FlatFileItemWriter<User> writer method (write in a file passed as parameter). Comment JdbcBatchItemWriter<User> writer method and the line above
		.build();
	}
	
	// READ/PROCESS/WRITE STEP 
	@Bean
	public Job job2(){
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting job2 in BatchConfig.java");
		
		return jobs.get("job2")
		.start(step2())
		.build();
	}
	
	
	
	/***************
	 *  DATABASES  *
	 ***************/ 
	// Database connection details
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/batch_jobs_db");
		dataSource.setUsername("user1");
		dataSource.setPassword("user1pass");
		
		return dataSource;
	}
}
