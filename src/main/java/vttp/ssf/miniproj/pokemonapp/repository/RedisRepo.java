package vttp.ssf.miniproj.pokemonapp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Autowired@Qualifier("redis-pokemon")
    RedisTemplate<String, Object> redisTemplatePokemon;

    //hset id username fred
    public void insertUser(User user) {
        
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        Map<String, String> values = new HashMap<>();
        values.put("username",user.getUsername());
        values.put("password", user.getPassword());
        values.put("email", user.getEmail());
        values.put("fullname", user.getFullname());
        values.put("gender", user.getGender());
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

        
        if (user.getUsername() == null) {
           
            return null; 
        }

        
        if (!user.getPassword().equals(password)) {
            
            return null; 
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
