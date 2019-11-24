package student;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import game.FindState;
import game.FleeState;
import game.Node;
import game.NodeStatus;
import game.SewerDiver;

public class DiverMin extends SewerDiver {
	private HashSet<Long> visited= new HashSet<>(); // visted set for the Find; every time Diver Min visits a node, it
												    // is added

	/** Get to the ring in as few steps as possible. Once you get there, <br>
	 * you must return from this function in order to pick<br>
	 * it up. If you continue to move after finding the ring rather <br>
	 * than returning, it will not count.<br>
	 * If you return from this function while not standing on top of the ring, <br>
	 * it will count as a failure.
	 *
	 * There is no limit to how many steps you can take, but you will receive<br>
	 * a score bonus multiplier for finding the ring in fewer steps.
	 *
	 * At every step, you know only your current tile's ID and the ID of all<br>
	 * open neighbor tiles, as well as the distance to the ring at each of <br>
	 * these tiles (ignoring walls and obstacles).
	 *
	 * In order to get information about the current state, use functions<br>
	 * currentLocation(), neighbors(), and distanceToRing() in state.<br>
	 * You know you are standing on the ring when distanceToRing() is 0.
	 *
	 * Use function moveTo(long id) in state to move to a neighboring<br>
	 * tile by its ID. Doing this will change state to reflect your new position.
	 *
	 * A suggested first implementation that will always find the ring, but <br>
	 * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
	 * Some modification is necessary to make the search better, in general. */
	@Override
	public void find(FindState state) {
		// TODO : Find the ring and return.
		// DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
		// Instead, write your method elsewhere, with a good specification,
		// and call it from this one.
		dfs(state);
	}

	/** This method runs DFS but optimized. DFS- Each time Diver Min runs into <br>
	 * a dead end, it will go back to the last node that still has an <br>
	 * unvisited neighbor. Optimized- Whenever Diver Min has a choice between <br>
	 * different nodes, it will go to the one that is closest to the ring. */
	private void dfs(FindState state) {
		if (state.distanceToRing() == 0) {
			return;
		} else {
			long current= state.currentLocation();
			visited.add(current);
			Heap<NodeStatus> closest= getCLosest(state.neighbors());
			while (closest.size() != 0) {
				NodeStatus c= closest.poll();
				long closestId= c.getId();
				if (!visited.contains(closestId)) {
					visited.add(closestId);
					state.moveTo(closestId);
					dfs(state);
					if (state.distanceToRing() == 0) { return; }
					state.moveTo(current);
				}
			}
		}
	}

	/** Returns a min heap with all the neighbors of the node with the <br>
	 * priorities as its distance to the ring. */
	private Heap<NodeStatus> getCLosest(Collection<NodeStatus> neighbors) {
		Heap<NodeStatus> heap= new Heap<>();
		for (NodeStatus n : neighbors) {
			heap.add(n, n.getDistanceToTarget());
		}
		return heap;
	}

	/** Flee the sewer system before the steps are all used, trying to <br>
	 * collect as many coins as possible along the way. Your solution must ALWAYS <br>
	 * get out before the steps are all used, and this should be prioritized above<br>
	 * collecting coins.
	 *
	 * You now have access to the entire underlying graph, which can be accessed<br>
	 * through FleeState. currentNode() and getExit() will return Node objects<br>
	 * of interest, and getNodes() will return a collection of all nodes on the graph.
	 *
	 * You have to get out of the sewer system in the number of steps given by<br>
	 * getStepsRemaining(); for each move along an edge, this number is <br>
	 * decremented by the weight of the edge taken.
	 *
	 * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
	 * When n is moved-to, coins on node n are automatically picked up.
	 *
	 * You must return from this function while standing at the exit. Failing <br>
	 * to do so before steps run out or returning from the wrong node will be<br>
	 * considered a failed run.
	 *
	 * Initially, there are enough steps to get from the starting point to the<br>
	 * exit using the shortest path, although this will not collect many coins.<br>
	 * For this reason, a good starting solution is to use the shortest path to<br>
	 * the exit. */
	@Override
	public void flee(FleeState state) {
		// TODO: Get out of the sewer system before the steps are used up.
		// DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
		// with a good specification, and call it from this one.

		ratiosAttempt(state);

	}

	/** This is our attempt using ratios. The ratio we refer to is the ratio <br>
	 * between the coin value of a node and the distance that node is from the <br>
	 * current node. First, we create a max heap with each node and its ratio. Then <br>
	 * we compute the path from the current node to the node with max ratio and the <br>
	 * path from the node with max ratio to the exit. If the sum of these pathes <br>
	 * are less than the number of steps left, Min goes along current2Max and the <br>
	 * method is called again. If the sum is equal to the number of steps left, <br>
	 * Min goes along current2Max and max2Exit. If the sum is greater than the <br>
	 * number of steps left, we find a new max node by polling the heap that is <br>
	 * less than or equal to the number of steps left, we go to it, and exit. */
	private void ratiosAttempt(FleeState state) {
		// create max heap
		Heap<Node> heap= ratio(state);

		// path to max
		List<Node> current2Max= Paths.shortest(state.currentNode(), heap.peek());
		// path to exit from max
		List<Node> max2Exit= Paths.shortest(heap.peek(), state.getExit());
		// get rid of max node
		heap.poll();

		// create path for it to go on
		if (Paths.pathSum(current2Max) + Paths.pathSum(max2Exit) < state.stepsLeft()) {
			go(current2Max, state);
			ratiosAttempt(state);
		} else {
			// create new current2Max and max2Exit with smaller ratio
			while (Paths.pathSum(current2Max) + Paths.pathSum(max2Exit) > state.stepsLeft()) {
				current2Max= Paths.shortest(state.currentNode(), heap.peek());
				max2Exit= Paths.shortest(heap.peek(), state.getExit());
				heap.poll();
			}
			go(current2Max, state);
			go(max2Exit, state);
		}
	}

	/** Returns a max heap of with the priorities as ratio(= coins/steps). <br>
	 * For the current node that Min is standing on, we set the length of the <br>
	 * path to be 0.01 as it is close to 0 and doesn't affect the <br>
	 * effectiveness of the heap. */
	private Heap<Node> ratio(FleeState state) {
		// initialize max heap
		Heap<Node> heap= new Heap<>(false);
		// add values with priority as ratio
		double ratio;
		for (Node n : state.allNodes()) {
			// if node is current node, make distance 0.01 (because it is close to 0)
			int length= Paths.pathSum(Paths.shortest(state.currentNode(), n));
			if (length == 0) {
				ratio= n.getTile().coins() / 0.01;
			} else {
				ratio= n.getTile().coins() / length;
			}
			heap.add(n, ratio);
		}
		return heap;
	}

	/** Executes the path that is given with a given state. */
	private void go(List<Node> path, FleeState state) {
		for (Node n : path) {
			if (n != state.currentNode()) {
				state.moveTo(n);
			}
		}
	}
}
