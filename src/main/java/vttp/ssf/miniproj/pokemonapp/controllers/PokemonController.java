package vttp.ssf.miniproj.pokemonapp.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
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
    public String displayPokemons(Model model) {

        List<Pokemon> pokemonList = pokemonSvc.getPokemonList();

        model.addAttribute("pokemonlist", pokemonList);

        return "pokemons";
    }

    @GetMapping("/pokemon-details/{pokemonid}")
    public String viewPokemonDetails(@PathVariable int pokemonid, Model model) {

        Pokemon pokemon = pokemonSvc.getPokemonDetailsById(pokemonid);

        if (pokemon == null) {
            model.addAttribute("error", "Pokémon not found!");
            return "error";
        }
        model.addAttribute("pokemon", pokemon);
        return "pokemondetails";
    }

    @GetMapping("/pokemon-profile/{username}")
    public String viewProfile(@PathVariable String username, Model model, @RequestParam(required = false) String searchTerm) {

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "error";
        }

        List<Pokemon> caughtPokemonList = user.getMyPokemonList();

        int caughtPokemonCount = caughtPokemonList.size();

       
        Set<String> uniquePokemonSet = new HashSet<>();
        for (Pokemon pokemon : caughtPokemonList) {
            uniquePokemonSet.add(pokemon.getName());
        }
        int uniquePokemonCount = uniquePokemonSet.size();


        List<Pokemon> filteredPokemonList;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            filteredPokemonList = pokemonSvc.searchPokemons(user, searchTerm);
        } else {
            filteredPokemonList = user.getMyPokemonList(); 
        }

        List<Pokemon> sortedUniquePokemonList = new ArrayList<>(user.getUniquePokemonSet());
        sortedUniquePokemonList
                .sort((pokemon1, pokemon2) -> Integer.compare(pokemon1.getPokemonid(), pokemon2.getPokemonid()));
        
        model.addAttribute("user", user);
        model.addAttribute("filteredPokemonList", filteredPokemonList);
        model.addAttribute("currentPokemonCount", caughtPokemonCount);
        model.addAttribute("uniquePokemonCount", uniquePokemonCount);
        model.addAttribute("sortedUniquePokemonList", sortedUniquePokemonList);

        return "profile";
    }


    @GetMapping("/")
    public String indexController(Model model) {

        model.addAttribute("user", new User());

        return "login";
    }

    @GetMapping("/game/{username}")
    public String displayRandomPokemon(@PathVariable String username, Model model, HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            String message = "You must log in before playing the game.";
            redirectAttributes.addFlashAttribute("loggedinmessage", message);

            return "redirect:/login";

        }

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "error";
        }

        LocalDate today = LocalDate.now();

        // Check if user has already caught a pokemon today
        if (user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)) {

            model.addAttribute("message", "You have already caught one Pokemon today. Come back again tomorrow!");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "game";
        }

        boolean rerollAvailable = redisSvc.canReroll(user);

        // Check if user has already rerolled 3 times today
        if (rerollAvailable == false) {

            model.addAttribute("message", "You can only re-roll 3 times per day.");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "game";

        }

        Pokemon randomPokemon = pokemonSvc.getRandomPokemon();

        user.setCurrentPokemon(randomPokemon);
        redisSvc.insertUser(user);

        session.setAttribute("loggedInUser", user);

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
    public String catchPokemon(@PathVariable String username, @ModelAttribute("pokemon") Pokemon pokemon, Model model,
            RedirectAttributes redirectAttributes, HttpSession session) {

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {
            redirectAttributes.addFlashAttribute("message", "You need to log in to catch Pokemon.");
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        if (user.getLastCatchDate() != null && user.getLastCatchDate().isEqual(today)) {

            redirectAttributes.addFlashAttribute("message", "You can only catch one Pokemon per day.");
            model.addAttribute("pokemon", user.getCurrentPokemon());

            return "redirect:/game/{username}";
        }

        Pokemon currentPokemon = user.getCurrentPokemon();

        // Allow the user to catch the pokemon
        if (currentPokemon != null) {

            pokemonSvc.saveCaughtPokemon(currentPokemon, user);
            user.setLastCatchDate(today);
            user.setCurrentPokemon(currentPokemon);

            redisSvc.insertUser(user);
        }

        session.setAttribute("loggedInUser", user);

        redirectAttributes.addFlashAttribute("caughtPokemon", currentPokemon);
        redirectAttributes.addFlashAttribute("message", "You caught the Pokemon!");

        return "redirect:/game/{username}";

    }

    @PostMapping("/run/{username}")
    public String rerollPokemon(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {

        User user = redisSvc.getUserByUsername(username);

        if (user == null) {

            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/login";
        }

        boolean rerollAvailable = redisSvc.canReroll(user);

        // Check if user still has reroll chances
        if (rerollAvailable == false) {

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
        model.addAttribute("message", "You re-rolled and got a new Pokemon!");

        return "redirect:/game/{username}";
    }

}
