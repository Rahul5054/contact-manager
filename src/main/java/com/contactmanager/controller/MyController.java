package com.contactmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contactmanager.dao.UserRepository;
import com.contactmanager.entities.User;
import com.contactmanager.helper.Message;

import jakarta.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class MyController {
	
	@Autowired
	private BCryptPasswordEncoder PasswordEncoder;
	
	
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model m) {
		
		m.addAttribute("title","Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("about")
	public String about(Model m) {
		
		m.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("signup")
	public String signup(Model m) {
		
		m.addAttribute("title","signup-Smart Contact Manager");
		m.addAttribute("user",new User());

		return "signup";
	}
	
	//this handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")User user,BindingResult result ,@RequestParam(value="agreement",defaultValue = "false")boolean agreement,Model m, HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("Please check terms and conditions");
			throw new Exception("Please check terms and conditions");
			
			}
			
			//validation condition
			
			if(result.hasErrors()) {
				System.out.println("Errors "+result.toString());
				m.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("Role_User");
			user.setEnabled(true);
			
			user.setPassword(PasswordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
		
			User res = this.userRepository.save(user);
			
			m.addAttribute("user",new User());
			  session.setAttribute("message", new Message("Successfully registered !!", "alert-success"));
	      		return "signup";
			
		}
		catch (Exception e) {
              e.printStackTrace();
              m.addAttribute("user",user);
              session.setAttribute("message", new Message("Something Went Wrong !!"+e.getMessage(), "alert-danger"));
      		return "signup";
		
		}
		
		
	}
	
	//handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","login page");
		return "login";
		
	}
	
}
