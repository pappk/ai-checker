package hu.bme.mi.utils;

import hu.bme.mi.utils.ErrorCode.ErrorCodes;


public class GameException extends Exception{
	public Boolean nextPlayer;
	public ErrorCodes errorCode;
	
	public GameException(){
		super();
		this.nextPlayer = null;
		this.errorCode = null;
	}
	
	public GameException(GameException ex){
		super(ex.getMessage());
		this.nextPlayer = ex.nextPlayer;
		this.errorCode = ex.errorCode;
	}
	
	public GameException(String message, Boolean nextPlayer){
		super(message);
		this.nextPlayer = nextPlayer;
		this.errorCode = null;
	}
	
	public GameException(String message, Boolean nextPlayer, ErrorCodes errorCode){
		super(message);
		this.nextPlayer = nextPlayer;
		this.errorCode = errorCode;
	}
}
