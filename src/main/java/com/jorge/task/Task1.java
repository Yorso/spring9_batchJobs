package com.jorge.task;

import org.springframework.batch.core.StepContribution;
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
	
	// This method contains the code to be executed for the job
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		System.out.println("INFO: Starting job in Task1.java");
		// TODO: Code here
		System.out.println("INFO: Job done in Task1.java");
		
		return RepeatStatus.FINISHED;
	}
	
}
