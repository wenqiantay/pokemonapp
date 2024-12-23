package vttp.ssf.miniproj.pokemonapp.controllers;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        Pokemon randomPokemon = pokemonSvc.getRandomPokemon();

        model.addAttribute("pokemon", randomPokemon);

        return "game";

    }

    @PostMapping("/catch-pokemon/{username}")
    public String catchPokemon(@PathVariable String username, @ModelAttribute("pokemon") Pokemon pokemon, Model model){

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            model.addAttribute("message", "You need to log in to catch Pokémon.");
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        if (user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)) {
            model.addAttribute("message", "You can only catch one Pokémon per day.");
            return "game"; 
        }

            pokemonSvc.saveCaughtPokemon(pokemon, user);

            user.setLastCatchDate(today);
            redisSvc.insertUser(user);

            model.addAttribute("pokemon", pokemon);
            model.addAttribute("message", "You caught the Pokémon!");

        
        return "redirect:/game/{username}";
    }


    
}
