package hu.bme.mi.utils;

import hu.bme.mi.agent.Movement;

import java.util.ArrayList;
import java.util.List;

public class Graph {
	private List<Vertex> vertexes;
	private List<Edge> edges;
	private Vertex startVertex;

	public Graph() {
		this.vertexes = new ArrayList<>();
		this.edges = new ArrayList<>();
		this.startVertex = null;
	}

	public Graph(Vertex startVertex) {
		this.vertexes = new ArrayList<>();
		this.edges = new ArrayList<>();
		this.startVertex = startVertex;
	}

	public Graph(Vertex startVertex, List<Vertex> vertexes, List<Edge> edges) {
		this.vertexes = vertexes;
		this.edges = edges;
		this.startVertex = startVertex;
	}

	public List<Vertex> getVertexes() {
		return vertexes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void addVertex(Vertex vertex) {
		vertexes.add(vertex);
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public Vertex getStartVertex() {
		return startVertex;
	}

	public void insertNode(Vertex vertex, Edge edge) {
		addVertex(vertex);
		addEdge(edge);
	}

	/**
	 * Gráf maximum útvonal keresés.
	 * 
	 * @param startVertex
	 * @param actualLevel
	 * @return
	 */
	public Movement getSearchMaxMovement(Vertex startVertex, int actualLevel) {
		List<Movement> possibleMovements = new ArrayList<>();
		List<Edge> edgeList = new ArrayList<>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(startVertex)) {
				Double h = 0.0;
				int signal = 1;
				if (actualLevel % 2 == 1) {
					signal = -1;
				}
				h = signal * edge.getMovement().getH();

				Movement movement = getSearchMaxMovement(edge.getDestination(),
						actualLevel + 1);
				Movement edgeMovement = edge.getMovement();
				if (movement != null) {
					h += movement.getH();
					edgeMovement.moveChain.add(movement);
					edgeMovement.moveChain.addAll(movement.moveChain);
				}
				edgeMovement.setH(h);
				edge.setMovemnet(edgeMovement);

				possibleMovements.add(edge.getMovement());
			}
		}
		if (actualLevel % 2 == 1) {
			for (Movement movement : possibleMovements) {
				movement.setH(movement.getH() / possibleMovements.size());
			}
		}

		Movement maxHeuristicMovement = null;
		for (Movement movement : possibleMovements) {
			if (maxHeuristicMovement == null
					|| movement.getH() > maxHeuristicMovement.getH()) {
				maxHeuristicMovement = movement;
			}
		}

		return maxHeuristicMovement;
	}
}
