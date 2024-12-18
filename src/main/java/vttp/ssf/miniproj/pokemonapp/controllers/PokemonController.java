package vttp.ssf.miniproj.pokemonapp.controllers;

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

import jakarta.validation.Valid;
import vttp.ssf.miniproj.pokemonapp.models.User;


@Controller
@RequestMapping
public class PokemonController {


    @GetMapping("/create")
    public String getCreate(Model model){

        User user = new User();

        model.addAttribute("user", user);

        return "create";
    }

    @PostMapping("/create")
    public String postCreate(@RequestBody MultiValueMap<String, String> form, @Valid@ModelAttribute("user") User user, BindingResult bindings, Model model) {
  
        String checkpw = form.getFirst("checkpw");

        if(bindings.hasErrors()){
            return "create";
        }


        if(!checkpw.equalsIgnoreCase(user.getPassword())){

            FieldError err = new FieldError("user", "password", "Password does not match" );

            bindings.addError(err);

            return "create";
        }

        model.addAttribute("user", user);
        model.addAttribute("checkpw", checkpw);
        
        return "main";
    }
    
}
