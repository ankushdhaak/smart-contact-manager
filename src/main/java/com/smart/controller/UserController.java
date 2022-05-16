package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.helper.Message;
import com.smart.modal.Contact;
import com.smart.modal.User;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	// for all controller
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME "+ userName);
		
		//get the user using userName(email)
		
	   User user = userRepository.getUserByUserName(userName);
	   System.out.println("USER"+ user);
	   
	   model.addAttribute("user", user);
	   
		
	}
	// Dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		
		return"normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","add contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
		
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam ("profileImage")MultipartFile file ,  
			Principal principal,HttpSession session) {
		try {
		String name=principal.getName();
		
		User user=this.userRepository.getUserByUserName(name);
		//processing and uploading file
		if(file.isEmpty()) {
			System.out.println("file is empty");
			contact.setImage("contact.png");
			
		}else {
			contact.setImage(file.getOriginalFilename());
			
			File saveFile=new ClassPathResource("static/img").getFile();
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		//System.out.println("Data" + contact);
		  
		System.out.println("Added to data base");
		//message success
		session.setAttribute("message", new Message("your contact is added!!add more","success"));
		
		
		}catch (Exception e) {
		System.out.println("Error"+e.getMessage());
		e.printStackTrace();
		//error message
		session.setAttribute("message", new Message("something went wrong!!try again..","Danger"));

		
		}
		
		return "normal/add_contact_form";
	}
	// contact view handler
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable Integer page,Model m,Principal principal) {
		m.addAttribute("title","show view contacts");
		//sent the contact list
		
//		String userName=principal.getName();
//		User user=this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts=user.getContacts();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable=PageRequest.of(page, 2);
		
		Page<Contact> contacts=this.contactRepository.FindContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentpage", page  );
		m.addAttribute("totalpages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	@RequestMapping("/{cid}/contacts")
	public String showContactDetails(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		//System.out.println("cid"+ cid);
		
		Optional<Contact> optionalContact =this.contactRepository.findById(cid);
		Contact contact= optionalContact.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
			
		}
		
		
		return "normal/contact_details";
		
	}
	//delete contact handler
	@GetMapping("/delete/{cid}")
    public String DeleteContact(@PathVariable("cid") Integer cid ,Model model,HttpSession session,Principal principal) {
		
		Optional<Contact> optionalCoOptional=this.contactRepository.findById(cid);
		
		Contact contact=optionalCoOptional.get();
		System.out.println("contact"+contact.getCid());
	//	contact.setUser(null);
   //	contactRepository.delete(contact);
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		
		
		return "redirect:/user/show-contacts/0";
    // open update form handler	
    }
	@PostMapping("/update-contact/{cid}")
	public String updateForm( @PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "update-contact");
		Contact contact=this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		
		return "normal/update-form";
	}
	// open process form handler
	@RequestMapping(value = "/process-update" ,method = RequestMethod.POST)
  public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model
		  model,HttpSession session , Principal principal) {
	  try {
		  // old contact details
		Contact oldContact=  this.contactRepository.findById(contact.getCid()).get();
		  
		  if(!file.isEmpty()) {
			// work on file or profile image
			  File deletefile=new ClassPathResource("static/img").getFile();
			  File file1= new File(deletefile,oldContact.getImage());
			  file1.delete();
			  
			  
			  
			  
			// first thinks old photo delete than updates new photos
			  File saveFile=new ClassPathResource("static/img").getFile();
			  Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
			  Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
			  contact.setImage(file.getOriginalFilename());
				
			  
		  }
		  else {
			  contact.setImage(oldContact.getImage());
		  }
		  User user=userRepository.getUserByUserName(principal.getName());
		  contact.setUser(user);
		  this.contactRepository.save(contact);
		  session.setAttribute("message", new Message("your contact is updated....", "success"));
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	  
	  return "redirect:/user/"+contact.getCid()+"/contacts";
  }
	//User Profile
	@GetMapping("/profile")
	public String userProfile(Model model) {
		model.addAttribute("title ", "profile page");
		
		return "normal/profile";
	}
	
	
	
	
	
	
	
	
	
}
