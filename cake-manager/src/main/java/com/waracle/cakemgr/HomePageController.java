package com.waracle.cakemgr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomePageController {

	@Autowired
	private CakeServices cakeServices;
	@Autowired
	private CakeRepository cakeRepository;

	@GetMapping
	public String load(Model model) {
		System.out.println("init started");
		ResponseEntity<Object> loadResponse = cakeServices.load();
		if (loadResponse.getStatusCode().equals(HttpStatus.OK)) {
			model.addAttribute("cakes", loadResponse.getBody());
		}
		model.addAttribute("cake", new CakeEntity());
		System.out.println("load finished");

		return "welcome";
	}

	@PostMapping
	public String add(@ModelAttribute("cake") CakeEntity cakeEntity, Model model, final BindingResult result) {

		ResponseEntity<Object> response = cakeServices.add(cakeEntity);
		if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
			if (CakeServices.INVALID_IMAGE_URL_PROVIDED.equals(response.getBody())) {
				model.addAttribute("urlError", true);
			} else if (CakeServices.TITLE_ALREADY_EXISTS.equals(response.getBody())) {
				model.addAttribute("titleError", true);
			}
		}
		model.addAttribute("cakes", cakeRepository.findAll());
		model.addAttribute("cake", new CakeEntity());
		System.out.println("add finished");
		return "welcome";
	}

}
