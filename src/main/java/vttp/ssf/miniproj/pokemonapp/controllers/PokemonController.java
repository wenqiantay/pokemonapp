package vttp.ssf.miniproj.pokemonapp.controllers;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.services.PokemonService;
import vttp.ssf.miniproj.pokemonapp.services.RedisService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequestMapping
public class PokemonController {

    @Autowired
    PokemonService pokemonSvc; 
    
    @Autowired
    RedisService redisSvc;

    // Helper method to check reroll limits
    private boolean canReroll(User user, Model model) {
        LocalDate today = LocalDate.now();

        if (user.getLastRerollDate() != null && user.getLastRerollDate().isEqual(today)) {
            if (user.getRerollCounter() >= 3) {
                model.addAttribute("message", "You can only re-roll 3 times per day.");
                model.addAttribute("pokemon", user.getCurrentPokemon());
                return false;
            }
        } else {
            user.setRerollCounter(0); // Reset counter for new day
        }

        return true;
    }

    @GetMapping("/pokemons")
    public String displayPokemons(Model model){

        List<Pokemon> pokemonList = pokemonSvc.getPokemonList();

        model.addAttribute("pokemonlist", pokemonList);

        return "pokemons";
        
    }

    @GetMapping("/game/{username}")
    public String displayRandomPokemon(@PathVariable String username, Model model){

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            
            model.addAttribute("error", "User not found!");
            return "error";
        }

        LocalDate today = LocalDate.now();

        // Check if the user has already rerolled today and if the reroll count is 3 or more
        if (!canReroll(user, model)) {
            return "game";  
        }

        Pokemon randomPokemon = pokemonSvc.getRandomPokemon();
        user.setCurrentPokemon(randomPokemon);

        redisSvc.insertUser(user);
        
        model.addAttribute("pokemon", randomPokemon);

        // Check if any flash attributes exist for a caught Pokémon or a message
        if (model.containsAttribute("message")) {
            model.addAttribute("message", model.asMap().get("message"));
        }

        if (model.containsAttribute("caughtPokemon")) {
            model.addAttribute("caughtPokemon", model.asMap().get("caughtPokemon"));
        }

        return "game";

        }

    @PostMapping("/catch-pokemon/{username}")
    public String catchPokemon(@PathVariable String username, @ModelAttribute("pokemon") Pokemon pokemon, Model model, RedirectAttributes redirectAttributes){

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            redirectAttributes.addFlashAttribute("message", "You need to log in to catch Pokémon.");
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        if (user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)) {

            redirectAttributes.addFlashAttribute("message", "You can only catch one Pokémon per day.");

            return "redirect:/game/{username}"; 
        }

            pokemonSvc.saveCaughtPokemon(pokemon, user);

            user.setLastCatchDate(today);

            //update user
            redisSvc.insertUser(user);

            redirectAttributes.addFlashAttribute("caughtpokemon", pokemon);  // Only add if a catch happens
            redirectAttributes.addFlashAttribute("message", "You caught the Pokémon!");  // Success message


            // model.addAttribute("pokemon", pokemon);
            // model.addAttribute("message", "You caught the Pokémon!");

        
        return "redirect:/game/{username}";
    }

    @PostMapping("/run/{username}")
    public String rerollPokemon(@PathVariable String username, Model model) {
        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "redirect:/login"; 
        }

        if (!canReroll(user, model)) {
            return "redirect:/game/{username}";
        }

        Pokemon randomPokemon = pokemonSvc.getRandomPokemon();
        user.setCurrentPokemon(randomPokemon);
        user.setLastRerollDate(LocalDate.now());
        user.setRerollCounter(user.getRerollCounter() + 1);

        redisSvc.insertUser(user);

        model.addAttribute("pokemon", randomPokemon);
        model.addAttribute("message", "You re-rolled and got a new Pokémon!");

        return "redirect:/game/{username}";
    }

    
}
