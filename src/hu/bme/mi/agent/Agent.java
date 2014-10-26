package hu.bme.mi.agent;

import hu.bme.mi.dama.Board;
import hu.bme.mi.dama.ButtonController;
import hu.bme.mi.dama.Cell;
import hu.bme.mi.utils.GameException;
import hu.bme.mi.utils.AgentsTurnListener;

import java.util.ArrayList;

public class Agent implements AgentsTurnListener {
	private Board actual;
	private boolean color;
	private ButtonController controller;

	/**
	 * Mesterséges intelligenciát reprezentáló osztály
	 * 
	 * @param board
	 *            A közös valóságalap a tábla aktuális állapota
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

	private void nextMove() {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();

		ArrayList<Cell> possibleAttackCells = actual
				.getFigurePossibleAttack(color);
		if (possibleAttackCells.size() > 0) {
			// Van kötelezõ ütés
			for (Cell cell : possibleAttackCells) {
				ArrayList<Cell> attackTargetCell = actual
						.getCellPossibleAttack(cell);
				for (Cell cell2 : attackTargetCell) {
					possibleNextMoves.add(new Movement(cell, cell2,
							attackHeuristica(cell, cell2)));
				}
			}
		} else {
			// Szabad lépés
			ArrayList<Cell> possibleMoveCells = actual
					.getFigurePossibleMove(color);
			for (Cell cell : possibleMoveCells) {
				ArrayList<Cell> moveTargetCell = actual
						.getCellPossibleMove(cell);
				for (Cell cell2 : moveTargetCell) {
					possibleNextMoves.add(new Movement(cell, cell2,
							idleHeuristica(cell, cell2)));
				}
			}
		}

		Movement maxHeuristicMovement = getMaxHeuristic(possibleNextMoves);
		controller.handlePlayerMovement(maxHeuristicMovement.getFrom(),
				maxHeuristicMovement.getTo());

	}

	private int attackHeuristica(Cell from, Cell to) {
		int h = 0;

		return h;
	}

	private int idleHeuristica(Cell from, Cell to) {
		int h = 0;

		return h;
	}

	private Movement getMaxHeuristic(ArrayList<Movement> movementList) {
		Movement returnMovemnet = null;

		for (Movement movement : movementList) {
			if (returnMovemnet == null
					|| movement.getH() > returnMovemnet.getH()) {
				returnMovemnet = movement;
			}
		}

		return returnMovemnet;
	}

	@Override
	public void yourTurn() {
		// TODO Auto-generated method stub
		System.out.println("agent jön");
		nextMove();
	}
}
