package vttp.ssf.miniproj.pokemonapp.repository;

import java.io.IOException;
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

    @Autowired@Qualifier("redis-pokemon")
    RedisTemplate<String, Object> redisTemplatePokemon;

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
        
        try {
       
            String pokemonListJson = objectMapper.writeValueAsString(user.getMyPokemonList());
            values.put("pokemonlist", pokemonListJson);

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

        String pokemonListString = hashOps.get(username, "pokemonlist");
        if (pokemonListString != null && !pokemonListString.isEmpty()) {
            try {
               
                List<Pokemon> pokemonList = objectMapper.readValue(pokemonListString, new TypeReference<List<Pokemon>>(){});
                user.setMyPokemonList(pokemonList);

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

        String pokemonListString = hashOps.get(username, "pokemonlist");
        if (pokemonListString != null && !pokemonListString.isEmpty()) {
            try {
               
                List<Pokemon> pokemonList = objectMapper.readValue(pokemonListString, new TypeReference<List<Pokemon>>(){});
                user.setMyPokemonList(pokemonList);

            } catch (IOException e) {

                e.printStackTrace();

            }
        }

        return user;
    }

    //set pokemon pokemonlist
    public void savePokemonDatas(String key, List<Pokemon> pokemonList){

        redisTemplatePokemon.opsForValue().set(key, pokemonList);

    }

    //exists pokemons
    public boolean pokemonlistExists(String key){
        return redisTemplatePokemon.hasKey(key);
    }

    //get pokemons
    @SuppressWarnings("unchecked")
    public List<Pokemon> getPokemonList(String key){

       return (List<Pokemon>) redisTemplatePokemon.opsForValue().get(key);
    }


}
