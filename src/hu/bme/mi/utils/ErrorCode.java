package hu.bme.mi.utils;

public final class ErrorCode {

	
	public static enum ErrorCodes {
		NOFIGUREINFROMCELL(1), INVALIDMOVEMENT(2), THEREISFIGUREINTOCELL(3), FORCEDATTACK(4), NOTYOURTURN(5),
		LOOPATTACK(6);
        private int value;

        private ErrorCodes(int value) {
                this.value = value;
        }
	};  

}
