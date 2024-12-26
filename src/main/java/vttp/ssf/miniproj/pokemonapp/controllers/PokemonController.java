package vttp.ssf.miniproj.pokemonapp.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@Controller
@RequestMapping
public class PokemonController {

    @Autowired
    PokemonService pokemonSvc; 
    
    @Autowired
    RedisService redisSvc;

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

        if(user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)){

            model.addAttribute("message", "You have already caught one Pokemon today. Come back again tomorrow!");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "game";
        }

        boolean rerollAvailable = redisSvc.canReroll(user);

        if(rerollAvailable == false) {

            model.addAttribute("message", "You can only re-roll 3 times per day.");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "game";

        }

        Pokemon randomPokemon = pokemonSvc.getRandomPokemon();

        user.setCurrentPokemon(randomPokemon);

        System.out.println(user.getCurrentPokemon());
        
        redisSvc.insertUser(user);
        
        model.addAttribute("pokemon", randomPokemon);

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

        if (username.equals("Admin")) {
            List<Pokemon> allPokemons = pokemonSvc.getPokemonList();
            user.setMyPokemonList(allPokemons); 
    
            redisSvc.insertUser(user);

        } else {

            LocalDate today = LocalDate.now();

                if (user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)) {

                    redirectAttributes.addFlashAttribute("message", "You can only catch one Pokémon per day.");
                    model.addAttribute("pokemon", user.getCurrentPokemon());

                    return "redirect:/game/{username}"; 
                }

                pokemonSvc.saveCaughtPokemon(pokemon, user);

                user.setLastCatchDate(today);
                user.setCurrentPokemon(pokemon);
                
                Pokemon caughtPokemon = user.getCurrentPokemon();
                System.out.println(caughtPokemon);

                redisSvc.insertUser(user);

                redirectAttributes.addFlashAttribute("caughtPokemon", caughtPokemon);
                redirectAttributes.addFlashAttribute("message", "You caught the Pokémon!"); 
        }
        
        return "redirect:/game/{username}";

    }

    @PostMapping("/run/{username}")
    public String rerollPokemon(@PathVariable String username, Model model) {
        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "redirect:/login"; 
        }

        boolean rerollAvailable = redisSvc.canReroll(user);

        if(rerollAvailable == false) {

            model.addAttribute("message", "You can only re-roll 3 times per day.");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "game";

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
