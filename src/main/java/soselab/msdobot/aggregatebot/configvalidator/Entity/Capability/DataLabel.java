package soselab.msdobot.aggregatebot.configvalidator.Entity.Capability;

import com.google.gson.GsonBuilder;

public class DataLabel {

    public String label;
    public String concept;

    public DataLabel(){
    }

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
