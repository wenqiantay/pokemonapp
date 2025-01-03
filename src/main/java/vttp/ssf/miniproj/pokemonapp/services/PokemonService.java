package vttp.ssf.miniproj.pokemonapp.services;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<Pokemon> getPokemonList() {

        List<Pokemon> pokemonList = redisRepo.getPokemonList(POKEMON_KEY);

        if (pokemonList == null || pokemonList.isEmpty()) {
            pokemonList = getPokemonsdata();
        }

        return pokemonList;
    }

    // Getting data from API
    public List<Pokemon> getPokemonsdata() {

        List<Pokemon> pokemonList = new LinkedList<>();

        RequestEntity<Void> req = RequestEntity
                .get(POKE_API_151)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
        String payload = resp.getBody();

        try {

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject jsonData = reader.readObject();
            JsonArray results = jsonData.getJsonArray("results");

            for (int i = 0; i < results.size(); i++) {
                String pokemonUrl = results.getJsonObject(i).getString("url");

                RequestEntity<Void> dataReq = RequestEntity
                        .get(pokemonUrl)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

                ResponseEntity<String> dataResp = restTemplate.exchange(dataReq, String.class);
                String pokemonData = dataResp.getBody();

                Pokemon pokemon = insertPokemonData(pokemonData);
                Pokemon detailedPokemon = getPokemonDetails(pokemon.getName());

                pokemon.setEvolvesFrom(detailedPokemon.getEvolvesFrom());
                pokemon.setFunfact(detailedPokemon.getFunfact());

                pokemonList.add(pokemon);

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        redisRepo.savePokemonDatas(POKEMON_KEY, pokemonList);

        return pokemonList;

    }

    // Insert the Pokemon data from API to Pokemon
    public Pokemon insertPokemonData(String payload) {

        Pokemon pokemon = new Pokemon();

        try {

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject pokemonData = reader.readObject();

            String pokemonName = pokemonData.getString("name");
            int pokemonId = pokemonData.getInt("order");
            String spritesUrl = pokemonData.getJsonObject("sprites").getString("front_default");
            JsonArray typeArray = pokemonData.getJsonArray("types");
            List<String> typesList = new LinkedList<>();

            for (int i = 0; i < typeArray.size(); i++) {
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

    // Get detailed data for a specific Pokemon by name
    public Pokemon getPokemonDetails(String pokemonName) {
        Pokemon pokemon = null;

        String url = "https://pokeapi.co/api/v2/pokemon-species/" + pokemonName;

        RequestEntity<Void> req = RequestEntity
                .get(url)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
        String payload = resp.getBody();

        try {

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject pokemonData = reader.readObject();

            pokemon = new Pokemon();

            if (pokemonData.containsKey("evolves_from_species") && !pokemonData.isNull("evolves_from_species")) {
                JsonObject evolvesFromSpecies = pokemonData.getJsonObject("evolves_from_species");

                if (evolvesFromSpecies != null) {
                    pokemon.setEvolvesFrom(evolvesFromSpecies.getString("name"));
                } else {
                    pokemon.setEvolvesFrom("-");
                }
            }

            JsonArray factsArray = pokemonData.getJsonArray("flavor_text_entries");
            if (factsArray != null && !factsArray.isEmpty()) {
                for (int i = 0; i < factsArray.size(); i++) {
                    JsonObject obj = factsArray.getJsonObject(i);
                    String language = obj.getJsonObject("language").getString("name");
                    if ("en".equals(language)) {  
                        String rawFact = obj.getString("flavor_text");

                     
                        String sanitizedFact = rawFact.replaceAll("[\\n\\f]", " ").trim();

                        pokemon.setFunfact(sanitizedFact);
                        break; 
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pokemon;
    }

    // Shuffle the pokemon list
    public Pokemon getRandomPokemon() {

        List<Pokemon> pokemonList = (List<Pokemon>) redisRepo.getPokemonList(POKEMON_KEY);

        if (pokemonList == null || pokemonList.isEmpty()) {

            return null;
        }

        Collections.shuffle(pokemonList);

        return pokemonList.get(0);

    }

    // Catch pokemon and save to redis for user
    public void saveCaughtPokemon(Pokemon pokemon, User user) {

        List<Pokemon> myPokemonList = user.getMyPokemonList();
        Set<Pokemon> uniquePokemonSet = user.getUniquePokemonSet();

        if (myPokemonList == null) {
            myPokemonList = new LinkedList<>();
        }

        if (uniquePokemonSet == null) {
            uniquePokemonSet = new HashSet<>();
        }

        boolean isDuplicate = false;
        for (Pokemon existingPokemon : uniquePokemonSet) {
            if (existingPokemon.getPokemonid() == pokemon.getPokemonid()) {
                isDuplicate = true;
                break;
            }
        }

        if (!isDuplicate) {
            uniquePokemonSet.add(pokemon);
        }

        myPokemonList.add(pokemon);

        user.setMyPokemonList(myPokemonList);
        user.setUniquePokemonSet(uniquePokemonSet);

        redisRepo.insertUser(user);
    }

    // Get UniquePokemonSet from Redis
    public Set<Pokemon> getUniquePokemonSet() {

        Set<Pokemon> uniquePokemonSet = new HashSet<>();

        List<Pokemon> pokemonList = getPokemonList();

        uniquePokemonSet.addAll(pokemonList);

        return uniquePokemonSet;
    }

    // Get Pokémon details by ID
    public Pokemon getPokemonDetailsById(int id) {

        List<Pokemon> pokemonList = redisRepo.getPokemonList(POKEMON_KEY);

        if (pokemonList == null || pokemonList.isEmpty()) {
            pokemonList = getPokemonsdata();
        }

        for (Pokemon pokemon : pokemonList) {
            if (pokemon.getPokemonid() == id) {
                return pokemon;
            }
        }

        return null;
    }

    //Search Pokemon
    public List<Pokemon> searchPokemons(User user, String searchTerm) {

        List<Pokemon> allPokemon = user.getMyPokemonList();

        // Filter Pokemon by name
        return allPokemon.stream()
            .filter(pokemon -> pokemon.getName().toLowerCase().contains(searchTerm.toLowerCase()))
            .collect(Collectors.toList());
    }

}
