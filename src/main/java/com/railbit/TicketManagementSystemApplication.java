 package com.railbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.railbit.Repository.UserRepository;

@SpringBootApplication
public class TicketManagementSystemApplication {

	public static void main(String[] args) {
		ApplicationContext context=SpringApplication.run(TicketManagementSystemApplication.class, args);
	    
		UserRepository userRepository=context.getBean(UserRepository.class);

	}

}
