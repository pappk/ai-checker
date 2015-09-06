package hu.bme.mi.utils;

import hu.bme.mi.agent.Movement;

public class Edge {
	private final Integer id;
	private final Vertex source;
	private final Vertex destination;
	private final double weight;
	private final boolean color;
	private Movement movemnet;

	public Edge(Integer id, Vertex source, Vertex destination, double weight, boolean color, Movement movement) {
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.weight = weight;
		this.color = color;
		this.movemnet = movement;
	}

	public Integer getId() {
		return id;
	}

	public Vertex getDestination() {
		return destination;
	}

	public Vertex getSource() {
		return source;
	}

	public double getWeight() {
		return weight;
	}
	
	public boolean getColor(){
		return color;
	}
	
	public Movement getMovement(){
		return movemnet;
	}

	public void setMovemnet(Movement movemnet) {
		this.movemnet = movemnet;
	}

	@Override
	public String toString() {
		String colorName = "";
		if(color){
			colorName = "Fehér";
		} else {
			colorName = "Fekete";
		}
		return  colorName + ": " + movemnet.toString();
	}
}
