package hu.bme.mi.agent;

import hu.bme.mi.dama.Board;
import hu.bme.mi.dama.ButtonController;
import hu.bme.mi.dama.Cell;
import hu.bme.mi.dama.Figure;
import hu.bme.mi.utils.AgentsTurnListener;
import hu.bme.mi.utils.Edge;
import hu.bme.mi.utils.GameEvent.GameEvents;
import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.Graph;
import hu.bme.mi.utils.Vertex;

import java.util.ArrayList;
import java.util.Random;

/*
 * FONTOS
 * Az {actual} változó pointer a tényleges táblára.
 * Nem szabad rajta módosítást végezni, kivéve a végleges lépést.
 * 
 */

public class Agent implements AgentsTurnListener {
	private Board actual;
	private boolean color;
	private ButtonController controller;
	private int nextId;

	/**
	 * Mesterséges intelligenciát reprezentáló osztály
	 * 
	 * @param board
	 *            A közös valóságalap a tábla aktuális állapotára mutató pointer
	 * @param color
	 *            Az MI játékos színe
	 */
	public Agent(Board board, boolean color) {
		this.actual = board;
		this.color = color;
	}

	public Agent(ButtonController controller, boolean color) {
		this.controller = controller;
		this.actual = controller.getBoard();
		this.color = color;
	}

	protected int getNextId() {
		return nextId++;
	}

