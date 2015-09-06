package hu.bme.mi.dama;

import hu.bme.mi.utils.ErrorCode.ErrorCodes;
import hu.bme.mi.utils.GameEvent.GameEvents;
import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.Initiater;

import java.util.ArrayList;

public class Board implements java.io.Serializable {
	final public int dimension = 8;
	protected Figure[][] figureArray = new Figure[dimension][dimension];
	protected boolean whiteOnTurn = true;
	boolean forcedAttack;
	boolean loopAttack;
	private int autoIncKey;
	private Figure prevFigure;
	private Cell prevCell;
	private Initiater initiater;
	// az MI játékos színe
	private boolean aiPlayerColor;

	public Board(Initiater initiater, boolean aiPlayerColor) {
		this.initiater = initiater;
		this.aiPlayerColor = aiPlayerColor;

		reset();
		start();
	}

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

	public boolean getLoopAttack() {
		return loopAttack;
	}
	
	public Cell getPrevCell(){
		return prevCell;
	}

	public void clearBoard() {
		forcedAttack = false;
		loopAttack = false;
		whiteOnTurn = true;
		autoIncKey = 0;
		prevFigure = null;
		prevCell = null;

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
					figureArray[7 - i][7 - j] = new Figure(getAutoIncKey(),
							true);
				}
			}
		}
	}

	/**
	 * Ha az AI játékos kezd, akkor értesíti a kezdésről
	 */
	public void start() {
		if (initiater != null && whiteOnTurn == aiPlayerColor) {
			initiater.yourTurn(aiPlayerColor);
		}
	}

	/**
	 * Elvégzi a lépés műveletét [from] celláról a [to] cellába.
	 * 
	 * @param from
	 * @param to
	 * @throws GameException
	 */
	public GameEvents moveFigureFromTo(Cell from, Cell to) throws GameException {
		/*
		 * System.out.println("from: " + from); System.out.println("to: " + to);
		 */

		GameEvents gameStatus = GameEvents.KEEPGOING;

		if (from.getColumn() < dimension && from.getRow() < dimension
				&& to.getColumn() < dimension && to.getRow() < dimension) {
			// Érényes játéktér
			Figure fromFigure = getFigure(from);
			if (fromFigure != null && fromFigure.getColor() == getWhiteOnTurn()) {

				// Új lépés
				ArrayList<Cell> possibleAttackCells = getCellPossibleAttack(from);
				if (possibleAttackCells.size() > 0) {
					// Van kötelező ütés a kiválasztott mezőről
					if (possibleAttackCells.contains(to)) {
						// Valamelyik kötelező ütési hely a kiválasztott mező

						if (loopAttack) {
							// Ütéssorozat folytatása
							if (!getFigure(from).equals(prevFigure)) {
								throw new GameException(
										"A lépéssorozatot megkezdő bábúval kell folytatni a lépést",
										getWhiteOnTurn(), ErrorCodes.LOOPATTACK);
							}
						}

						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK
								&& moveResult != MoveResult.MR_IFATTACKING) {
							throw new GameException("Rossz lépés",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							if (possibleAttack(to)) {
								// Ha további ütés lehetséges a [to] mezőről,
								// akkor ütéssorozatot kell a következő lépésben
								loopAttack = true;
								prevFigure = getFigure(to);
								prevCell = to;
							} else {
								// A játékos köre befejeződött
								gameStatus = changePlayer();
							}

						}
					} else {
						// Van kötelező ütés, azt kell választani
						throw new GameException(
								"Van kötelező ütés ezzel a bábúval",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					}

				} else {
					// Nincs kötelező ütési lehetőség a kiválasztott mezőről
					ArrayList<Cell> possibleAttackCellsOther = getFigurePossibleAttack(getWhiteOnTurn());
					if (possibleAttackCellsOther.size() > 0) {
						// Más bábuval van kötelező ütés
						throw new GameException(
								"Van kötelező ütés egy másik bábúval",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					} else {
						// Új lépés, ami nem ütés
						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK
								&& moveResult != MoveResult.MR_IFNOTATTACKING) {
							throw new GameException("Rossz lépés",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							// A játékos köre befejeződött
							gameStatus = changePlayer();
						}
					}
				}

			} else {
				// Nincs bábuja a játékosnak a kiinduló mezőn
			}
		} else {
			// Érvénytelen játéktér
		}

		// MI játékos értesítése ütéssorozat esetén
		/*
		 * if (initiater != null && loopAttack && whiteOnTurn == miPlayerColor)
		 * { initiater.yourTurn(); }
		 */
		/*
		 * if (loopAttack && whiteOnTurn == miPlayerColor) {
		 * initiater.yourTurn(); }
		 */

		return gameStatus;
	}

	public Figure getFigure(Cell a) {
		return figureArray[a.getRow()][a.getColumn()];
	}

	public void removeFigure(Cell a) {
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

	protected int move(Cell c0, Cell c1) {
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
	 * Visszatér egy listával a lehetséges ütési lehetőségekkel egy adott
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

	/**
	 * Visszatér egy listával a lehetséges lépési lehetőségekkel egy adott
	 * celláról
	 * 
	 * @param a
	 *            Forrás cella
	 * @return Cellákat tartalmazó lista
	 */
	public ArrayList<Cell> getCellPossibleMove(Cell a) {
		ArrayList<Cell> returnList = new ArrayList<Cell>();
		if (getFigure(a) != null) {
			Cell destCell = null;
			ArrayList<Cell> list = new ArrayList<Cell>();
			if (a.getRow() > 0) {
				if (a.getColumn() > 0) {
					destCell = new Cell(a.getRow() - 1, a.getColumn() - 1);
					list.add(destCell);
				}
				if (a.getColumn() < dimension - 1) {
					destCell = new Cell(a.getRow() - 1, a.getColumn() + 1);
					list.add(destCell);
				}
			}
			if (a.getRow() < dimension - 1) {
				if (a.getColumn() > 0) {
					destCell = new Cell(a.getRow() + 1, a.getColumn() - 1);
					list.add(destCell);
				}
				if (a.getColumn() < dimension - 1) {
					destCell = new Cell(a.getRow() + 1, a.getColumn() + 1);
					list.add(destCell);
				}
			}
			for (int i = 0; i < list.size(); i++) {
				if (getFigure(list.get(i)) == null
						&& getFigure(a).possibleMove(a, list.get(i)) == MoveResult.MR_IFNOTATTACKING) {
					returnList.add(list.get(i));
				}
			}
			return returnList;
		} else {
			returnList.clear();
			return returnList;
		}
	}

	/**
	 * Visszatér van-e kötelező ütések a forrás celláról
	 * 
	 * @param a
	 *            forrás cella
	 * @return boolean
	 */
	public boolean possibleAttack(Cell a) {
		if (getCellPossibleAttack(a).size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Visszatér van-e lehetséges lépés a forrás celláról
	 * 
	 * @param a
	 *            forrás cella
	 * @return boolean
	 */
	public boolean possibleMove(Cell a) {
		if (getCellPossibleMove(a).size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	// a paraméter meghatározza melyik színű játékos bábujai között keresünk
	/**
	 * Azoknak a bábuknak a listájával tér vissza, amiknek kötelező ütése van
	 * 
	 * @param aColor
	 *            A játékos színét meghatározó paraméter
	 * @return lista
	 */
	public ArrayList<Cell> getFigurePossibleAttack(boolean aColor) {
		ArrayList<Cell> attackArray = new ArrayList<Cell>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
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
	 * Azoknak a bábuknak a listájával tér vissza, amik szabadon léphetnek Nem
	 * tartalmazza azokat a figurákat, amiknek kötelező ütése van
	 * 
	 * @param aColor
	 *            A játékos színét meghatározó paraméter
	 * @return lista
	 */
	public ArrayList<Cell> getFigurePossibleMove(boolean aColor) {
		ArrayList<Cell> moveArray = new ArrayList<Cell>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (figureArray[i][j] != null
						&& figureArray[i][j].color == aColor) {
					Cell aCell = new Cell(i, j);
					if (possibleAttack(aCell) == false
							&& possibleMove(aCell) == true) {
						moveArray.add(aCell);
					}
				}
			}
		}
		return moveArray;
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

	/**
	 * Megmondja, hogy egy adott játékos tud-e lépni vagy ütni az aktuális
	 * helyzetben
	 * 
	 * @param aColor
	 *            Játékos színét meghatározó paraméter
	 * @return boolean
	 */
	public boolean canPlayerMove(boolean aColor) {
		if (getFigurePossibleAttack(aColor).size() > 0
				|| getFigurePossibleMove(aColor).size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Megvizsgálja, hogy nem ért-e véget a játék ha nem, akkor a következő
	 * játékosra helyezi sort ha MI játékoson van sor, akkor értesíti
	 * 
	 * @return
	 */
	public GameEvents changePlayer() {
		setWhiteOnTurn(!whiteOnTurn);
		prevFigure = null;
		forcedAttack = false;
		loopAttack = false;

		int whiteCount = getFigureCount(true);
		int blackCount = getFigureCount(false);

		/*
		 * System.out.println("fehér: "+whiteCount);
		 * System.out.println("fekete: "+blackCount);
		 */

		// Feltétel vizsgálat, hogy tart-e még a játék
		if (whiteCount > 0 && blackCount > 0 && canPlayerMove(whiteOnTurn)) {

			// MI játékos értesítése
			/*
			 * if (initiater != null && whiteOnTurn == miPlayerColor) {
			 * initiater.yourTurn(); }
			 */

			// Nem ért még véget a játék
			return GameEvents.KEEPGOING;
		} else {
			// A játék végetért
			if (whiteCount == 0 && blackCount > 0) {
				// Fehér bábujai elfogytak, nyertes: fekete
				return GameEvents.WINNERBLACK;
			}
			if (whiteCount > 0 && blackCount == 0) {
				// Fekete bábujai elfogytak, nyertes: fehér
				return GameEvents.WINNERWHITE;
			}
			if (!canPlayerMove(whiteOnTurn)) {
				if(whiteOnTurn){
					// Fehér már nem tud lépni, nyertes: fekete
					return GameEvents.WINNERBLACK;
				} else {
					// Fekete már nem tud lépni, nyertes: fehér
					return GameEvents.WINNERWHITE;
				}
			}
		}

		return null;
	}

	/**
	 * Vissaztér a pálya másolatával
	 * 
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public Board getBoardClone() throws CloneNotSupportedException {
		// Másolat készítése a tábláról úgy, hogy ne érje el az értesítés küldő
		// interfészt
		Board newBoard = new Board(null, aiPlayerColor);
		newBoard.autoIncKey = autoIncKey;
		// newBoard.figureArray = figureArray.clone();
		newBoard.forcedAttack = forcedAttack;
		newBoard.loopAttack = loopAttack;
		newBoard.prevFigure = prevFigure;
		newBoard.prevCell = prevCell;
		newBoard.whiteOnTurn = whiteOnTurn;
		newBoard.figureArray = new Figure[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (figureArray[i][j] != null) {
					newBoard.figureArray[i][j] = (Figure) figureArray[i][j]
							.clone();
				}
			}
		}

		return newBoard;
	}

	/**
	 * Alaphelyzetbe állítja a környezeti változókat. Támadás számításkor
	 * problémát okozhat, ha a klónozott értékkel számol tovább a program.
	 */
	public void resetStatusVariables() {
		forcedAttack = false;
		loopAttack = false;
		prevFigure = null;
		prevCell = null;
	}

	/**
	 * Visszatér egy játékos összes figurájának pozíciójával
	 * 
	 * @param aColor
	 * @return
	 */
	public ArrayList<Cell> getAllFigureCellsPlayer(boolean aColor) {
		ArrayList<Cell> figureCells = new ArrayList<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (figureArray[i][j] != null
						&& figureArray[i][j].color == aColor) {
					figureCells.add(new Cell(i, j));
				}
			}
		}
		return figureCells;
	}

	/**
	 * Meghatározza, hogy adott cella értelmezhető-e a játéktérben
	 * 
	 * @param cell
	 * @return
	 */
	public boolean isCellValid(Cell cell) {
		if (cell.getRow() >= 0 && cell.getRow() < dimension
				&& cell.getColumn() >= 0 && cell.getColumn() < dimension) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Meghatározza, hogy addot celláról addott cellára lehetséges-e az ütes
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean possibleAttackFromTo(Cell from, Cell to) {
		ArrayList<Cell> attackCells = getCellPossibleAttack(from);
		if (attackCells.contains(to)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Meghatározza, hogy az adott cellán álló bábú támadás alatt áll-e
	 * 
	 * @param cell
	 * @return
	 */
	public ArrayList<Cell> isFigureAttacked(Cell cell) {
		Figure figure = getFigure(cell);
		ArrayList<Cell> attackCells = new ArrayList<>();
		if (figure != null) {
			// Szélrózsa alapú elnevezés
			Cell NW = new Cell(cell.getRow() - 1, cell.getColumn() - 1);
			Cell NE = new Cell(cell.getRow() - 1, cell.getColumn() + 1);
			Cell SW = new Cell(cell.getRow() + 1, cell.getColumn() - 1);
			Cell SE = new Cell(cell.getRow() + 1, cell.getColumn() + 1);

			if (isCellValid(NW) && isCellValid(SE)) {
				if (possibleAttackFromTo(NW, SE)) {
					attackCells.add(NW);
				}
				if (possibleAttackFromTo(SE, NW)) {
					attackCells.add(SE);
				}
			}
			if (isCellValid(NE) && isCellValid(SW)) {
				if (possibleAttackFromTo(NE, SW)) {
					attackCells.add(NE);
				}
				if (possibleAttackFromTo(SW, NE)) {
					attackCells.add(SW);
				}
			}
		}

		return attackCells;
	}

	/*
	 * Analizáló függvények adott játékos szemszögéből vizsgálja meg a tábla
	 * aktuális állapotát
	 */

	/**
	 * Megadja egy játékos figuráinak számát
	 * 
	 * @param color
	 * @return
	 */
	public int getFigureCount(boolean color) {
		int figureCount = 0;
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				Figure figure = getFigure(new Cell(i, j));
				if (figure != null && figure.getColor() == color) {
					figureCount++;
				}
			}
		}
		return figureCount;

	}

	/**
	 * Visszatér egy játékos összes megtámadott bábujának pozíciójával
	 * 
	 * @param color
	 * @return
	 */
	public ArrayList<Cell> getAttackedFigureCells(boolean color) {
		ArrayList<Cell> figures = getAllFigureCellsPlayer(color);
		ArrayList<Cell> attackedFigures = new ArrayList<>();
		for (Cell cell : figures) {
			if (isFigureAttacked(cell).size() > 0) {
				attackedFigures.add(cell);
			}
		}
		return attackedFigures;
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
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
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
		for (int j = 0; j < dimension; j++)
			System.out.print(j + " ");
	}
}