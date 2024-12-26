package vttp.ssf.miniproj.pokemonapp.controllers;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.services.RedisService;


@Controller
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
    public String postCreate(@RequestBody MultiValueMap<String, String> form, @Valid@ModelAttribute("user") User user, BindingResult bindings, Model model, HttpSession session) {
  
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

        session.setAttribute("user", user);

        model.addAttribute("user", user);
        model.addAttribute("checkpw", checkpw);
        model.addAttribute("username", user.getUsername());
        
        return "redirect:/login";
    }


    @GetMapping("/login")
    public String getLogin(Model model) {
        User user = new User();

        model.addAttribute("user", user);

        return "login";
    }

    @PostMapping("/login")
    public String postLogin(@Valid@ModelAttribute("user") User user, BindingResult bindings, Model model, HttpSession session){
        
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.trim().isEmpty()) {
            
            FieldError loginErr = new FieldError("user", "username", "Username cannot be empty");
            bindings.addError(loginErr);
            return "login"; 
        }

        User retrievedUser = redisSvc.getUser(username, password);
    
    if (retrievedUser == null) {

        
        FieldError loginErr = new FieldError("user", "username", "Invalid username or password");
        bindings.addError(loginErr);
        return "login";
    }   

        session.setAttribute("user", retrievedUser);

        model.addAttribute("username", retrievedUser.getUsername());

        return "redirect:/game/" + retrievedUser.getUsername();
    }

    @PostMapping("/logout")
    public String logout(HttpSession session){

        session.invalidate();

        return "redirect:/login";
    }

    @GetMapping("/profile/{username}")
    public String getAccountStats(@PathVariable String username, HttpSession session, Model model){

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        user = redisSvc.getUserByUsername(username);

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/profile/{username}")
    public String postAccountStats(@PathVariable String username, HttpSession session, Model model){
        
        User user = (User) session.getAttribute("user");

        if (user == null) {

            return "redirect:/login";
        }

        User currentUser = redisSvc.getUserByUsername(username);

        if (user.getMyPokemonList() == null) {
            user.setMyPokemonList(new LinkedList<>());
        }

        // List<Pokemon> myCurrentPokemonList = currentUser.getMyPokemonList();
      
        // if (myCurrentPokemonList == null) {
        //     myCurrentPokemonList = new LinkedList<>();
        //     currentUser.setMyPokemonList(myCurrentPokemonList); 
        // }
        // // System.out.println(myCurrentPokemonList);

        int currentPokemonCount = user.getMyPokemonList().size();

        model.addAttribute("user", user);
        model.addAttribute("currentuser", currentUser);
        model.addAttribute("currentPokemonCount", currentPokemonCount);

        return "profile";
    }

    
}
