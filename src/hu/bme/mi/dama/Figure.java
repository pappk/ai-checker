package hu.bme.mi.dama;



public class Figure implements java.io.Serializable{
	/*
	 * false: sötét true: világos
	 */
	final public boolean color;
	private int dir;
	private int id;

	public Figure(int id, boolean aColor) {
		this.id = id;
		this.color = aColor;
		if (color == true) {
			dir = -1;
		} else {
			dir = 1;
		}
	}
	
	public int getId(){
		return id;
	}

	public int getDir() {
		return dir;
	}
	
	public boolean getColor(){
		return color;
	}

	public int possibleMove(Cell a0, Cell a1) {
		if (a0.getRow() == a1.getRow() || a0.getColumn() == a1.getColumn()) {
			return MoveResult.MR_BAD;
		}
		int dRow = (a1.getRow() - a0.getRow());
		dRow *= dir;
		int dColumn = Math.abs(a0.getColumn() - a1.getColumn());
		if (dRow == 1 && dColumn == 1) {
			return MoveResult.MR_IFNOTATTACKING;
		}
		else if (dRow == 2 && dColumn == 2) {
			return MoveResult.MR_IFATTACKING;
		}

		return MoveResult.MR_BAD;
	}
	
	public boolean isChecker(){
		return false;
	}
	
	public boolean equals(Figure fig) {
		if(this.getId() == fig.getId()){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		if (color == true)
			return "v";
		else
			return "s";
	}
	
	
}
