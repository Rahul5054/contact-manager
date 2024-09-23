package com.contactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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

import com.contactmanager.dao.ContactRepository;
import com.contactmanager.dao.UserRepository;
import com.contactmanager.entities.Contact;
import com.contactmanager.entities.User;
import com.contactmanager.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data for response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String username = principal.getName();

		System.out.println("USERNAME : " + username);
		// get the user using username(Email)
		User user = userRepository.getUserByUserName(username);

		System.out.println("USER " + user);

		m.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal pricipal) { // principal ke help se user ka uniqe identiy nikal skte
																// hai ye hume spring security se milta hai
		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";

	}

	// add contacts handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";

	}

	// processing add contact form
	@PostMapping("/process-contact")
	 public String ProcessContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		 try {
			 
		 
		String name = principal.getName();
		 
		User user = this.userRepository.getUserByUserName(name);
		
//		if(3>2) {
//			throw new Exception();
//		}
		
		
		//processing and uploding file
		if(file.isEmpty()) {
			//is the file is empty then try our message
		System.out.println("file is empty");
		contact.setImage("blur.jpg");
		}
		else {
			//file the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/image").getFile();
	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
	
			System.out.println("Image is uploded");
		}
		
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Added to database");
		
		 System.out.println("DATA"+contact);
		 //message success
		 
		 session.setAttribute("message", new Message("Your content is added !! Add more..","success"));
		 
		 }catch (Exception e) {
			// TODO: handle exception
			 System.out.println("ERROR "+e.getMessage());
		e.printStackTrace();
		
		//message error
		 session.setAttribute("message", new Message("Something went wrong !! Try Again..","danger"));

		 }
		 
		 
		 return "normal/add_contact_form";
		 
	 }
	
	//show contacts handler
	//per page=5
	//current page=0[page]
	
	@GetMapping("/show_contacts/{page}")//esse dynamic variable aayega
	public String showContact(@PathVariable("page") Integer page, Model model,Principal principal) {
		model.addAttribute("title","show user contacts");
		
		//contact ki list ko bhijna hai
		
		String username=principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		//current page-page
		//contact per page-5
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);//esse sare contact aa jayege
		
		model.addAttribute("currentPage",page); //esse current page dekh sakte hai kis page per hai
		model.addAttribute("totalPages",contacts.getTotalPages()); //esse total kitne page hai
		
		return "normal/show_contacts";
		
	}
	
	//showing particular contact details
	@RequestMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid")Integer cid,Model model,Principal principal) {
	
		System.out.println("CID "+cid);
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
			{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getCname());
			}
		return "normal/contact_detail";
		
	}

	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cid,Model model,HttpSession session,Principal principal) {
		
		Contact contact = this.contactRepository.findById(cid).get();
		
	
		System.out.println("Contact "+contact.getCid());
		
		//contact.setUser(null);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		// after delete contact remove img from contact 
		
		
		
		this.contactRepository.delete(contact);
		
		System.out.println("DELETED");
		
		session.setAttribute("message", new Message("Contact deleted Successfully..","success"));
		
		return "redirect:/user/show_contacts/0";
		
	}
//open update form handler
	@PostMapping("/update_contact/{cid}")
	public String udateForm(@PathVariable("cid") Integer cid, Model m) {
		
		m.addAttribute("title","update contact ");
		
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
		
		
	}
	
	// process update contact handler
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal){
		
		try {
			// old contact details
			
			Contact oldcontactDetail= this.contactRepository.findById(contact.getCid()).get();
			
			
			//image..
			if(!file.isEmpty()) {
				//file work
				
				//rewrite
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				
				File file1=new File(deleteFile,oldcontactDetail.getImage());
file1.delete();
				
				//update new photo
				
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
						contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is update","success"));
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("contact name"+contact.getCname());
		
		
		
		return "redirect:/user/contact/"+contact.getCid();
		
		
		
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		
		m.addAttribute("title","Profile Page");
		
		return "normal/profile";
		
	}
	
}
