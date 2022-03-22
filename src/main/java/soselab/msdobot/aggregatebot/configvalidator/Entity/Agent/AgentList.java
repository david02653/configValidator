package soselab.msdobot.aggregatebot.configvalidator.Entity.Agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class AgentList {
    public ArrayList<Agent> agentList;

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(agentList);
    }
}
