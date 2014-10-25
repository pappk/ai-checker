package hu.bme.mi.dama;

public class Cell {
	/* x meghatározza a vízszintes koordinátát
	 * y pedig a függõleges mélységet
	 */
	private int column;
	private int row;
	
	public Cell(int aRow, int aColumn){
		column=aColumn;
		row=aRow;
	}
	
	public void setPosition(int aRow, int aColumn){
		column=aColumn;
		row=aRow;
	}
	
	public int getColumn(){
		return column;
	}
	
	public int getRow(){
		return row;
	}
	
	public boolean equal(Cell c2){
		if(this.getColumn() == c2.getColumn() && this.getRow() == c2.getRow()){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.equal((Cell) obj);
	}

	@Override
	public String toString(){
		return ""+getRow()+", "+getColumn();
	}
}
