package vttp.ssf.miniproj.pokemonapp.services;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.repository.RedisRepo;

@Service
public class RedisService {
    
    @Autowired
    RedisRepo redisRepo;

    public void insertUser(User user){

        redisRepo.insertUser(user);

    }

    //check if username is unique
    public boolean isUsernameUnique(String username) {
        return redisRepo.isNameUnique(username);
    }

    //Get user details
    public User getUser(String username, String password){

        User user = redisRepo.getUser(username, password);

        if (user == null) {
      
        return null;
    }

        return user;
    }

    public User getUserByUsername(String username){

        return redisRepo.getUsername(username);

    }

    //Check if reroll is available
    public boolean canReroll(User user){

        LocalDate today = LocalDate.now();
        
        if(user.getLastRerollDate() != null && user.getLastRerollDate().isEqual(today)){
            if(user.getRerollCounter() >= 3){
                return false;
            }
        } else {
            user.setRerollCounter(0);
        }

        return true;
        
    }


}

