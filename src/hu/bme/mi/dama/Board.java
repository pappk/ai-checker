package hu.bme.mi.dama;

import hu.bme.mi.utils.GameEvent;
import hu.bme.mi.utils.GameEvent.GameEvents;
import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.Initiater;
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
	private Initiater initiater;

	public boolean getWhiteOnTurn() {
		return whiteOnTurn;
	}

	public String getWhiteOnTurnLabel() {
		if (whiteOnTurn == true)
			return "Vil�gos k�vetkezik!";
		else
			return "S�t�t k�vetkezik!";
	}

	public void setWhiteOnTurn(boolean a) {
		whiteOnTurn = a;
	}

	public Board(Initiater initiater) {
		this.initiater = initiater;
		
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
		 * Szab�lyos elhelyz�se a b�buknak a j�t�k elej�n
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
	 * Elv�gzi a l�p�s m�velet�t [from] cell�r�l a [to] cell�ba.
	 * 
	 * @param from
	 * @param to
	 * @throws GameException
	 */
	// TODO: GameException dob�sa
	public GameEvents moveFigureFromTo(Cell from, Cell to) throws GameException {
		System.out.println("from: " + from);
		System.out.println("to: " + to);
		
		GameEvents gameStatus = GameEvents.KEEPGOING;

		if (from.getColumn() < dimension && from.getRow() < dimension
				&& to.getColumn() < dimension && to.getRow() < dimension) {
			// �r�nyes j�t�kt�r
			Figure fromFigure = getFigure(from);
			if (fromFigure != null && fromFigure.getColor() == getWhiteOnTurn()) {

				// �j l�p�s
				ArrayList<Cell> possibleAttackCells = getCellPossibleAttack(from);
				if (possibleAttackCells.size() > 0) {
					// Van k�telez� �t�s a kiv�lasztott mez�r�l
					if (possibleAttackCells.contains(to)) {
						// Valamelyik k�telez� �t�si hely a kiv�lasztott mez�

						if (loopAttack) {
							// �t�ssorozat folytat�sa
							if (!getFigure(from).equals(prevFigure)) {
								throw new GameException(
										"A l�p�ssorozatot megkezd� b�b�val kell folytatni a l�p�st",
										getWhiteOnTurn(), ErrorCodes.LOOPATTACK);
							}
						}

						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK
								&& moveResult != MoveResult.MR_IFATTACKING) {
							throw new GameException("Rossz l�p�s",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							if (possibleAttack(to)) {
								// Ha tov�bbi �t�s lehets�ges a [to] mez�r�l,
								// akkor �t�ssorozatot kell a k�vetkez� l�p�sben
								loopAttack = true;
								prevFigure = getFigure(to);
							} else {
								// A j�t�kos k�re befejez�d�tt
								gameStatus = changePlayer();
							}

						}
					} else {
						// Van k�telez� �t�s, azt kell v�lasztani
						throw new GameException(
								"Van k�telez� �t�s ezzel a b�b�val",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					}

				} else {
					// Nincs k�telez� �t�si lehet�s�g a kiv�lasztott mez�r�l
					ArrayList<Cell> possibleAttackCellsOther = getFigurePossibleAttack(getWhiteOnTurn());
					if (possibleAttackCellsOther.size() > 0) {
						// M�s b�buval van k�telez� �t�s
						throw new GameException(
								"Van k�telez� �t�s egy m�sik b�b�val",
								getWhiteOnTurn(), ErrorCodes.FORCEDATTACK);
					} else {
						// �j l�p�s, ami nem �t�s
						int moveResult = move(from, to);
						if (moveResult != MoveResult.MR_OK
								&& moveResult != MoveResult.MR_IFNOTATTACKING) {
							throw new GameException("Rossz l�p�s",
									getWhiteOnTurn(),
									ErrorCodes.INVALIDMOVEMENT);
						} else {
							// A j�t�kos k�re befejez�d�tt
							gameStatus = changePlayer();
						}
					}
				}

			} else {
				// Nincs b�buja a j�t�kosnak a kiindul� mez�n
			}
		} else {
			// �rv�nytelen j�t�kt�r
		}
		
		return gameStatus;
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
		 * tudjuk biztosan, hogy oszthat� 2-vel mert csak akkor kapja ezt a
		 * MoveResult �rt�ket, a �tl�ban 2 cell�t akar l�pni
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

		// ha vil�gos �s el�rte a p�lya v�g�t
		if ((c1.getRow() == 0 && srcFigure.color == true)
		// ha s�t�t �s el�rte a p�lya v�g�t
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
	 * Visszat�r egy list�val a lehets�ges �t�si lehet�s�gekkel egy adott
	 * cell�r�l
	 * 
	 * @param a
	 *            Forr�s cella
	 * @return Cell�kat tartalmaz� lista
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
	 * Visszat�r egy list�val a lehets�ges l�p�si lehet�s�gekkel egy adott
	 * cell�r�l
	 * 
	 * @param a
	 *            Forr�s cella
	 * @return Cell�kat tartalmaz� lista
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
				if (a.getColumn() < dimension-1) {
					destCell = new Cell(a.getRow() - 1, a.getColumn() + 1);
					list.add(destCell);
				}
			}
			if (a.getRow() < dimension-1) {
				if (a.getColumn() > 0) {
					destCell = new Cell(a.getRow() + 1, a.getColumn() - 1);
					list.add(destCell);
				}
				if (a.getColumn() < dimension-1) {
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
	 * Visszat�r van-e k�telez� �t�sek a forr�s cell�r�l
	 * @param a	forr�s cella
	 * @return	boolean
	 */
	public boolean possibleAttack(Cell a) {
		if (getCellPossibleAttack(a).size() != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Visszat�r van-e lehets�ges l�p�s a forr�s cell�r�l
	 * @param a	forr�s cella
	 * @return	boolean
	 */
	public boolean possibleMove(Cell a) {
		if (getCellPossibleMove(a).size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	// a param�ter meghat�rozza melyik sz�n� j�t�kos b�bujai k�z�tt keres�nk
	/**
	 * Azoknak a b�buknak a list�j�val t�r vissza, amiknek k�telez� �t�se van
	 * @param aColor	A j�t�kos sz�n�t meghat�roz� param�ter
	 * @return			lista
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
	 * Azoknak a b�buknak a list�j�val t�r vissza, amik szabadon l�phet
	 * @param aColor	A j�t�kos sz�n�t meghat�roz� param�ter
	 * @return			lista
	 */
	public ArrayList<Cell> getFigurePossibleMove(boolean aColor) {
		ArrayList<Cell> moveArray = new ArrayList<Cell>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (figureArray[i][j] != null
						&& figureArray[i][j].color == aColor) {
					Cell aCell = new Cell(i, j);
					if (possibleMove(aCell) == true) {
						moveArray.add(aCell);
					}
				}
			}
		}
		return moveArray;
	}

	/**
	 * Gener�l egy egyedi azonos�t�sz�mot
	 * 
	 * @return az �j azonos�t�
	 */
	public int getAutoIncKey() {
		int returnKey = autoIncKey;
		autoIncKey++;
		return returnKey;
	}
	
	/**
	 * Megmondja, hogy egy adott j�t�kos tud-e l�pni vagy �tni az aktu�lis helyzetben
	 * @param aColor	J�t�kos sz�n�t meghat�roz� param�ter
	 * @return boolean
	 */
	public boolean canPlayerMove(boolean aColor){
		if(getFigurePossibleAttack(whiteOnTurn).size() > 0
			|| getFigurePossibleMove(whiteOnTurn).size() > 0){
			return true;
		} else {
			return false;
		}
	}

	
	public GameEvents changePlayer() {
		setWhiteOnTurn(!whiteOnTurn);
		prevFigure = null;
		forcedAttack = false;
		loopAttack = false;
		
		//Felt�tel vizsg�lat, hogy tart-e m�g a j�t�k
		if(whiteCount > 0 && blackCount > 0 && canPlayerMove(whiteOnTurn)){
			
			if(whiteOnTurn == false){
				initiater.sayHello();
			}
			
			//Nem �rt m�g v�get a j�t�k
			return GameEvents.KEEPGOING;
		} else {
			//A j�t�k v�get�rt
			if(whiteCount == 0 && blackCount > 0){
				//Feh�r b�bujai elfogytak, nyertes: fekete
				return GameEvents.WINNERBLACK;
			}
			if(whiteCount > 0 && blackCount == 0){
				//Fekete b�bujai elfogytak, nyertes: feh�r
				return GameEvents.WINNERWHITE;
			}
			if(canPlayerMove(true) && !canPlayerMove(false)){
				//Feh�r m�g tud l�pni, fekete nm�r nem, nyertes: feh�r
				return GameEvents.WINNERWHITE;
			}
			if(!canPlayerMove(true) && canPlayerMove(false)){
				//Fekete m�g tud l�pni, feh�r nm�r nem, nyertes: fekete
				return GameEvents.WINNERBLACK;
			}
			if(!canPlayerMove(true) && !canPlayerMove(false)){
				//Egyik j�t�kos se tud m�r l�pni, nyertes: d�ntetlen
				return GameEvents.TIE;
			}
		}
		
		return null;
	}

	/*
	 * TESZTEKHEZ SZ�KS�GES MET�DUSOK
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
