package vttp.ssf.miniproj.pokemonapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.services.RedisService;


@Controller
@SessionAttributes("user")
@RequestMapping
public class AccountController {

    @Autowired
    RedisService redisSvc;


    @GetMapping("/create")
    public String getCreate(Model model){

        User user = new User();

        model.addAttribute("user", user);

        return "create";
    }

    @PostMapping("/create")
    public String postCreate(@RequestBody MultiValueMap<String, String> form, @Valid@ModelAttribute("user") User user, BindingResult bindings, Model model) {
  
        String checkpw = form.getFirst("checkpw");

        String username = form.getFirst("username");

        if(bindings.hasErrors()){
            return "create";
        }


        if(!checkpw.equalsIgnoreCase(user.getPassword())){

            FieldError passworderr = new FieldError("user", "password", "Password does not match" );

            bindings.addError(passworderr);

            return "create";
        }

        if(!redisSvc.isUsernameUnique(username)){
            FieldError usernameerr = new FieldError("user", "username", "Username is already taken");

            bindings.addError(usernameerr);

            return "create";
        }

        redisSvc.insertUser(user);

        model.addAttribute("user", user);
        model.addAttribute("checkpw", checkpw);
        model.addAttribute("username", user.getUsername());
        
        return "redirect:/game/" + user.getUsername();
    }


    @GetMapping("/login")
    public String getLogin(Model model) {
        User user = new User();

        model.addAttribute("user", user);

        return "login";
    }

    @PostMapping("/login")
    public String postLogin(@Valid@ModelAttribute("user") User user, BindingResult bindings, Model model){
        
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.trim().isEmpty()) {
            
            FieldError loginErr = new FieldError("user", "username", "Username cannot be empty");
            bindings.addError(loginErr);
            return "login"; 
        }

        User retrievedUser = redisSvc.getUser(username, password);
    
    if (retrievedUser == null) {

        // If no user is found or password doesn't match
        FieldError loginErr = new FieldError("user", "username", "Invalid username or password");
        bindings.addError(loginErr);
        return "login";
    }

        model.addAttribute("user", retrievedUser);
        model.addAttribute("username", retrievedUser.getUsername());

        return "redirect:/game/" + retrievedUser.getUsername();
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){

        request.getSession().invalidate();;

        return "redirect:/login";
    }

    
}
