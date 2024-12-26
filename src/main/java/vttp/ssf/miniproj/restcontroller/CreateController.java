package vttp.ssf.miniproj.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.services.RedisService;

@RestController
@RequestMapping("/api/users")
public class CreateController {

    @Autowired
    RedisService redisSvc;
    
    @PostMapping("/create")
    public ResponseEntity<?> postUser(@RequestBody @Valid User user, @RequestParam("checkpw") String checkpw, @RequestParam("username") String username, BindingResult bindings){

        if(bindings.hasErrors()){

            return ResponseEntity.badRequest().body("Validation failed.");
        }

        if(!checkpw.equalsIgnoreCase(user.getPassword())){

            return ResponseEntity.badRequest().body("Password does not match.");
        }

        if(!redisSvc.isUsernameUnique(username)){

            return ResponseEntity.badRequest().body("Username is already taken.");
        }

        redisSvc.insertUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

}
