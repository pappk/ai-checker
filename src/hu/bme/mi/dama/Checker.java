package hu.bme.mi.dama;


public class Checker extends Figure implements java.io.Serializable{
	public Checker(int id, boolean aColor) {
		super(id, aColor);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int possibleMove(Cell a0, Cell a1) {
		if (a0.getRow() == a1.getRow() || a0.getColumn() == a1.getColumn()) {
			return MoveResult.MR_BAD;
		}
		int dRow = Math.abs(a1.getRow() - a0.getRow());
		int dColumn = Math.abs(a1.getColumn() - a0.getColumn());
		if (dRow == 1 && dColumn == 1) {
			return MoveResult.MR_IFNOTATTACKING;
		} else if (dRow == 2 && dColumn == 2) {
			return MoveResult.MR_IFATTACKING;
		}

		return MoveResult.MR_BAD;
	}
	
	@Override
	public String toString(){
		if(color == true)
			return "<html><b><u>V</u></b></html>";
		else
			return "<html><b><u>S</u></b></html>";
	}
	
	@Override
	public boolean isChecker(){
		return true;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Checker(getId(), getColor());
	}
	
	

}
