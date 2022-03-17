package soselab.msdobot.aggregatebot.configvalidator.Entity.Agent;

import com.google.gson.Gson;

import java.util.ArrayList;

public class AgentList {
    public ArrayList<Agent> agentList;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(agentList);
    }
}
