package hu.bme.mi.utils;

import hu.bme.mi.dama.Board;

public class Vertex {
	final private Integer id;
	//final private String name;
	final private Board board;

	public Vertex(Integer id, Board board) {
		this.id = id;
		this.board = board;
	}

	public Integer getId() {
		return id;
	}
	
	public Board getBoard() {
		return board;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return board.toString();
	}

}