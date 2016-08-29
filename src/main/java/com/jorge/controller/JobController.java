package com.jorge.controller;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Executing a job from a controller method
 * 
 * It's convenient to launch a job from a controller method when that job is triggered by a user action.
 * For example, launching a job to process a video just uploaded by the user
 *
 */
@Controller
public class JobController {

	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	Job job;
	
	@RequestMapping("home")
	@ResponseBody
	public String example(){
		String res = null;
		try {
			// The job parameters are the same as those in the command line
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			jobParametersBuilder.addDate("d", new Date());
			jobLauncher.run(job, jobParametersBuilder.toJobParameters());
			res = "Job launched!!!";
			System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": Job launched!!!");
		} catch (Exception e) {
			res = "ERROR: " + e.toString();
			System.out.println(this.getClass().getSimpleName() + "." + new Exception().getStackTrace()[0].getMethodName() + ": ERROR: " + e.toString());
		}
		
		return "<b>" + res + "</b>";
	}
}
