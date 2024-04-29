package com.jerry.busappbackend.filter;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestResponseLoggingFilter implements Filter {
    
    private final Logger logger = LogManager.getLogger(RequestResponseLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String transactionId = UUID.randomUUID().toString();

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("X-Transaction-ID", transactionId);

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        logger.info(transactionId + " [Request] [" + request.getMethod() + " " + request.getRequestURI() + "] [" + request.getRemoteAddr() + "]");
        filterChain.doFilter(request, wrappedResponse);

        byte[] responseData = wrappedResponse.getContentAsByteArray();
        String responseBody = new String(responseData, wrappedResponse.getCharacterEncoding());

        logger.info(transactionId + " [Response] [" + wrappedResponse.getStatus() + "]\n" + 
        "================ PAYLOAD START ================\n" +
        "{}\n" + 
        "================= PAYLOAD END =================\n", responseBody);
        wrappedResponse.copyBodyToResponse();
    }
}