	/**
	 * color színű játékos legoptimálisabb lépésvel visszatérő függény
	 * 
	 * @param color
	 *            Játékos színe
	 * @param board
	 *            aktuális játékállapot
	 * @return
	 */
	private Movement getNextMovement(boolean color, Board board) {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();
		try {
			Board workingBoard = board.getBoardClone();
			workingBoard.resetStatusVariables();

			ArrayList<Cell> possibleAttackCells = workingBoard
					.getFigurePossibleAttack(color);
			if (possibleAttackCells.size() > 0) {
				// Van kötelező ütés
				for (Cell cell : possibleAttackCells) {
					ArrayList<Cell> attackTargetCell = workingBoard
							.getCellPossibleAttack(cell);
					for (Cell cell2 : attackTargetCell) {
						possibleNextMoves.add(new Movement(cell, cell2,
								attackHeuristica(cell, cell2, workingBoard)));
					}
				}
			} else {
				// Szabad lépés
				ArrayList<Cell> possibleMoveCells = workingBoard
						.getFigurePossibleMove(color);
				for (Cell cell : possibleMoveCells) {
					ArrayList<Cell> moveTargetCell = workingBoard
							.getCellPossibleMove(cell);
					for (Cell cell2 : moveTargetCell) {
						possibleNextMoves.add(new Movement(cell, cell2,
								idleHeuristica(cell, cell2, workingBoard)));
					}
				}
			}

			workingBoard = null;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return getMaxHeuristic(possibleNextMoves);

	}

	/**
	 * Ütéssorozat esetén használandó függvény, visszaadja a legoptimálisabb
	 * ütést
	 * 
	 * @param from
	 * @param board
	 * @return Optimális lépés
	 */
	private Movement getNextLoopAttackMovement(Cell from, Board board) {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();
		Board workingBoard = null;
		try {
			workingBoard = board.getBoardClone();

			ArrayList<Cell> attackTargetCell = workingBoard
					.getCellPossibleAttack(from);
			for (Cell cell : attackTargetCell) {
				possibleNextMoves.add(new Movement(from, cell,
						attackHeuristica(from, cell, workingBoard)));
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		workingBoard = null;
		
		if (possibleNextMoves.size() > 1) {
			return getMaxHeuristic(possibleNextMoves);
		} else {
			return possibleNextMoves.get(0);
		}
	}

	/**
	 * Következő lépéseket gráfba rendezi. maxLevel paraméter mélységű gráfot
	 * állít elő.
	 * 
	 * @param color
	 * @param board
	 * @param graph
	 *            Graph pointer, paramétereit változtatja a függvény
	 * @param previousVertex
	 * @param maxLevel
	 *            gráf mélységének beállítása
	 * @param actualLevel
	 */
	private void getNextMovementGraph(boolean color, Board board, Graph graph,
			Vertex previousVertex, int maxLevel, int actualLevel) {
		// DEBUG: Lépéslehetőségek kiiratása
		String align = "";
		for (int i = 0; i < actualLevel; i++) {
			align += "-";
		}

		ArrayList<Movement> possibleNextMoves = new ArrayList<>();
		try {
			Board workingBoard = board.getBoardClone();
			workingBoard.resetStatusVariables();

			ArrayList<Cell> possibleAttackCells = workingBoard
					.getFigurePossibleAttack(color);
			if (possibleAttackCells.size() > 0) {
				// Van kötelező ütés
				for (Cell cell : possibleAttackCells) {
					ArrayList<Cell> attackTargetCell = workingBoard
							.getCellPossibleAttack(cell);
					for (Cell cell2 : attackTargetCell) {
						possibleNextMoves.add(new Movement(cell, cell2,
								attackHeuristica(cell, cell2, workingBoard)));
					}
				}
			} else {
				// Szabad lépés
				ArrayList<Cell> possibleMoveCells = workingBoard
						.getFigurePossibleMove(color);
				for (Cell cell : possibleMoveCells) {
					ArrayList<Cell> moveTargetCell = workingBoard
							.getCellPossibleMove(cell);
					for (Cell cell2 : moveTargetCell) {
						possibleNextMoves.add(new Movement(cell, cell2,
								idleHeuristica(cell, cell2, workingBoard)));
					}
				}
			}

			for (Movement movement : possibleNextMoves) {
				Board paralelWorkingCopy = workingBoard.getBoardClone();
				paralelWorkingCopy.moveFigureFromTo(movement.getFrom(),
						movement.getTo());
				Vertex nextVertex = new Vertex(getNextId(), paralelWorkingCopy);
				Edge edge = new Edge(getNextId(), previousVertex, nextVertex,
						movement.getH(), color, movement);
				graph.insertNode(nextVertex, edge);

				// DEBUG: Lépéslehetőségek kiiratása
				System.out.println(align + edge);

				if (actualLevel < maxLevel) {
					Movement prevMovement = movement;
					while (paralelWorkingCopy.getLoopAttack()) {
						Movement loopMovement = getNextLoopAttackMovement(
								prevMovement.getTo(), paralelWorkingCopy);

						paralelWorkingCopy.moveFigureFromTo(
								loopMovement.getFrom(), loopMovement.getTo());
						prevMovement = loopMovement;
					}

					getNextMovementGraph(!color, paralelWorkingCopy, graph,
							nextVertex, maxLevel, actualLevel + 1);
				}

				paralelWorkingCopy = null;
			}

			workingBoard = null;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * MI játékos lépése kiszámolja a lehetséges lépések heurisztikáját és
	 * kiválasztja a legjobbat, majd elvégi a lépést a táblán
	 */
	private void nextMove(boolean color) {
		Movement maxHeuristicMovement = null;
		if (!actual.getLoopAttack()) {
			// Szabad lépés esetén
			Vertex startVertex = new Vertex(getNextId(), actual);
			Graph graph = new Graph(startVertex);
			getNextMovementGraph(color, actual, graph, startVertex, 3, 0);

			Movement nextMovement = graph.getSearchMaxMovement(
					graph.getStartVertex(), 0);

			System.out.println(nextMovement + " => " + nextMovement.moveChain);

			maxHeuristicMovement = nextMovement;
		} else {
			// Ütésorozat esetén
			maxHeuristicMovement = getNextLoopAttackMovement(
					actual.getPrecCell(), actual);
		}
		try {
			controller.handlePlayerMovement(maxHeuristicMovement.getFrom(),
					maxHeuristicMovement.getTo());
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Támadás esetén a paraméterként megadott lépés minőségét osztályozó
	 * függvény
	 * 
	 * @param from
	 * @param to
	 * @return heurisztika
	 */
	private double attackHeuristica(Cell from, Cell to, Board actual) {
		double h = 0;
		try {
			Board workingCopy = actual.getBoardClone();
			try {
				if (!workingCopy.getLoopAttack()) {
					h += 6 * isAttacked(workingCopy, from);
					h += 8 * isCheckerAttacked(workingCopy, from, to);
					h += 2 * canProtectOthers(workingCopy, from, to);
					workingCopy.moveFigureFromTo(from, to);
					if (!workingCopy.getLoopAttack()) {
						h += 2 * getDistanceFromEdge(workingCopy, to);
						Figure figure = workingCopy.getFigure(from);
						if (figure != null && figure.isChecker()) {
							h += getDistanceFromEnemy(workingCopy, from, to);
						}
						h += 10 * getAttackPossibility(workingCopy, to);
						h += 8 * willBeAttacked(workingCopy, to);
					}		
					if(willIWin(workingCopy)){
						h += 1000;
					}
					if(willILose(workingCopy)){
						h -= 1000;
					}
				} else {
					return h;
				}
			} catch (GameException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}
			workingCopy = null;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return h;
	}

	/**
	 * Szabad lépés esetén a paraméterként megadott lépés minőséégt osztályozó
	 * függvény
	 * 
	 * @param from
	 * @param to
	 * @return heurisztika
	 */
	private double idleHeuristica(Cell from, Cell to, Board actual) {
		double h = 0;
		try {
			Board workingCopy = actual.getBoardClone();
			Figure figure = workingCopy.getFigure(from);

			try {
				double isAttackedH = isAttacked(workingCopy, from);
				double getDistanceFromEdgeH = 0.0;
				double getDistanceFromEnemyH = 0.0;
				double willBeCheckerH = willBeChecker(workingCopy, from, to);
				double canProtectOthersH = canProtectOthers(workingCopy, from,
						to);

				h += 10 * canProtectOthersH;
				h += 15 * isAttackedH;
				h += 10 * willBeCheckerH;
				workingCopy.moveFigureFromTo(from, to);
				
				
				if (figure != null && figure.isChecker()) {
					getDistanceFromEnemyH = getDistanceFromEnemy(workingCopy,
							from, to);
					h += 10 * getDistanceFromEnemyH;
					getDistanceFromEdgeH = getDistanceFromEdge(workingCopy, to);
					h += getDistanceFromEdgeH;
				}

				double getAttackPossibilityH = getAttackPossibility(
						workingCopy, to);
				double willBeAttackedH = willBeAttacked(workingCopy, to);

				h += 12 * getAttackPossibilityH;
				h += 20 * willBeAttackedH;
				
				if(willIWin(workingCopy)){
					h += 1000;
				}
				if(willILose(workingCopy)){
					h -= 1000;
				}

			} catch (GameException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}

			workingCopy = null;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return h;
	}

	/**
	 * Lépések listájából kiválasztja a legnagyonn heurisztikával rendelkezőt
	 * 
	 * @param movementList
	 * @return
	 */
	private Movement getMaxHeuristic(ArrayList<Movement> movementList) {
		Movement returnMovemnet = null;
		Double maxHeuristic = null;
		ArrayList<Movement> bestMoves = new ArrayList<>();

		// Max heurisztika felderítése
		for (Movement movement : movementList) {
			if (maxHeuristic == null || movement.getH() > maxHeuristic) {
				returnMovemnet = movement;
				maxHeuristic = movement.getH();
			}
		}

		// Max heurisztikával rendelkező lépések kiválasztása
		if (maxHeuristic != null) {
			int movementkey = 0;

			for (Movement movement : movementList) {
				if (movement.getH() >= maxHeuristic) {
					bestMoves.add(movement);
				}
				movementkey++;
			}
		}

		Random randomselect = new Random();
		returnMovemnet = bestMoves.get(randomselect.nextInt(bestMoves.size()));
		return returnMovemnet;
	}

	@Override
	public void yourTurn(boolean color) {
		// TODO Auto-generated method stub
		System.out.println("agent jön");
		nextMove(color);
	}

	/*
	 * Monitorozó, osztályozó függvények
	 */
	/**
	 * Visszaadja a cella távolságát a tábla szélétől százalékosan
	 * 
	 * @param board
	 * @param cell
	 * @return [0,1] intervallumból tér vissza.
	 */
	protected Double getDistanceFromEdge(Board board, Cell cell) {
		Double d = null;

		int dist = Math.min(cell.getColumn(),
				(board.dimension - 1) - cell.getColumn());
		d = 1.0 - (double) dist / (double) (board.dimension - 1);

		return d;
	}

	/**
	 * Visszaadja az üthetési lehetőséget százalékosan. Ha létezik ütéssorozat,
	 * akkor 1 feletti értékkel tér vissza.
	 * 
	 * @param board
	 * @param from
	 * @param to
	 * @return Ha nincs ütési lehetőség 0-val tér vissza, ha létezik 1. Ha
	 *         létezik ütéssorozat, akkor 1 feletti értékkel tér vissza.
	 */
	protected Double getAttackPossibility(Board board, Cell to) {
		Double d = 0.0;
		try {
			ArrayList<Cell> possibleAttackCells = null;
			possibleAttackCells = board.getCellPossibleAttack(to);

			if (possibleAttackCells != null) {
				for (Cell c : possibleAttackCells) {
					Board paralelWorkingCopy = board.getBoardClone();
					paralelWorkingCopy.moveFigureFromTo(to, c);
					d += 1.0;
					d += getAttackPossibility(paralelWorkingCopy, c);
					paralelWorkingCopy = null;
				}
			}

		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * Visszatér, hogy az új helyen üthető-e a bábu. Negatív értékekkel
	 * korrigálja a heurisztikát.
	 * 
	 * @param board
	 * @param cell
	 * @return
	 */
	protected Double willBeAttacked(Board board, Cell cell) {
		return getFigureAttackedRate(board, cell, true);
	}

	/**
	 * Visszatér, hogy az eddigi helyen üthető-e a babu. Pozitív értékekkel
	 * korrigálja a heurisztikát.
	 * 
	 * @param board
	 * @param cell
	 * @return
	 */
	protected Double isAttacked(Board board, Cell cell) {
		return getFigureAttackedRate(board, cell, false);
	}

	/**
	 * Visszatér, hogy az új helyen üthető-e a bábu?
	 * 
	 * @param board
	 * @param cell
	 * @param nextRound
	 *            Ha true, akkor a cell a lépés célja, különben a cell a lépés
	 *            kezdete. Vagyis ha a következő lépést vizsgálja, akkor
	 *            negatívan befolyásolja az heurisztikát. Ha a jelen lépést
	 *            vizsgálja, akkor pozitívan befolyásolja az heurisztikát.
	 * @return Ha üthető a bábu visszatérési értéke 1, különben 0. Ha a bábu
	 *         ütésével több saját bábu is üthető ütésláncolattal, akkor 1
	 *         fölötti értékkel tér vissza.
	 */
	protected Double getFigureAttackedRate(Board board, Cell cell,
			boolean nextRound) {
		Double d = 0.0;
		ArrayList<Cell> attackingFromCells = board.isFigureAttacked(cell);
		int signal = (nextRound == true) ? -1 : 1;

		if (attackingFromCells.size() > 0) {
			for (Cell c : attackingFromCells) {
				try {
					Board paralelWorkingCopy = board.getBoardClone();

					d += signal;
					d += signal * getAttackPossibility(paralelWorkingCopy, c);

					paralelWorkingCopy = null;

				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return d;
	}

	/**
	 * Visszatér, hogy milyen távol van a bábu attól, hogy dáma legyen.
	 * 
	 * @param board
	 * @param cell
	 * @param figure
	 * @return [0,1] intervallumból tér vissza a távolság függvényében. Ha már
	 *         dáma, visszatérési értéke 0.
	 */
	protected Double willBeChecker(Board board, Cell from, Cell to) {
		Double d = 0.0;
		Figure figure = board.getFigure(from);

		if (figure != null && !figure.isChecker()) {

			int row = to.getRow();
			int dist = (board.dimension - 1) - (row * figure.getDir());
			d = 1 - (double) dist / (double) (board.dimension - 1);

			d = Math.pow(d, 2);

		}

		return d;
	}

	/**
	 * Visszatér a legközelebbi ellenfél távolsága alapján számított osztályozó
	 * értékkel.
	 * 
	 * @param board
	 * @param from
	 * @param to
	 * @return
	 */
	// TODO: az ellenfél támadási pontját kiszámolni
	protected Double getDistanceFromEnemy(Board board, Cell from, Cell to) {
		ArrayList<Cell> enemyFigures = board.getAllFigureCellsPlayer(!color);
		Cell closestEnemy = null;
		Double closestEnemyDist = null;
		for (Cell c : enemyFigures) {
			double dist = c.distance(from);
			if (closestEnemyDist == null || closestEnemyDist > dist) {
				closestEnemy = c;
				closestEnemyDist = dist;
			}
		}
		double newDist = closestEnemy.distance(to);
		double signal = (newDist < closestEnemyDist) ? 1.0 : -1.0;

		if (newDist != closestEnemyDist) {
			return signal;
		} else {
			return 0.5;
		}
	}

	/**
	 * Megvizsgálja, hogy tudja-e akadályozni, hogy az ellenfél levegye a
	 * bábuját.
	 * 
	 * @param board
	 * @param from
	 * @param to
	 * @return Visszatér a megvédhető bábuk számával.
	 */
	protected Double canProtectOthers(Board board, Cell from, Cell to) {
		Double d = 0.0;

		try {
			ArrayList<Cell> enemyNumberCanAttackBefore = board
					.getFigurePossibleAttack(!color);
			board.moveFigureFromTo(from, to);
			ArrayList<Cell> enemyNumberCanAttackAfter = board
					.getFigurePossibleAttack(!color);

			d = (double) (enemyNumberCanAttackBefore.size() - enemyNumberCanAttackAfter
					.size());
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return d;
	}

	/**
	 * Visszatér, hogy az adott állapot nyerést jelent vagy nem
	 * 
	 * @param board
	 * @return true nyer, false nem
	 */
	protected boolean willIWin(Board board) {
		if (board.getFigureCount(!this.color) == 0
				|| !board.canPlayerMove(!this.color)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Visszatér, hogy az adott állapot vesztést jelent vagy nem
	 * @param board
	 * @return true vesztés, false nem
	 */
	protected boolean willILose(Board board) {
		if (board.getFigureCount(this.color) == 0
				|| !board.canPlayerMove(this.color)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Visszaadja hogy Checker-t fogok-e ütni.
	 * 
	 * @param board
	 * @param from
	 * @param to
	 * @return Ha igen, akkor 1-el tér vissza, ha nem, akkor 0-val.
	 */
	protected Double isCheckerAttacked(Board board, Cell from, Cell to) {
		Double d = 0.0;
		if (board.getFigure(board.getMidCell(from, to)).isChecker() == true) {
			d += 1.0;
			// System.out.println("Checkert ütött!");
		}
		return d;
	}
}