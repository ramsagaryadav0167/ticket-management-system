// Package declaration
package com.railbit.TicketManagementSystem.Controller;

// Required imports
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.railbit.TicketManagementSystem.Entity.TechnicalSupportDepartment;
import com.railbit.TicketManagementSystem.Service.TechnicalSupportDepartmentService;

// Marks this class as a Spring MVC controller
@Controller
// Base URL for all endpoints in this controller: /admin/technical-support
@RequestMapping("/admin/technical-support")
public class TechnicalSupportDepartmentController {
	/*
	 * // Injects the TechnicalSupportDepartmentService to perform business logic
	 * 
	 * @Autowired private TechnicalSupportDepartmentService departmentService;
	 * 
	 */
}
