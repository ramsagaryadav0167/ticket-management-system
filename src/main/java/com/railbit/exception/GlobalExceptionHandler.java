package com.railbit.exception;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Turns expected failures from form posts into a flash message on the page the user came from.
 * Not-found and access-denied fall through to Spring Boot's error pages (templates/error/*.html).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + backTo(request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex,
                                         HttpServletRequest request, RedirectAttributes flash) {
        log.info("Concurrent update rejected: {}", ex.getMessage());
        flash.addFlashAttribute("error", "This record was changed by someone else. Please review and try again.");
        return "redirect:" + backTo(request);
    }

    /** Same-origin path of the referring page, so a forged Referer cannot redirect off-site. */
    private static String backTo(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (uri.getPath() != null && uri.getPath().startsWith("/")) {
                    return uri.getRawQuery() == null ? uri.getPath() : uri.getPath() + "?" + uri.getRawQuery();
                }
            } catch (IllegalArgumentException ignored) {
                // fall through to the home page
            }
        }
        return "/";
    }
}
