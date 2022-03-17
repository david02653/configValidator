package soselab.msdobot.aggregatebot.configvalidator.Entity.UpperIntent;

import com.google.gson.Gson;
import soselab.msdobot.aggregatebot.configvalidator.Entity.Capability.Capability;

import java.util.ArrayList;

public class UpperIntentList {

    public ArrayList<UpperIntent> crossCapabilityList;

    public UpperIntentList(){
    }

    public ArrayList<Capability> getSemiCapabilityList(String intent){
        for(UpperIntent upperIntent : crossCapabilityList){
            if(upperIntent.upperIntent.equals(intent)) {
                return upperIntent.getSequencedSemiCapabilityList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String toString(){
        return new Gson().toJson(this);
    }
}
