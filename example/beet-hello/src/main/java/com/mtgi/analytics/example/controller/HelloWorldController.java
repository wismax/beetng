package com.mtgi.analytics.example.controller;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.mtgi.analytics.example.model.HelloData;
import com.mtgi.analytics.example.service.HelloService;

/**
 * Web controller providing CRUD operations for our persistent data class,
 * {@link HelloData}.  The underlying application logic is implemented by 
 * {@link HelloService}.
 * 
 * There are no beet-specific characteristics in any of the application classes;
 * it is a normal Spring MVC application.
 */
public class HelloWorldController extends SimpleFormController {

	private HelloService service;

	@Required
	public void setService(HelloService service) {
		this.service = service;
	}

	@Override
	protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
		ModelAndView view = super.onSubmit(command, errors);

		Request request = (Request)command;
		switch (request.getCommand()) {
		
		case update:
			HelloData data = request.getData();
			data.setId(null);
			service.save(data);
			view.addObject("result", "New record saved with ID " + request.getData().getId() + ".");
			break;
			
		case delete:
			service.delete(request.getData());
			view.addObject("result", "Record with ID " + request.getData().getId() + " deleted.");
			break;
			
		case create:
			service.generate();
			view.addObject("result", "Random data inserted.");
			break;

		case clear:
			service.clear();
			view.addObject("result", "All data deleted.");
			break;
		}
		
		return view.addObject("records", service.list("id"));
	}
	
	public static enum Command {
		create, retrieve, update, delete, clear;
	}
	
	public static class Request {

		private Command command;
		private HelloData data;

		public Command getCommand() {
			return command;
		}
		public void setCommand(Command command) {
			this.command = command;
		}
		public HelloData getData() {
			if (data == null)
				data = new HelloData();
			return data;
		}
		public void setData(HelloData data) {
			this.data = data;
		}
		
		
	}
}
