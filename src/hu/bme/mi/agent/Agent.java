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
	 * color színû játékos legoptimálisabb lépésvel visszatérõ függény
	 * 
	 * @param color	Játékos színe
	 * @param board	aktuális játékállapot
	 * @return
	 */
	private Movement getNextMovement(boolean color, Board board) {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();
		Board workingBoard = board.getBoardClone();

		ArrayList<Cell> possibleAttackCells = workingBoard
				.getFigurePossibleAttack(color);
		if (possibleAttackCells.size() > 0) {
			// Van kötelezõ ütés
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
	 * Támadás esetén a paraméterként megadott lépés minõségét osztályozó
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
	 * Szabad lépés esetén a paraméterként megadott lépés minõséégt osztályozó
	 * függvény
	 * 
	 * @param from
	 * @param to
	 * @return heurisztika
	 */
	private double idleHeuristica(Cell from, Cell to, Board actual) {
		double h = 0;

		return h;
	}


	/**
	 * Lépések listájából kiválasztja a legnagyonn heurisztikával rendelkezõt
	 * 
	 * @param movementList
	 * @return
	 */
	private Movement getMaxHeuristic(ArrayList<Movement> movementList) {
		Movement returnMovemnet = null;
		ArrayList<Movement> BestMoves = new ArrayList<>();
		int movementkey = 0;

		for (Movement movement : movementList) {
			if (returnMovemnet == null
					|| movement.getH() >= returnMovemnet.getH()) {
				returnMovemnet = movement;
				BestMoves.add(movement);
			}
			System.out.println(movementkey+". lehetseges lepes:"+movement.getFrom().getRow()+":"+movement.getFrom().getColumn()+" -to:"+movement.getTo().getRow()+":"+movement.getTo().getColumn());
			movementkey++;
		}

		Random randomselect = new Random();
		returnMovemnet = BestMoves.get(randomselect.nextInt(movementkey));
		return returnMovemnet;
	}

	@Override
	public void yourTurn() {
		// TODO Auto-generated method stub
		System.out.println("agent jön");
		nextMove();
	}
}
