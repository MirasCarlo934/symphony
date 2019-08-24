package ui.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorController extends AbstController {
	
	public ErrorController(@Value("${log.domain.ui}") String logDomain) {
		super(logDomain, ErrorController.class.getSimpleName());
	}
	
//	@ResponseStatus(HttpStatus.NOT_FOUND)
//	public String notFound(Model model) {
//		String msg = "Error 404: URL cannot be found!";
//		
//		model.addAttribute("msg", msg);
//    		return dispatchView("error");
//    }
//	
//	@ResponseStatus
//	public String handleError(HttpServletResponse res, Model model) {
//		String msg = "An error occurred! (Error " + res.getStatus() + ")";
//		
//		model.addAttribute("msg", msg);
//    		return dispatchView("error");
//	}
}
