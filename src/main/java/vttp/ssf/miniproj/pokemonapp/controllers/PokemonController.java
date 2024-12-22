package vttp.ssf.miniproj.pokemonapp.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
import vttp.ssf.miniproj.pokemonapp.services.PokemonService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequestMapping
public class PokemonController {

    @Autowired
    PokemonService pokemonSvc;

    @GetMapping("/pokemons")
    public String displayPokemons(Model model){

        List<Pokemon> pokemonList = pokemonSvc.getPokemonList();

        model.addAttribute("pokemonlist", pokemonList);

        return "pokemons";
        
    }
    
}
