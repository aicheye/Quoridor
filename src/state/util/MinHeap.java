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
    // declare fields
    int capacity; // maximum possible size of the heap
    int[] heap; // array to store the heap
    int[][] items; // array to store each heap item
    int size; // current size of the heap

    /**
     * PriorityQueue constructor
     * <p>
     * Constructor for PriorityQueue
     *
     * @param n {@code int} - The capacity of the priority queue
     */
    public MinHeap(int n) {
        capacity = n; // set the capacity
        heap = new int[n]; // initialize the heap array
        items = new int[n][2]; // initialize the items aray
        size = 0; // set the size to 0
        for (int i = 0; i < n; i++) heap[i] = Integer.MAX_VALUE; // set all heap values to max value
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

        // insert the key and position
        idx = size;
        heap[idx] = K;
        items[idx] = pos.clone();
        size++;

        // heapify-up:
        while (idx != 0 && K < heap[parent(idx)]) {
            // swap the current node with its parent
            foo = heap[idx];
            heap[idx] = heap[parent(idx)];
            heap[parent(idx)] = foo;

            // swap the current position with its parent
            bar = items[idx].clone();
            items[idx] = items[parent(idx)].clone();
            items[parent(idx)] = bar.clone();

            // move up the heap
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
        int foo;
        int[] bar;

        // return if the heap is empty
        if (size <= 0) {
            root = new int[]{-1, -1};
        }

        // return the root if the heap has only one element
        else if (size == 1) {
            // extract the root
            root = items[0].clone();

            // set the root to max value
            heap[0] = Integer.MAX_VALUE;
            items[0] = new int[2];

            size--; // decrement the size
        } else {
            // extract the root
            root = items[0].clone();

            // swap the root with the last element
            heap[0] = heap[size - 1];
            heap[size - 1] = Integer.MAX_VALUE;

            // swap the root position with the last position
            items[0] = items[size - 1].clone();
            items[size - 1] = new int[2];

            size--; // decrement the size

            // heapify-down:
            while (smallest != idx) {
                // find the smallest child
                smallest = idx;

                if (left(idx) < size && heap[left(idx)] < heap[smallest]) smallest = left(idx);
                if (right(idx) < size && heap[right(idx)] < heap[smallest]) smallest = right(idx);

                // swap the current node with the smallest child
                if (smallest != idx) {
                    foo = heap[idx];
                    heap[idx] = heap[smallest];
                    heap[smallest] = foo;

                    bar = items[idx].clone();
                    items[idx] = items[smallest].clone();
                    items[smallest] = bar.clone();

                    idx = smallest;
                    smallest = -1;
                }
            }
        }

        return root;
    }
}
