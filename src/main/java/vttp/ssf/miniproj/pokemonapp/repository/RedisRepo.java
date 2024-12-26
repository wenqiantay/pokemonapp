package vttp.ssf.miniproj.pokemonapp.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
import vttp.ssf.miniproj.pokemonapp.models.User;

@Repository
public class RedisRepo {
    
    @Autowired@Qualifier("redis-0")
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    //hset id username fred
    public void insertUser(User user) {
        
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        Map<String, String> values = new HashMap<>();
        values.put("username",user.getUsername());
        values.put("password", user.getPassword());
        values.put("email", user.getEmail());
        values.put("fullname", user.getFullname());
        values.put("gender", user.getGender());
        values.put("rerollcount", String.valueOf(user.getRerollCounter()));
        
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
    
        try {
       
            String pokemonListJson = objectMapper.writeValueAsString(user.getMyPokemonList());
            values.put("pokemonlist", pokemonListJson);

            String currentPokemonJson = objectMapper.writeValueAsString(user.getCurrentPokemon());
            values.put("currentpokemon", currentPokemonJson);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        hashOps.putAll(user.getUsername(), values);

        redisTemplate.opsForValue().set("uniqueusername" + user.getUsername(), user.getFullname());
    
    }

    public boolean isNameUnique(String username) {
        // Check if a key exists for this username in Redis
        return !redisTemplate.hasKey("uniqueusername" + username);
    }

    //keys *
    public Set<String> getKeys() {
        
       return redisTemplate.keys("*");

    }
     
    public User getUser(String username, String password){

        if (username == null || username.trim().isEmpty()) {

            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        User user = new User();
        user.setFullname(hashOps.get(username, "fullname"));
        user.setEmail(hashOps.get(username, "email"));
        user.setGender(hashOps.get(username, "gender"));
        user.setUsername(hashOps.get(username, "username"));
        user.setPassword(hashOps.get(username, "password"));

        String lastCatchDateString = hashOps.get(username, "lastcatchdate");

        if (lastCatchDateString != null && !lastCatchDateString.isEmpty()) {
            user.setLastCatchDate(LocalDate.parse(lastCatchDateString));
        }
        else {
            user.setLastCatchDate(null); 
        }

        String lastRerollDateStr = hashOps.get(username, "lastrerolldate");
        
        if (lastRerollDateStr != null && !lastRerollDateStr.isEmpty()) {
            user.setLastRerollDate(LocalDate.parse(lastRerollDateStr));
        }

        String rerollCountStr = hashOps.get(username, "rerollcount");

        if (rerollCountStr != null) {
            user.setRerollCounter(Integer.parseInt(rerollCountStr));
        }

        String pokemonListString = hashOps.get(username, "pokemonlist");
        if (pokemonListString != null && !pokemonListString.isEmpty()) {
            try {
               
                List<Pokemon> pokemonList = objectMapper.readValue(pokemonListString, new TypeReference<List<Pokemon>>(){});
                user.setMyPokemonList(pokemonList);

            } catch (IOException e) {

                e.printStackTrace();

            }
        }

        String currentPokemonJson = hashOps.get(username, "currentpokemon");
            if (currentPokemonJson != null && !currentPokemonJson.isEmpty()) {
            try {
                Pokemon currentPokemon = objectMapper.readValue(currentPokemonJson, Pokemon.class);
                user.setCurrentPokemon(currentPokemon);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

        if (user.getUsername() == null) {
           
            return null; 
        }

        if (!user.getPassword().equals(password)) {
            
            return null; 
        }

        return user;
    }

    public User getUsername(String username){

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> userMap = hashOps.entries(username);

        if(userMap.isEmpty()){
            return null;
        }

        User user = new User();
        user.setUsername(userMap.get("username"));
        user.setPassword(userMap.get("password"));
        user.setEmail(userMap.get("email"));
        user.setFullname(userMap.get("fullname"));
        user.setGender(userMap.get("gender"));

        String lastCatchDateString = hashOps.get(username, "lastcatchdate");

        if (lastCatchDateString != null && !lastCatchDateString.isEmpty()) {
            user.setLastCatchDate(LocalDate.parse(lastCatchDateString));
        }
        else {
            user.setLastCatchDate(null); 
        }

        String lastRerollDateStr = hashOps.get(username, "lastrerolldate");
        
        if (lastRerollDateStr != null && !lastRerollDateStr.isEmpty()) {
            user.setLastRerollDate(LocalDate.parse(lastRerollDateStr));
        }

        String rerollCountStr = hashOps.get(username, "rerollcount");

        if (rerollCountStr != null) {
            user.setRerollCounter(Integer.parseInt(rerollCountStr));
        }

        String pokemonListString = userMap.get("pokemonlist");
        if (pokemonListString != null && !pokemonListString.isEmpty()) {
            try {
               
                List<Pokemon> pokemonList = objectMapper.readValue(pokemonListString, new TypeReference<List<Pokemon>>(){});
                user.setMyPokemonList(pokemonList);

            } catch (IOException e) {

                e.printStackTrace();

            }
        }

        String currentPokemonJson = userMap.get( "currentpokemon");
         if (currentPokemonJson != null && !currentPokemonJson.isEmpty()) {
        try {
            Pokemon currentPokemon = objectMapper.readValue(currentPokemonJson, Pokemon.class);
            user.setCurrentPokemon(currentPokemon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        return user;
    }

    //set pokemon pokemonlist
    public void savePokemonDatas(String key, List<Pokemon> pokemonList){

        try {

            String pokemonListJson = objectMapper.writeValueAsString(pokemonList);
            redisTemplate.opsForValue().set(key, pokemonListJson);
            
        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    //exists pokemons
    public boolean pokemonlistExists(String key){
        return redisTemplate.hasKey(key);
    }

    //get pokemons
    public List<Pokemon> getPokemonList(String key){

        String pokemonListJson = redisTemplate.opsForValue().get(key);

        if (pokemonListJson != null && !pokemonListJson.isEmpty()) {
            try {
                return objectMapper.readValue(pokemonListJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Pokemon.class));
            
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return null; 

    }


}
