    package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.helper.Message;
import com.smart.modal.User;

@Controller
   public class HomeController{
	 
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title" ,"home-smart contect manager");
		return "home";
	}
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title" ,"About-smart contect manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title" ,"Register-smart contect manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	//handler for registering user
//	@RequestMapping(value= "/do_register",method = RequestMethod.POST)
	@PostMapping("/do_register")
	public String registerUser(@Valid  @ModelAttribute("user") User user,BindingResult result1 ,@RequestParam(value ="agreement",defaultValue = "false" )
	boolean agreement,Model model,HttpSession session) 
	{
		  try {
			  if(!agreement) {
				   System.out.println("you have not agreed the terms and conditions");
				   throw new Exception("you have not agreed the terms and conditions");
				   } 
			  
			  if(result1.hasErrors()) {
				  System.out.println("error"+result1.toString());
				  model.addAttribute("User" ,user);
				  
				  return "signup";
				  
			  }
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
			User result=this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully registred" , "alert-error"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("User", user);
			session.setAttribute("message", new Message("something went wrong!!!" + e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		 
		
	}
	
// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title" ,"Login page");
		
		return "login";
		
	}
	
	   
   }