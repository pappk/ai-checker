package hu.bme.mi.dama;

import hu.bme.mi.utils.GameException;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonController {
	Board board;
	ButtonView view;
	BoardButton b0 = null;
	BoardButton b1 = null;
	BoardButton b = null;
	BoardButton bPrev = null;

	public ButtonController() {
		board = new Board();
		view = new ButtonView(board);
		view.setVisible(true);

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

	public void controllClick2(BoardButton b) {
		// Hanyadik kattintás, volt már elõtte egy?
		if (bPrev == null) {
			// Az elsõ kattintás csak kijelöl
			if (board.getFigure(b.getCell()) != null
					&& board.getFigure(b.getCell()).getColor() == board
							.getWhiteOnTurn()) {
				bPrev = b;
				view.highlightCell(bPrev.getCell());
			}
		} else {
			// Ha volt már kijelölt elsõ kattintás
			if (b.equals(bPrev)) {
				view.setDefaultPaint(bPrev.getCell());
				bPrev = null;
			} else {
				if (board.getFigure(b.getCell()) == null) {
					try {
						view.highlightCell(b.getCell());
						board.moveFigureFromTo(bPrev.getCell(), b.getCell());

						view.setDefaultPaint(bPrev.getCell());
						view.setDefaultPaint(b.getCell());
						bPrev = null;

						view.reset();
					} catch (GameException ex) {
						// ex.printStackTrace();
						view.setErrorLabel(ex.getMessage());
						view.setDefaultPaint(bPrev.getCell());
						view.setDefaultPaint(b.getCell());
						bPrev = null;
					}
				}
			}
		}

	}

	public void controllClick(BoardButton b) {
		if (board.whiteCount > 0 && board.blackCount > 0) {
			if (board.forcedAttack == false && b.cell == null) {
				b0 = null;
			} else {
				if ((board.loopAttack == true && b.equal(bPrev)
						&& board.forcedAttack == false && b0 == null)
						|| (board.forcedAttack == false && b0 == null && board.loopAttack == false)) {

					if (board.getFigure(b.cell) != null) {
						ArrayList<Cell> list = board
								.getFigurePosibleAttack(board.getWhiteOnTurn());
						if (list.size() != 0) {
							for (int i = 0; i < list.size(); i++) {
								if (list.get(i).getRow() == b.cell.getRow()
										&& list.get(i).getColumn() == b.cell
												.getColumn()) {
									board.forcedAttack = true;
									b0 = b;
									view.highlightCell(b0.cell);
								}
							}
							if (b0 == null) {
								b0 = null;
								view.setErrorLabel("Van kötelezõ ütés!");
							}
						} else {
							b0 = b;
							view.highlightCell(b0.cell);
						}

					}
				} else {
					if (board.forcedAttack == true) {
						ArrayList<Cell> destCell = board
								.getCellPossibleAttack(b0.cell);
						for (int i = 0; i < destCell.size(); i++) {
							if (destCell.get(i).getRow() == b.cell.getRow()
									&& destCell.get(i).getColumn() == b.cell
											.getColumn()) {
								b1 = b;
								board.forcedAttack = false;
							}
						}
						if (b1 == null) {
							view.setErrorLabel("Van kötelezõ ütés!");
						}

					}
					if (board.forcedAttack == false) {
						b1 = b;
						view.setDefaultPaint(b0.cell);
						int result = board.move(b0.cell, b1.cell);
						if (result == 0) {
							view.setErrorLabel("Érvénytelen lépés!");
						}

						else {
							view.refresh(b0.cell);
							view.refresh(b1.cell);
							if (result == MoveResult.MR_IFATTACKING) {
								view.refresh(board.getMidCell(b0.cell, b1.cell));
								if (board.possibleAttack(b1.cell) == true) {
									board.loopAttack = true;
									bPrev = b1;
								} else {
									board.loopAttack = false;
									bPrev = null;
								}
							}
							if (board.loopAttack == false) {
								board.setWhiteOnTurn(!board.getWhiteOnTurn());
							}
							view.refreshColorLabel();
						}
						b0 = b1 = null;
						view.delErrorLabel();
					}
				}
			}
		} else {
			if (board.whiteCount == 0) {
				view.setWinnerLabel(false);
			}
			if (board.blackCount == 0) {
				view.setWinnerLabel(true);
			}
		}

	}
}
