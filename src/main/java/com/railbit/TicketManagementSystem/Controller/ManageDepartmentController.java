// Declares the package this class belongs to
package com.railbit.TicketManagementSystem.Controller;

// Spring MVC annotations
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Marks this class as a Spring MVC Controller
@Controller
// Base URL mapping for all methods in this controller: /admin/department/...
@RequestMapping("/admin/department")
public class ManageDepartmentController {

    // ✅ IT Support
    // Maps GET request to /admin/department/it-support
    @GetMapping("/it-support")
    public String viewITSupportDepartment() {
        // Returns the Thymeleaf view located at templates/departments/it-support.html
        return "departments/it-support";
    }

    // ✅ Finance
    // Maps GET request to /admin/department/finance
    @GetMapping("/finance")
    public String viewFinanceDepartment() {
        // Returns the Thymeleaf view located at templates/departments/finance.html
        return "departments/finance";
    }

    // ✅ HR
    // Maps GET request to /admin/department/hr
    @GetMapping("/hr")
    public String viewHRDepartment() {
        // Returns the Thymeleaf view located at templates/departments/hr.html
        return "departments/hr";
    }

    // ✅ Legal
    // Maps GET request to /admin/department/legal
    @GetMapping("/legal")
    public String viewLegalDepartment() {
        // Returns the Thymeleaf view located at templates/departments/legal.html
        return "departments/legal";
    }

    // ✅ Operations
    // Maps GET request to /admin/department/operations
    @GetMapping("/operations")
    public String viewOperationsDepartment() {
        // Returns the Thymeleaf view located at templates/departments/operations.html
        return "departments/operations";
    }
}
