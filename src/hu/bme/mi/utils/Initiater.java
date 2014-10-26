package hu.bme.mi.utils;

import java.util.ArrayList;
import java.util.List;

public class Initiater {
	List<AgentsTurnListener> listeners = new ArrayList<AgentsTurnListener>();

    public void addListener(AgentsTurnListener toAdd) {
        listeners.add(toAdd);
    }

    public void sayHello() {
        System.out.println("Hello!!");

        // Notify everybody that may be interested.
        for (AgentsTurnListener hl : listeners)
            hl.yourTurn();
    }
}
