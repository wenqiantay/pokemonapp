package vttp.ssf.miniproj.pokemonapp.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
import vttp.ssf.miniproj.pokemonapp.models.User;

@Repository
public class RedisRepo {
    
    @Autowired@Qualifier("redis-0")
    RedisTemplate<String, String> redisTemplate;

    @Autowired@Qualifier("redis-object")
    RedisTemplate<String, Object> redisTemplateObj;



    //hset id username fred
    public void insertUser(User user) {
        
        HashOperations<String, String, Object> hashOps = redisTemplateObj.opsForHash();

        Map<String, Object> values = new HashMap<>();
        values.put("username",user.getUsername());
        values.put("password", user.getPassword());
        values.put("email", user.getEmail());
        values.put("fullname", user.getFullname());
        values.put("gender", user.getGender());
        values.put("rerollcount", user.getRerollCounter());
        
        if (user.getLastCatchDate() != null) {

            values.put("lastcatchdate", user.getLastCatchDate().toString());

        } else {

            values.put("lastcatchdate", "");
        }

        if (user.getLastRerollDate() != null) {

            values.put("lastrerolldate", user.getLastRerollDate().toString());

        } else {

            values.put("lastrerolldate", "");
        }

        values.put("pokemonlist", user.getMyPokemonList());
        values.put("currentpokemon", user.getCurrentPokemon());

        hashOps.putAll(user.getUsername(), values);

        redisTemplate.opsForValue().set("uniqueusername" + user.getUsername(), user.getFullname());
    
    }

    public boolean isNameUnique(String username) {
        // Check if a key exists for this username in Redis
        return !redisTemplate.hasKey("uniqueusername" + username);
    }

     
    @SuppressWarnings({ "null", "unchecked" })
    public User getUser(String username, String password){

        if (username == null || username.trim().isEmpty()) {

            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        HashOperations<String, String, Object> hashOps = redisTemplateObj.opsForHash();

        User user = new User();
        user.setFullname(hashOps.get(username, "fullname").toString());
        user.setEmail(hashOps.get(username, "email").toString());
        user.setGender(hashOps.get(username, "gender").toString());
        user.setUsername(hashOps.get(username, "username").toString());
        user.setPassword(hashOps.get(username, "password").toString());

        String lastCatchDateString = hashOps.get(username, "lastcatchdate").toString();

        if (lastCatchDateString != null && !lastCatchDateString.isEmpty()) {
            user.setLastCatchDate(LocalDate.parse(lastCatchDateString));
        }
        else {
            user.setLastCatchDate(null); 
        }

        String lastRerollDateStr = hashOps.get(username, "lastrerolldate").toString();
        
        if (lastRerollDateStr != null && !lastRerollDateStr.isEmpty()) {
            user.setLastRerollDate(LocalDate.parse(lastRerollDateStr));
        }

        String rerollCountStr = hashOps.get(username, "rerollcount").toString();

        if (rerollCountStr != null) {
            user.setRerollCounter(Integer.parseInt(rerollCountStr));
        }

        List<Pokemon> pokemonList = (List<Pokemon>) hashOps.get(username, "pokemonlist");
        if (pokemonList != null) {
            user.setMyPokemonList(pokemonList);
        }

        Pokemon currentPokemon = (Pokemon) hashOps.get(username, "currentpokemon");
        if (currentPokemon != null) {
        user.setCurrentPokemon(currentPokemon);
        }

        if (user.getUsername() == null) {
           
            return null; 
        }

        if (!user.getPassword().equals(password)) {
            
            return null; 
        }

        return user;
    }

    @SuppressWarnings({ "null", "unchecked" })
    public User getUsername(String username){

        HashOperations<String, String, Object> hashOps = redisTemplateObj.opsForHash();
        Map<String, Object> userMap = hashOps.entries(username);

        if(userMap.isEmpty()){
            return null;
        }

        User user = new User();
        user.setUsername(userMap.get("username").toString());
        user.setPassword(userMap.get("password").toString());
        user.setEmail(userMap.get("email").toString());
        user.setFullname(userMap.get("fullname").toString());
        user.setGender(userMap.get("gender").toString());

        String lastCatchDateString = hashOps.get(username, "lastcatchdate").toString();

        if (lastCatchDateString != null && !lastCatchDateString.isEmpty()) {
            user.setLastCatchDate(LocalDate.parse(lastCatchDateString));
        }
        else {
            user.setLastCatchDate(null); 
        }

        String lastRerollDateStr = hashOps.get(username, "lastrerolldate").toString();
        
        if (lastRerollDateStr != null && !lastRerollDateStr.isEmpty()) {
            user.setLastRerollDate(LocalDate.parse(lastRerollDateStr));
        }

        String rerollCountStr = hashOps.get(username, "rerollcount").toString();

        if (rerollCountStr != null) {
            user.setRerollCounter(Integer.parseInt(rerollCountStr));
        }
  
        List<Pokemon> pokemonList = (List<Pokemon>) hashOps.get(username, "pokemonlist");
        if (pokemonList != null) {
            user.setMyPokemonList(pokemonList);
        }

        Pokemon currentPokemon = (Pokemon) hashOps.get(username, "currentpokemon");
        if (currentPokemon != null) {
            user.setCurrentPokemon(currentPokemon);
        }
    
        return user;
    }

    //set pokemon pokemonlist
    public void savePokemonDatas(String key, List<Pokemon> pokemonList){

        redisTemplateObj.opsForValue().set(key, pokemonList);

    }

    //exists pokemons
    public boolean pokemonlistExists(String key){
        return redisTemplateObj.hasKey(key);
    }

    //get pokemons
    public List<Pokemon> getPokemonList(String key){

        List<Pokemon> pokemonList = (List<Pokemon>) redisTemplateObj.opsForValue().get(key);

        return pokemonList;
    }


}
