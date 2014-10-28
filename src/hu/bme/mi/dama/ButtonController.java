package hu.bme.mi.dama;

import hu.bme.mi.agent.Agent;
import hu.bme.mi.utils.GameEvent.GameEvents;
import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.Initiater;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonController {
	Board board;
	ButtonView view;
	BoardButton b0 = null;
	BoardButton b1 = null;
	BoardButton b = null;
	BoardButton bPrev = null;
	Initiater initiater = new Initiater();
	Agent agent = null;
	GameEvents status = null;

	public ButtonController() {
		board = new Board(initiater, false);
		view = new ButtonView(board);
		view.setVisible(true);
		agent = new Agent(this, false);
		initiater.addListener(agent);
		status = GameEvents.KEEPGOING;

		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b = (BoardButton) e.getSource();

				controllClick2(b);
			}
		};
		Component[] buttons = view.getButtons();
		for (int i = 0; i < buttons.length; i++) {
			((BoardButton) buttons[i]).addActionListener(buttonListener);
		}
	}

	public Board getBoard() {
		return this.board;
	}

	/**
	 * F�ggv�ny, ami lekezel egy l�p�st �s a l�p�s hat�s�ra felmer�l� jelz�seket
	 * 
	 * @param from
	 *            L�p�s innen
	 * @param to
	 *            L�p�s ide
	 * @throws GameException 
	 */
	public void handlePlayerMovement(Cell from, Cell to) throws GameException {
		try {

			status = board.moveFigureFromTo(from, to);
			view.setDefaultPaintAll();
			bPrev = null;

			view.reset();

			if (status != GameEvents.KEEPGOING) {
				view.setWinnerLabel(status);
			}
		} catch (GameException ex) {
			throw new GameException(ex);
		}
	}

	public void controllClick2(BoardButton b) {
		if (status == GameEvents.KEEPGOING) {
			// Hanyadik kattint�s, volt m�r el�tte egy?
			if (bPrev == null) {
				// Az els� kattint�s csak kijel�l
				if (board.getFigure(b.getCell()) != null
						&& board.getFigure(b.getCell()).getColor() == board
								.getWhiteOnTurn()) {
					bPrev = b;
					view.highlightCell(bPrev.getCell());
				}
			} else {
				// Ha volt m�r kijel�lt els� kattint�s
				if (b.equals(bPrev)) {
					view.setDefaultPaint(bPrev.getCell());
					bPrev = null;
				} else {
					if (board.getFigure(b.getCell()) == null) {
						view.highlightCell(b.getCell());
						try {
							handlePlayerMovement(bPrev.getCell(), b.getCell());
						} catch (GameException ex) {
							view.setErrorLabel(ex.getMessage());
							view.setDefaultPaintAll();
							bPrev = null;
						}
					}
				}
			}
		}
	}
}
