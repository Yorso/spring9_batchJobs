package com.jorge.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jorge.config.AppConfig;
import com.jorge.task.Task1;

import junit.framework.Assert;

/*
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import java.util.List;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;*/

/**
 * Unit testing batch jobs
 * 
 * Spring Batch provides different ways to test a batch job; the whole job, only one step, or just a Tasklet class can be tested.
 *
 * The Spring Batch configuration class has to be loaded, so that the test methods can access the job and
 * its steps. JobLauncherTestUtils is a helper class that is used to easily execute a job or one of its steps
 * 
 */
// Using JUnit
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BatchConfig.class})
public class BatchTest {
	
// Using TestNG
//@ContextConfiguration(classes = {BatchConfig.class})
//public class BatchJob1Test extends AbstractTestNGSpringContextTests {

	// IT DOESN'T WORK, ERROR: Unsatisfied dependency expressed through field 'jobLauncherTestUtils': No qualifying bean of type [org.springframework.batch.test.JobLauncherTestUtils] found for dependency [org.springframework.batch.test.JobLauncherTestUtils]: expected at least 1 bean which qualifies as autowire candidate for this dependency. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type [org.springframework.batch.test.JobLauncherTestUtils] found for dependency [org.springframework.batch.test.JobLauncherTestUtils]: expected at least 1 bean which qualifies as autowire candidate for this dependency. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	// This is how you can test an entire job, check its exit status, and the number of steps that were	executed:
	@Test
	public void testJob() throws Exception {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: starting testing job");
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		Assert.assertEquals(1, jobExecution.getStepExecutions().size());
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: testing jobs finished");
	}
	
	// This is how you can test a specific step:
	@Test
	public void testStep() throws Exception {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: starting testing an specific step 'step1'");
		
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: testing 'step1' finished");
	}
	
	// This is how you can test Tasklet:
	@Test
	public void testTasklet() throws Exception {
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: starting testing specific tasklet 'Task1'");
		
		Task1 task1 = new Task1();
		Assert.assertEquals(RepeatStatus.FINISHED, task1.execute(null, null));
		
		System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": INFO: testing 'Task1' finished");
	}
}
