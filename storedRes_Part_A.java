import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

public class res_store_partA {
	static class Node {
		String name;
		boolean locked;
		int lockedBy;
		Node parent;
		List<Node> children;
		Set<Node> lockedDescendants;

		Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
			this.children = new ArrayList<>();
			this.lockedDescendants = new HashSet<>();
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		int m = scanner.nextInt();
		int q = scanner.nextInt();
		scanner.nextLine();
		Map<String, Node> nodeMap = new HashMap<>();
		Node root = null;
		// Read node names
		String[] nodeNames = new String[n];
		for (int i = 0; i < n; i++) {
			nodeNames[i] = scanner.nextLine().trim();
		}
		// Create the root node
		root = new Node(nodeNames[0], null);
		nodeMap.put(nodeNames[0], root);
		// Build the tree structure
		Queue<Node> queue = new LinkedList<>();
		queue.add(root);
		int index = 1;
		while (!queue.isEmpty() && index < n) {
			Node currentNode = queue.poll();
			for (int i = 0; i < m && index < n; i++) {
				Node childNode = new Node(nodeNames[index], currentNode);
				currentNode.children.add(childNode);
				nodeMap.put(nodeNames[index], childNode);
				queue.add(childNode);
				index++;
			}
		}
		// Process queries and store results
		List<Boolean> results = new ArrayList<>();
		for (int i = 0; i < q; i++) {
			String[] query = scanner.nextLine().split(" ");
			int operation = Integer.parseInt(query[0]);
			String nodeName = query[1];
			int userId = Integer.parseInt(query[2]);

			Node node = nodeMap.get(nodeName);
			boolean result = false;

			if (operation == 1) {
				result = lock(node, userId);
			} else if (operation == 2) {
				result = unlock(node, userId);
			} else if (operation == 3) {
				result = upgradeLock(node, userId);
			}

			results.add(result);
		}

		// Print all results at once
		for (boolean result : results) {
			System.out.println(result);
		}

		scanner.close();
	}

	private static boolean lock(Node node, int userId) {
		if (node.locked || hasLockedAncestor(node) || hasLockedDescendant(node)) {
			return false;
		}

		node.locked = true;
		node.lockedBy = userId;

		Node current = node.parent;
		while (current != null) {
			current.lockedDescendants.add(node);
			current = current.parent;
		}

		return true;
	}

	private static boolean unlock(Node node, int userId) {
		if (!node.locked || node.lockedBy != userId) {
			return false;
		}

		node.locked = false;
		node.lockedBy = 0;

		Node current = node.parent;
		while (current != null) {
			current.lockedDescendants.remove(node);
			current = current.parent;
		}

		return true;
	}

	private static boolean upgradeLock(Node node, int userId) {
		if (node.locked || hasLockedAncestor(node) || !allDescendantsLockedByUser(node, userId)) {
			return false;
		}

		unlockAllDescendants(node, userId);
		node.locked = true;
		node.lockedBy = userId;

		Node current = node.parent;
		while (current != null) {
			current.lockedDescendants.add(node);
			current = current.parent;
		}

		return true;
	}

	private static boolean hasLockedAncestor(Node node) {
		Node current = node.parent;
		while (current != null) {
			if (current.locked) {
				return true;
			}
			current = current.parent;
		}
		return false;
	}

	private static boolean hasLockedDescendant(Node node) {
		return !node.lockedDescendants.isEmpty();
	}

	private static boolean allDescendantsLockedByUser(Node node, int userId) {
		for (Node descendant : node.lockedDescendants) {
			if (descendant.lockedBy != userId) {
				return false;
			}
		}
		return true;
	}

	private static void unlockAllDescendants(Node node, int userId) {
		for (Node descendant : new HashSet<>(node.lockedDescendants)) {
			unlock(descendant, userId);
		}
	}
}
