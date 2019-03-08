package org.demo.app;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

	@RequestMapping(value = "/on")
	public String handler(HttpServletRequest request) {
		
		Enumeration<String> headers = request.getHeaderNames();
		while(headers.hasMoreElements()) {
			String headerName = headers.nextElement();
			String headerValue = request.getHeader(headerName);
			LOG.info("收到headers:{}=>{}", headerName, headerValue);
		}
		return StartupListener.getData().toString();
	}

}
