package com.jorge.task;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus; 

/**
 * Generate a JAR file (execute this command at spring9_batchJobs root):
 * 		mvn clean compile assembly:single
 * 
 * Execute the job by running the JAR file generated in the target folder, with the class where the
 * job is defined ( BatchConfig ) and the job name ( job1 ) as arguments:
 * 		java -jar target/spring9_batchJobs-jar-with-dependencies.jar com.jorge.batch.BatchConfig job1
 *
 */
public class Task1 implements Tasklet {
	
	@StepScope // To retrieve and use a job parameter value in Tasklet
	// This method contains the code to be executed for the job
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Starting Task1.execute()");
		
		/**
		 * Retrieve a job parameter value by using the job parameter name
		 * 
		 * Run the job with a parameter named test:
		 * 		mvn compile exec:java -Dexec.mainClass=org.springframework.batch.core.launch.support.CommandLineJobRunner -Dexec.args="com.jorge.batch.BatchConfig job1 test=hello"
		 *
		 * The String test will contain the hello parameter value passed on the command line. This recipe
		 * will also work if the job is launched from a controller method
		 * 
		 */
		if(chunkContext != null){
			String test = (String)chunkContext.getStepContext().getJobParameters().get("test"); 
			System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": RESULT OF TEST: " + test);
		}
		else
			System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": RESULT OF TEST: Test finished correctly");

		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: Task1.execute() done");
		
		return RepeatStatus.FINISHED;
	}
	
}
