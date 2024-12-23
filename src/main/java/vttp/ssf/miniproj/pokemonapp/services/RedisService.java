package vttp.ssf.miniproj.pokemonapp.services;

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

        // Check if the user exists in Redis
        if (user == null) {
        // If user is not found in Redis, return null
        return null;
    }

        return user;
    }

    public User getUserByUsername(String username){

        return redisRepo.getUsername(username);

    }


}

