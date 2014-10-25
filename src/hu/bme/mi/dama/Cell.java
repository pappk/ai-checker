package hu.bme.mi.dama;

public class Cell {
	/* x meghat�rozza a v�zszintes koordin�t�t
	 * y pedig a f�gg�leges m�lys�get
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
