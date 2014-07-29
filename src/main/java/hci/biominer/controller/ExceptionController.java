package hci.biominer.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;


import hci.biominer.util.ErrorModel;

@ControllerAdvice
public class ExceptionController {
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorModel handleException(Exception ex) {
		ErrorModel em = new ErrorModel();
		em.setErrorName("Server error encountered: " + ex.getClass().getSimpleName());
		em.setErrorMessage(ex.getMessage());
		ex.printStackTrace();
		
		return em;
	}

}
