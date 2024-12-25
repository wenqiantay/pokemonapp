package vttp.ssf.miniproj.pokemonapp.services;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.ssf.miniproj.pokemonapp.models.Pokemon;
import vttp.ssf.miniproj.pokemonapp.models.User;
import vttp.ssf.miniproj.pokemonapp.repository.RedisRepo;

@Service
public class PokemonService {

    @Autowired
    RedisRepo redisRepo;

    private final String POKE_API_151 = "https://pokeapi.co/api/v2/pokemon?limit=151";
    private final String POKEMON_KEY = "pokemons";

    public List<Pokemon> getPokemonList(){
        
        List<Pokemon> pokemonList = new LinkedList<>();

        if(redisRepo.pokemonlistExists(POKEMON_KEY)){

            pokemonList = (List<Pokemon>)redisRepo.getPokemonList(POKEMON_KEY);

            return pokemonList;
            
        }

        pokemonList = getPokemonsdata();

        redisRepo.savePokemonDatas(POKEMON_KEY, pokemonList);

        return pokemonList;

    }

    public List<Pokemon> getPokemonsdata(){
        
        List<Pokemon> pokemonList = new LinkedList<>();

        RequestEntity<Void> req = RequestEntity
                                    .get(POKE_API_151)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .build();
        
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
        String payload = resp.getBody();
        
        //Read json data to extract the pokemon data url
        try {

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject jsonData = reader.readObject();
            JsonArray results = jsonData.getJsonArray("results");



            for(int i = 0; i < results.size(); i++) {
                String pokemonUrl = results.getJsonObject(i).getString("url");

                RequestEntity<Void> dataReq = RequestEntity
                                            .get(pokemonUrl)
                                            .accept(MediaType.APPLICATION_JSON)
                                            .build();
                
                ResponseEntity<String> dataResp = restTemplate.exchange(dataReq, String.class);
                String pokemonData = dataResp.getBody();

                Pokemon pokemon = insertPokemonData(pokemonData);
                pokemonList.add(pokemon);
                
            }

            
        } catch (Exception e) {

            e.printStackTrace();
        }

        return pokemonList;

    }

    public Pokemon insertPokemonData(String payload){

        Pokemon pokemon = new Pokemon();
        
        try {
            
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject pokemonData = reader.readObject();
            
            String pokemonName = pokemonData.getString("name");
            int pokemonId = pokemonData.getInt("order");
            String spritesUrl = pokemonData.getJsonObject("sprites").getString("front_default");
            JsonArray typeArray = pokemonData.getJsonArray("types");
            List<String> typesList = new LinkedList<>();
            
            for(int i = 0; i < typeArray.size(); i++) {
                JsonObject obj = typeArray.getJsonObject(i);
                String type = obj.getJsonObject("type").getString("name");
                
                typesList.add(type);
            }
            
            pokemon.setName(pokemonName);
            pokemon.setPokemonid(pokemonId);
            pokemon.setSprite(spritesUrl);
            pokemon.setType(typesList);

            
            
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
        }

        return pokemon;
    }

    //shuffle the pokemon list
    public Pokemon getRandomPokemon(){

        List<Pokemon> pokemonList = (List<Pokemon>) redisRepo.getPokemonList(POKEMON_KEY);

        if(pokemonList == null || pokemonList.isEmpty()){

            return null;
        }

        Collections.shuffle(pokemonList);

        return pokemonList.get(0);
 
    }

    //catch pokemon and save to redis for user
    public void saveCaughtPokemon(Pokemon pokemon, User user){

        List<Pokemon> myPokemonList = user.getMyPokemonList();
    
        if (myPokemonList == null) {
            myPokemonList = new LinkedList<>();
        }
    
        myPokemonList.add(pokemon);

        user.setMyPokemonList(myPokemonList);

        redisRepo.insertUser(user);
    }
    
}
