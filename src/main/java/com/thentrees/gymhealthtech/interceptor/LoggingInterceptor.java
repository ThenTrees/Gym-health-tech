package com.thentrees.gymhealthtech.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@Order(4) // TODO:
public class LoggingInterceptor implements HandlerInterceptor {

}
