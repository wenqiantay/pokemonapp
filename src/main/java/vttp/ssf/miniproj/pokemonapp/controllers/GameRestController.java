package vttp.ssf.miniproj.pokemonapp.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import vttp.ssf.miniproj.pokemonapp.models.User;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

    //Check if user is logged in before playing the game
    @GetMapping("/{username}")
    public ResponseEntity<?> apiDisplayRandomPokemon(@PathVariable String username, HttpSession session, RedirectAttributes redirectAttributes) {
      
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login") 
                    .build();
        }

        return ResponseEntity.ok("User is logged in. Proceed with the game.");
    }
}

