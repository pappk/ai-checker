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
	 * Mesters�ges intelligenci�t reprezent�l� oszt�ly
	 * 
	 * @param board
	 *            A k�z�s val�s�galap a t�bla aktu�lis �llapot�ra mutat� pointer
	 * @param color
	 *            Az MI j�t�kos sz�ne
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
	 * MI j�t�kos l�p�se
	 * kisz�molja a lehets�ges l�p�sek heurisztik�j�t �s kiv�lasztja a legjobbat,
	 * majd elv�gi a l�p�st a t�bl�n
	 */
	private void nextMove() {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();

		ArrayList<Cell> possibleAttackCells = actual
				.getFigurePossibleAttack(color);
		if (possibleAttackCells.size() > 0) {
			// Van k�telez� �t�s
			for (Cell cell : possibleAttackCells) {
				ArrayList<Cell> attackTargetCell = actual
						.getCellPossibleAttack(cell);
				for (Cell cell2 : attackTargetCell) {
					possibleNextMoves.add(new Movement(cell, cell2,
							attackHeuristica(cell, cell2)));
				}
			}
		} else {
			// Szabad l�p�s
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
		try {
			controller.handlePlayerMovement(maxHeuristicMovement.getFrom(),
					maxHeuristicMovement.getTo());
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * T�mad�s eset�n a param�terk�nt megadott l�p�s min�s�g�t oszt�lyoz� f�ggv�ny
	 * @param from
	 * @param to
	 * @return heurisztika
	 */
	private double attackHeuristica(Cell from, Cell to) {
		double h = 0;

		return h;
	}

	/**
	 * Szabad l�p�s eset�n a param�terk�nt megadott l�p�s min�s��gt oszt�lyoz� f�ggv�ny
	 * @param from
	 * @param to
	 * @return heurisztika
	 */
	private double idleHeuristica(Cell from, Cell to) {
		double h = 0;

		return h;
	}
	
	/**
	 * Heurisztika f�ggv�ny, l�p�s poz�ci�ja alapj�n
	 * @param from
	 * @param to
	 * @return
	 */
	private double distanceFromEdge(Cell from, Cell to){
		double h = 0;
		
		

		return h;
	}

	/**
	 * L�p�sek list�j�b�l kiv�lasztja a legnagyonn heurisztik�val rendelkez�t
	 * @param movementList
	 * @return
	 */
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
		System.out.println("agent j�n");
		nextMove();
	}
}
