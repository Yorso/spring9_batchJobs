package com.jorge.processor;

import org.springframework.batch.item.ItemProcessor;

import com.jorge.model.User;

public class UserProcessorIncrementAge implements ItemProcessor<User, User>{
	// READ/PROCESS/WRITE STEP: This method takes a User object, increments its age , and returns the modified User object
	public User process(User user) throws Exception {
		int age = user.getAge();
		age++;
		user.setAge(age);
		return user;
	}
}
