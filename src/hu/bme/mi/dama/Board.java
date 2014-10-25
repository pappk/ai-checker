package hu.bme.mi.dama;

import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.ErrorCode.ErrorCodes;

import java.io.Console;
import java.util.ArrayList;

public class Board implements java.io.Serializable {
	final protected int dimension = 8;
	protected Figure[][] figureArray = new Figure[dimension][dimension];
	protected int whiteCount;
	protected int blackCount;
	protected boolean whiteOnTurn = true;
	boolean forcedAttack;
	boolean loopAttack;
	private int autoIncKey;
	private Figure prevFigure;

	public boolean getWhiteOnTurn() {
		return whiteOnTurn;
	}

	public String getWhiteOnTurnLabel() {
		if (whiteOnTurn == true)
			return "Világos következik!";
		else
			return "Sötét következik!";
	}

	public void setWhiteOnTurn(boolean a) {
		whiteOnTurn = a;
	}

	public Board() {
		reset();
	}

	public void clearBoard() {
		forcedAttack = false;
		loopAttack = false;
		whiteOnTurn = true;
		whiteCount = 0;
		blackCount = 0;
		autoIncKey = 0;
		prevFigure = null;

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				figureArray[i][j] = null;
			}
		}
	}

	public void reset() {
		clearBoard();
		/*
		 * Szabályos elhelyzése a bábuknak a játék elején
		 */
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 8; j++) {
				if ((j + i) % 2 != 0) {
					figureArray[i][j] = new Figure(getAutoIncKey(), false);
					blackCount++;
					figureArray[7 - i][7 - j] = new Figure(getAutoIncKey(),
							true);
					whiteCount++;
				}
			}
		}

	}

	/**
	 * Elvégzi a lépés mûveletét [from] celláról a [to] cellába.
	 * 
	 * @param from
	 * @param to
	 * @throws GameException
	 */
	// TODO: GameException dobása
	public void moveFigureFromTo(Cell from, Cell to) throws GameException {
		System.out.println("from: " + from);
		System.out.println("to: " + to);

		if (from.getColumn() < dimension && from.getRow() < dimension
				&& to.getColumn() < dimension && to.getRow() < dimension) {
			// Érényes játéktér
			Figure fromFigure = getFigure(from);
			if (fromFigure != null && fromFigure.getColor() == getWhiteOnTurn()) {

				// Új lépés
				ArrayList<Cell> possibleAttackCells = getCellPossibleAttack(from);
				if (possibleAttackCells.size() > 0) {
					// Van kötelezõ ütés a kiválasztott mezõrõl
					if (possibleAttackCells.contains(to)) {
						// Valamelyik kötelezõ ütési hely a kiválasztott mezõ

						if (loopAttack) {
							// Ütéssorozat folytatása
							if (!getFigure(from).equals(prevFigure)) {
								throw new GameException(
										"A lépéssorozatot megkezdõ bábúval kell folytatni a lépést",
										getWhiteOnTurn(), ErrorCodes.LOOPATTACK);
							}
						}

						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK && moveResult != MoveResult.MR_IFATTACKING) {
							throw new GameException("Rossz lépés",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							if (possibleAttack(to)) {
								// Ha további ütés lehetséges a [to] mezõrõl,
								// akkor ütéssorozatot kell a következõ lépésben
								loopAttack = true;
								prevFigure = getFigure(to);
							} else {
								// A játékos köre befejezõdött
								changePlayer();
							}

						}
					} else {
						// Van kötelezõ ütés, azt kell választani
						throw new GameException(
								"Van kötelezõ ütés ezzel a bábúval",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					}

				} else {
					// Nincs kötelezõ ütési lehetõség a kiválasztott mezõrõl
					ArrayList<Cell> possibleAttackCellsOther = getFigurePosibleAttack(getWhiteOnTurn());
					if (possibleAttackCellsOther.size() > 0) {
						// Más bábuval van kötelezõ ütés
						throw new GameException(
								"Van kötelezõ ütés egy másik bábúval",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					} else {
						// Új lépés, ami nem ütés
						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK && moveResult != MoveResult.MR_IFNOTATTACKING) {
							throw new GameException("Rossz lépés",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							// A játékos köre befejezõdött
							changePlayer();
						}
					}
				}

			} else {
				// Nincs bábuja a játékosnak a kiinduló mezõn
			}
		} else {
			// Érvénytelen játéktér
		}
	}

	public Figure getFigure(Cell a) {
		return figureArray[a.getRow()][a.getColumn()];
	}

	public void removeFigure(Cell a) {
		if (getFigure(a).color == true)
			whiteCount--;
		else
			blackCount--;
		figureArray[a.getRow()][a.getColumn()] = null;
	}

	public Cell getMidCell(Cell c0, Cell c1) {
		/*
		 * tudjuk biztosan, hogy osztható 2-vel mert csak akkor kapja ezt a
		 * MoveResult értéket, a átlóban 2 cellát akar lépni
		 */
		int dRow = (c1.getRow() - c0.getRow()) / 2;
		int dColumn = (c1.getColumn() - c0.getColumn()) / 2;
		return new Cell(c0.getRow() + (dRow), c0.getColumn() + dColumn);
	}

	public int move(Cell c0, Cell c1) {
		Figure srcFigure = getFigure(c0);
		Figure destFigure = getFigure(c1);
		Figure midFigure;
		Cell midCell;

		if (srcFigure == null || srcFigure.color != whiteOnTurn)
			return MoveResult.MR_BAD;

		int res = srcFigure.possibleMove(c0, c1);

		if (res == MoveResult.MR_BAD) {
			return MoveResult.MR_BAD;
		}

		if (res == MoveResult.MR_IFATTACKING) {

			midCell = getMidCell(c0, c1);
			midFigure = getFigure(midCell);

			if (destFigure == null && midFigure != null
					&& midFigure.color != srcFigure.color) {
				removeFigure(midCell);
			} else
				return MoveResult.MR_BAD;
		}
		if (res == MoveResult.MR_IFNOTATTACKING && destFigure == null) {
			res = MoveResult.MR_OK;
		}
		if (destFigure != null)
			return MoveResult.MR_BAD;

		if (res != MoveResult.MR_OK && res != MoveResult.MR_IFATTACKING)
			return MoveResult.MR_BAD;

		// ha világos és elérte a pálya végét
		if ((c1.getRow() == 0 && srcFigure.color == true)
		// ha sötét és elérte a pálya végét
				|| (c1.getRow() == dimension - 1 && srcFigure.color == false)) {
			figureArray[c1.getRow()][c1.getColumn()] = new Checker(
					srcFigure.getId(), srcFigure.color);
		} else {
			figureArray[c1.getRow()][c1.getColumn()] = srcFigure;
		}
		figureArray[c0.getRow()][c0.getColumn()] = null;
		return res;
	}

	/**
	 * Visszatér egy lsitával a lehetséges ütési lehetõségekkel egy adott
	 * celláról
	 * 
	 * @param a
	 *            Forrás cella
	 * @return Cellákat tartalmazó lista
	 */
	public ArrayList<Cell> getCellPossibleAttack(Cell a) {
		ArrayList<Cell> returnList = new ArrayList<Cell>();
		if (getFigure(a) != null) {
			boolean aColor = getFigure(a).color;
			Cell destCell = null;
			Cell midCell = null;
			ArrayList<Cell> list = new ArrayList<Cell>();
			if (a.getRow() > 1) {
				if (a.getColumn() > 1) {
					destCell = new Cell(a.getRow() - 2, a.getColumn() - 2);
					list.add(destCell);
				}
				if (a.getColumn() < 6) {
					destCell = new Cell(a.getRow() - 2, a.getColumn() + 2);
					list.add(destCell);
				}
			}
			if (a.getRow() < 6) {
				if (a.getColumn() > 1) {
					destCell = new Cell(a.getRow() + 2, a.getColumn() - 2);
					list.add(destCell);
				}
				if (a.getColumn() < 6) {
					destCell = new Cell(a.getRow() + 2, a.getColumn() + 2);
					list.add(destCell);
				}
			}
			for (int i = 0; i < list.size(); i++) {
				midCell = getMidCell(a, list.get(i));
				if (getFigure(list.get(i)) == null
						&& getFigure(midCell) != null
						&& getFigure(midCell).color != aColor
						&& getFigure(a).possibleMove(a, list.get(i)) == MoveResult.MR_IFATTACKING) {
					returnList.add(list.get(i));
				}
			}
			return returnList;
		} else {
			returnList.clear();
			return returnList;
		}
	}

	public boolean possibleAttack(Cell a) {
		if (getCellPossibleAttack(a).size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	// a paraméter meghatározza melyik színû játékos bábujai között keresünk
	public ArrayList<Cell> getFigurePosibleAttack(boolean aColor) {
		ArrayList<Cell> attackArray = new ArrayList<Cell>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (figureArray[i][j] != null
						&& figureArray[i][j].color == aColor) {
					Cell aCell = new Cell(i, j);
					if (possibleAttack(aCell) == true) {
						attackArray.add(aCell);
					}
				}
			}
		}
		return attackArray;
	}

	/**
	 * Generál egy egyedi azonosítószámot
	 * 
	 * @return az új azonosító
	 */
	public int getAutoIncKey() {
		int returnKey = autoIncKey;
		autoIncKey++;
		return returnKey;
	}

	public void changePlayer() {
		setWhiteOnTurn(!whiteOnTurn);
		prevFigure = null;
		forcedAttack = false;
		loopAttack = false;
	}

	/*
	 * TESZTEKHEZ SZÜKSÉGES METÓDUSOK
	 */
	public void putFigure(Cell aCell, boolean aColor, boolean checker) {
		if (aCell != null && aCell.getRow() >= 0 && aCell.getRow() < dimension
				&& aCell.getColumn() >= 0 && aCell.getColumn() < dimension) {
			if (checker == false) {
				figureArray[aCell.getRow()][aCell.getColumn()] = new Figure(
						getAutoIncKey(), aColor);
			} else {
				figureArray[aCell.getRow()][aCell.getColumn()] = new Checker(
						getAutoIncKey(), aColor);
			}
		}
	}

	public void draw() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (figureArray[i][j] != null)
					System.out.print(figureArray[i][j] + " ");
				else {
					if ((i + j) % 2 == 0)
						System.out.print("_ ");
					else
						System.out.print(". ");
				}
			}
			System.out.print(i + "\n");
		}
		for (int j = 0; j < 8; j++)
			System.out.print(j + " ");
	}
}
