package vttp.ssf.miniproj.pokemonapp.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
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

        //Check if confirm password input matches the password
        if(!checkpw.equals(user.getPassword())){

            FieldError passworderr = new FieldError("user", "password", "Password does not match" );

            bindings.addError(passworderr);

            return "create";
        }

        //Check if Username is already taken
        if(!redisSvc.isUsernameUnique(username)){
            FieldError usernameerr = new FieldError("user", "username", "Username is already taken");

            bindings.addError(usernameerr);

            return "create";
        }

        redisSvc.insertUser(user);

        session.setAttribute("loggedInUser", user);

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

        //Check if username is empty
        if (username == null || username.trim().isEmpty()) {
            
            FieldError loginErr = new FieldError("user", "username", "Username cannot be empty");
            bindings.addError(loginErr);
            return "login"; 
        }

        User retrievedUser = redisSvc.getUser(username, password);
        
        //Check if the User exist 
        if (retrievedUser == null) {
        
            FieldError loginErr = new FieldError("user", "username", "User does not exist or Invalid password");
            bindings.addError(loginErr);

            model.addAttribute("globalError", "Invalid username or Password.");
            return "login";
        }   

        session.setAttribute("loggedInUser", retrievedUser);

        model.addAttribute("username", retrievedUser.getUsername());

        return "redirect:/game/" + retrievedUser.getUsername();
    }

    @GetMapping("/profile/{username}")
    public String getAccountStats(@PathVariable String username, HttpSession session, Model model){

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        user = redisSvc.getUserByUsername(username);

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/profile/{username}")
    public String postAccountStats(@PathVariable String username, HttpSession session, Model model){
        
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {

            return "redirect:/login";
        }

        User currentUser = redisSvc.getUserByUsername(username);

        if (user.getMyPokemonList() == null) {
            user.setMyPokemonList(new LinkedList<>());
        }

        if(user.getUniquePokemonSet() == null){
            user.setUniquePokemonSet(new HashSet<>());
        }

        //Sort the pokedex in ascending order with the pokemon Id
        List<Pokemon> sortedUniquePokemonList = new ArrayList<>(user.getUniquePokemonSet());
        sortedUniquePokemonList.sort((pokemon1, pokemon2) -> Integer.compare(pokemon1.getPokemonid(), pokemon2.getPokemonid()));

        int currentPokemonCount = user.getMyPokemonList().size();
        int uniquePokemonCount = user.getUniquePokemonSet().size();
        
        model.addAttribute("user", user);
        model.addAttribute("currentuser", currentUser);
        model.addAttribute("sortedUniquePokemonList", sortedUniquePokemonList);
        model.addAttribute("currentPokemonCount", currentPokemonCount);
        model.addAttribute("uniquePokemonCount", uniquePokemonCount);

        return "profile";
    }
 
}
