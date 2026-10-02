package com.ppsu.placement.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The user never sees a stack trace: every known failure becomes a friendly page with the right status. */
@ControllerAdvice(annotations = Controller.class)
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound(NotFoundException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String businessConflict(ConflictException e, Model model) {
        model.addAttribute("status", 409);
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    String conflict(Exception e, Model model) {
        model.addAttribute("status", 409);
        model.addAttribute("message", "That action conflicts with the current data. Reload the page and try again.");
        return "error";
    }
}
