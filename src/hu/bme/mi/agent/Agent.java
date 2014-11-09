package hu.bme.mi.agent;

import hu.bme.mi.dama.Board;
import hu.bme.mi.dama.ButtonController;
import hu.bme.mi.dama.Cell;
import hu.bme.mi.utils.AgentsTurnListener;
import hu.bme.mi.utils.GameException;

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
	 * MI játékos lépése kiszámolja a lehetséges lépések heurisztikáját és
	 * kiválasztja a legjobbat, majd elvégi a lépést a táblán
	 */
	private void nextMove() {
		Movement maxHeuristicMovement = getNextMovement(color, actual);
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
			h += getDistanceFromEdge(actual, to);

			try {
				workingCopy.moveFigureFromTo(from, to);
				int i = 0;

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
				System.out.println(movementkey + ". lehetseges lepes:"
						+ movement.getFrom().getRow() + ":"
						+ movement.getFrom().getColumn() + " -to:"
						+ movement.getTo().getRow() + ":"
						+ movement.getTo().getColumn() + " -h:"
						+ movement.getH()
						+ (bestMoves.contains(movement) ? "  [*]" : ""));
			}
			movementkey++;
		}

		Random randomselect = new Random();
		returnMovemnet = bestMoves.get(randomselect.nextInt(bestMoves.size()));
		return returnMovemnet;
	}

	@Override
	public void yourTurn() {
		// TODO Auto-generated method stub
		System.out.println("agent jön");
		nextMove();
	}

	/*
	 * Monitorozó, osztályozó függvények
	 */
	/**
	 * Visszaadja a cella távolságát a tábla szélétől százalékosan
	 * 
	 * @param board
	 * @param cell
	 * @return [0,1] intervallumból tér vissza
	 */
	protected Double getDistanceFromEdge(Board board, Cell cell) {
		Double d = null;

		int dist = Math.min(cell.getColumn(),
				(board.dimension - 1) - cell.getColumn());
		d = 1.0 - dist / (double) (board.dimension - 1);

		System.out.println("Lépett cella: " + cell.getColumn());
		System.out.println("távolság: " + dist);
		System.out.println("távolság százalék: " + d);

		return d;
	}
}
