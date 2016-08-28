package com.jorge.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.jorge.task.Task1;

@Configuration
@EnableBatchProcessing // Spring configuration class for Spring Batch
					   // The @EnableBatchProcessing annotation enables Spring Batch and
				       // provides reasonable defaults for batch jobs, which can be overridden if necessary (the default
					   // JobLauncher object, the default TransactionManager object, and so on)
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobs;
	
	@Autowired
	private StepBuilderFactory steps;
	
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
	 * mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job1 p=4"
	 * 
	 */
	//Define the step1 bean, which will execute our code, from the Task1 class
	@Bean
	public Step step1(){
		System.out.println("INFO: Starting step1 in BatchConfig.java");
		return steps.get("step1")
		.tasklet(new Task1()) // We defined a job1 job executing the step1 step, which will call the execute() method in the Task1 class.
		//.next(step2()) // This is to execute more than one step
		.build();
	}
		
	//Define the job1 bean that will execute step1
	@Bean
	public Job job1(){
		System.out.println("INFO: Starting job1 in BatchConfig.java");
		return jobs.get("job1")
		.start(step1()).build();
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
