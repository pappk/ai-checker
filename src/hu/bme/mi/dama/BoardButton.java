package hu.bme.mi.dama;

import javax.swing.JButton;

public class BoardButton extends JButton{
	Cell cell;
	
	public BoardButton(){
		super();
		this.setOpaque(true);
		this.setBorderPainted(false);
	}
	
	public BoardButton(String s){
		super(s);
		this.setOpaque(true);
		this.setBorderPainted(false);
	}
	
	public boolean equal(BoardButton b2){
		if(this.cell.equal(b2.cell)){
			return true;
		}
		return false;
	}
	
	public Cell getCell(){
		return cell;
	}
}
