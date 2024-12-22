package vttp.ssf.miniproj.pokemonapp.services;



import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

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

@Service
public class PokemonService {

    private String POKE_API_151 = "https://pokeapi.co/api/v2/pokemon?limit=151";

    public List<Pokemon> getPokemons(){
        
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
    
}
