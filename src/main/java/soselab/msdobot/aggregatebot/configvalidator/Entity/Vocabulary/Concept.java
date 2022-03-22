package soselab.msdobot.aggregatebot.configvalidator.Entity.Vocabulary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class Concept {
    public String conceptName;
    public ArrayList<String> usedVocabulary;

    public Concept(){
    }

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
