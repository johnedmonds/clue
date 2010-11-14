package com.pocketcookies.clue.service.server.web;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

/**
 * Servlet implementation class ClueWebServlet
 */
@Controller
public class ClueWebController {
	private static final long serialVersionUID = 1L;
	private final ClueServiceAPI service;
	private static final Logger logger = Logger
			.getLogger(ClueWebController.class);

	public ClueWebController() {
		super();
		try {
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class, Config.SERVICE_LOCATION);
		} catch (MalformedURLException e) {
			logger.fatal("There was a problem connecting to the service.", e);
			throw new ExceptionInInitializerError(e);
		}
		System.out.println("Here");
	}

	@RequestMapping(value = "/index")
	public ModelAndView index() {
		return new ModelAndView("index");
	}
}