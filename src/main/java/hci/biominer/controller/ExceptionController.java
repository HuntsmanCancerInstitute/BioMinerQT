package hci.biominer.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
		
		//Set stack trace
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		
		em.setErrorStackTrace(exceptionAsString);
		ex.printStackTrace();
		
		//Set time
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date today = Calendar.getInstance().getTime();        
		String errorTime = df.format(today);
		em.setErrorTime(errorTime);
		
		return em;
	}

}
