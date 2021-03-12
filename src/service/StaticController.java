package service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {
	
	@GetMapping("/hi")
	public String hello(ModelMap model) {
		model.put("user", "Judge");
		return "hello";
	}
}
