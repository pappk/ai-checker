package hu.bme.mi.utils;

public final class GameEvent {
	public static enum GameEvents {
		KEEPGOING(0), WINNERWHITE(1), WINNERBLACK(2), TIE(3);
        private int value;

        private GameEvents(int value) {
                this.value = value;
        }
	};  
}
