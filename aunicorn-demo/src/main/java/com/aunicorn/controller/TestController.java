package com.aunicorn.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestController{ 

	@RequestMapping("/index")
	public String service(HttpServletRequest req, HttpServletResponse resp) {
		return "hello";
	}
}
