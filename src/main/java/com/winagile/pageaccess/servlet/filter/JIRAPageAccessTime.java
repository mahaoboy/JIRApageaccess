package com.winagile.pageaccess.servlet.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JIRAPageAccessTime implements Filter {
	private static final Logger log = LoggerFactory
			.getLogger(JIRAPageAccessTime.class);
	private String header;

	public void init(FilterConfig filterConfig) throws ServletException {
		header = filterConfig.getInitParameter("header");
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse) response;
		Cookie cookie = new Cookie(header, String.valueOf(new Date().getTime()));
		res.addCookie(cookie);
		chain.doFilter(request, res);
	}

}