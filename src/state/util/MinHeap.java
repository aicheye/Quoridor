package state.util;

/**
 * state.util.MinHeap class
 * <p>
 * Represents a minimum heap priority queue
 *
 * @author Sean Yang
 * @version 30/05/2024
 */
public class MinHeap {
    int capacity;
    int[] heap;
    int[][] positions;
    int size;

    /**
     * PriorityQueue constructor
     * <p>
     * Constructor for PriorityQueue
     *
     * @param n {@code int} - The capacity of the priority queue
     */
    public MinHeap(int n) {
        capacity = n;
        heap = new int[n];
        positions = new int[n][2];
        size = 0;
        for (int i = 0; i < n; i++) heap[i] = Integer.MAX_VALUE;
    }

    /**
     * parent method
     * <p>
     * Returns the index of the parent of the node at index i
     *
     * @param i {@code int} - The index of the node
     * @return {@code int} - The index of the parent of the node at index i
     */
    int parent(int i) {
        return (i - 1) / 2;
    }

    /**
     * left method
     * <p>
     * Returns the index of the left child of the node at index i
     *
     * @param i {@code int} - The index of the node
     * @return {@code int} - The index of the left child of the node at index i
     */
    int left(int i) {
        return (2 * i) + 1;
    }

    /**
     * right method
     * <p>
     * Returns the index of the right child of the node at index i
     *
     * @param i {@code int} - The index of the node
     * @return {@code int} - The index of the right child of the node at index i
     */
    int right(int i) {
        return (2 * i) + 2;
    }

    /**
     * insert method
     * <p>
     * Inserts a key position pair into the priority queue
     *
     * @param K   {@code int} the key to insert
     * @param pos {@code int[]} the position to insert
     */
    public void insert(int[] pos, int K) {
        // declare variables
        int foo;
        int[] bar;
        int idx;

        idx = size;
        heap[idx] = K;
        positions[idx] = pos.clone();
        size++;

        // heapify-up:
        while (idx != 0 && K < heap[parent(idx)]) {
            foo = heap[idx];
            heap[idx] = heap[parent(idx)];
            heap[parent(idx)] = foo;

            bar = positions[idx].clone();
            positions[idx] = positions[parent(idx)].clone();
            positions[parent(idx)] = bar.clone();

            idx = parent(idx);
        }
    }

    /**
     * extract method
     * <p>
     * Extracts the minimum key position pair from the priority queue
     *
     * @return {@code int[]} - The minimum key position pair
     */
    public int[] extract() {
        // declare variables
        int[] root = new int[2];
        int idx = 0;
        int smallest = -1;

        if (size <= 0) {
            root[0] = -1;
            root[1] = -1;
        } else if (size == 1) {
            size--;
            root = positions[0].clone();
        } else {
            root = positions[0].clone();

            heap[0] = heap[size - 1];
            heap[size - 1] = Integer.MAX_VALUE;

            positions[0] = positions[size - 1].clone();
            positions[size - 1] = new int[2];

            size--;

            // heapify-down:
            while (smallest != idx) {
                smallest = idx;

                if (left(idx) < size && heap[left(idx)] < heap[smallest]) smallest = left(idx);

                if (right(idx) < size && heap[right(idx)] < heap[smallest]) smallest = right(idx);

                if (smallest != idx) {
                    int foo = heap[idx];
                    heap[idx] = heap[smallest];
                    heap[smallest] = foo;

                    int[] bar = positions[idx].clone();
                    positions[idx] = positions[smallest].clone();
                    positions[smallest] = bar.clone();

                    idx = smallest;
                }
            }
        }

        return root;
    }
}
