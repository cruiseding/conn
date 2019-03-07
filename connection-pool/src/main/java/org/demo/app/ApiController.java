package org.demo.app;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

	@RequestMapping(value = "/on", method = RequestMethod.GET)
	public String headerHandler(HttpServletRequest request) {
		return StartupListener.getData().toString();
	}

}
