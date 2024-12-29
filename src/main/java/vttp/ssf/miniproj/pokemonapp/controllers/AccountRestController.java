package vttp.ssf.miniproj.pokemonapp.controllers;



import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping
public class AccountRestController {

    //Logging out from account
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        
        session.invalidate(); 

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
    
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
