package com.unionpay.quartz.task.core;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@Order(-1)
public class GlobalExceptionHandler {
	
    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String runtimeExceptionHandler(RuntimeException e, Model model) {
        log.error("未知异常:", e);
        model.addAttribute("status", 500);
        model.addAttribute("error", "服务器内部错误");
        model.addAttribute("exception", e.getClass().getName());
        model.addAttribute("message", e.getMessage());
        return "error";
    }
}
