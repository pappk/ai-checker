package hu.bme.mi.agent;

import hu.bme.mi.dama.Cell;

public class Movement {
	private Cell from;
	private Cell to;
	private Double h;
	
	public Movement(Cell from, Cell to){
		this.from = from;
		this.to = to;
		this. h = null;
	}
	
	public Movement(Cell from, Cell to, Double h){
		this.from = from;
		this.to = to;
		this. h = h;
	}

	public Cell getFrom() {
		return from;
	}

	public void setFrom(Cell from) {
		this.from = from;
	}

	public Cell getTo() {
		return to;
	}

	public void setTo(Cell to) {
		this.to = to;
	}

	public Double getH() {
		return h;
	}

	public void setH(Double h) {
		this.h = h;
	}
	
	
}
