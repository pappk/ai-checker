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
 * Az {actual} v�ltoz� pointer a t�nyleges t�bl�ra.
 * Nem szabad rajta m�dos�t�st v�gezni, kiv�ve a v�gleges l�p�st.
 * 
 */

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
	 * color sz�n� j�t�kos legoptim�lisabb l�p�svel visszat�r� f�gg�ny
	 * 
	 * @param color
	 *            J�t�kos sz�ne
	 * @param board
	 *            aktu�lis j�t�k�llapot
	 * @return
	 */
	private Movement getNextMovement(boolean color, Board board) {
		ArrayList<Movement> possibleNextMoves = new ArrayList<>();
		try {
			Board workingBoard = board.getBoardClone();

			ArrayList<Cell> possibleAttackCells = workingBoard
					.getFigurePossibleAttack(color);
			if (possibleAttackCells.size() > 0) {
				// Van k�telez� �t�s
				for (Cell cell : possibleAttackCells) {
					ArrayList<Cell> attackTargetCell = workingBoard
							.getCellPossibleAttack(cell);
					for (Cell cell2 : attackTargetCell) {
						possibleNextMoves.add(new Movement(cell, cell2,
								attackHeuristica(cell, cell2, workingBoard)));
					}
				}
			} else {
				// Szabad l�p�s
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
	 * MI j�t�kos l�p�se kisz�molja a lehets�ges l�p�sek heurisztik�j�t �s
	 * kiv�lasztja a legjobbat, majd elv�gi a l�p�st a t�bl�n
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
	 * T�mad�s eset�n a param�terk�nt megadott l�p�s min�s�g�t oszt�lyoz�
	 * f�ggv�ny
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
	 * Szabad l�p�s eset�n a param�terk�nt megadott l�p�s min�s��gt oszt�lyoz�
	 * f�ggv�ny
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
	 * L�p�sek list�j�b�l kiv�lasztja a legnagyonn heurisztik�val rendelkez�t
	 * 
	 * @param movementList
	 * @return
	 */
	private Movement getMaxHeuristic(ArrayList<Movement> movementList) {
		Movement returnMovemnet = null;
		Double maxHeuristic = null;
		ArrayList<Movement> bestMoves = new ArrayList<>();

		// Max heurisztika felder�t�se
		for (Movement movement : movementList) {
			if (maxHeuristic == null || movement.getH() > maxHeuristic) {
				returnMovemnet = movement;
				maxHeuristic = movement.getH();
			}
		}

		// Max heurisztik�val rendelkez� l�p�sek kiv�laszt�sa
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
		System.out.println("agent j�n");
		nextMove();
	}

	/*
	 * Monitoroz�, oszt�lyoz� f�ggv�nyek
	 */
	/**
	 * Visszaadja a cella t�vols�g�t a t�bla sz�l�t�l sz�zal�kosan
	 * 
	 * @param board
	 * @param cell
	 * @return [0,1] intervallumb�l t�r vissza
	 */
	protected Double getDistanceFromEdge(Board board, Cell cell) {
		Double d = null;

		int dist = Math.min(cell.getColumn(),
				(board.dimension - 1) - cell.getColumn());
		d = 1.0 - dist / (double) (board.dimension - 1);

		System.out.println("L�pett cella: " + cell.getColumn());
		System.out.println("t�vols�g: " + dist);
		System.out.println("t�vols�g sz�zal�k: " + d);

		return d;
	}
}
