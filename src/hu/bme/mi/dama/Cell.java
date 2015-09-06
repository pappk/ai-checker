package hu.bme.mi.dama;

public class Cell {
	/*
	 * x meghatározza a vízszintes koordinátát y pedig a függőleges mélységet
	 */
	private int column;
	private int row;

	public Cell(int aRow, int aColumn) {
		column = aColumn;
		row = aRow;
	}

	public void setPosition(int aRow, int aColumn) {
		column = aColumn;
		row = aRow;
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	/**
	 * Kiszámítja két cella távolságát
	 * @param cell
	 * @return
	 */
	public double distance(Cell cell) {
		return Math.sqrt(Math.pow((this.getRow() - cell.getRow()), 2)
				+ Math.pow((this.getColumn() - cell.getColumn()), 2));
	}

	public boolean equal(Cell cell) {
		if (this.getColumn() == cell.getColumn()
				&& this.getRow() == cell.getRow()) {
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
	public String toString() {
		return "" + getRow() + ", " + getColumn();
	}
}
