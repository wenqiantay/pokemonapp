package vttp.ssf.miniproj.pokemonapp.models;

import java.util.List;

public class Pokemon {
    
    private int pokemonid;
    private String name;

    private String sprite;
    
    private List<String> type;

    public int getPokemonid() {
        return pokemonid;
    }

    public void setPokemonid(int pokemonid) {
        this.pokemonid = pokemonid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }


    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Pokemon [pokemonid=" + pokemonid + ", name=" + name + ", sprite=" + sprite + ", type=" + type + "]";
    }

   
    
    
}
