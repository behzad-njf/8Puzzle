/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg8puzzle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * @author behzad
 */
public class Puzzle {

    static final LinkedList<State> list = new LinkedList<>();   //list for final states(Solution)
    static final LinkedList<FGH> list2 = new LinkedList<>();
    static final byte[] goalTiles = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    static double timeOfExecution = 0;
    static boolean solved = false;
    // A* priority queue.
    final static PriorityQueue<State> queue = new PriorityQueue<>(100, (State a, State b) -> a.priority() - b.priority());
    static int countOfStates = 0 ;
    // The closed state set.
    final static HashSet<State> closed = new HashSet<>();

    // Add a non-null and not closed successor to the A* queue.
    void addSuccessor(State successor) {
        if (successor != null && !closed.contains(successor)) {
            queue.add(successor);
        }
    }


    void solve(byte[] initial) {

        queue.clear();
        closed.clear();
        long time_start = System.currentTimeMillis();
        // Add initial state to queue.
        queue.add(new State(initial));

        while (!queue.isEmpty()) {
            // Get the lowest priority state.
            State state = queue.poll();
            // If it's the goal, finish !!!
            if (state.isGoal()) {
                timeOfExecution = (double) ((int) ((System.currentTimeMillis() / 1000.0 - time_start / 1000.0) * 1000)) / 1000;
                solved = true;
                countOfStates = closed.size();
                state.addToList();
                return;
            }
            
            // we don't revisit this state again.
            closed.add(state);

            // Add successors to the queue.
            addSuccessor(state.moveUP());
            addSuccessor(state.moveDOWN());
            addSuccessor(state.moveRIGHT());
            addSuccessor(state.moveLEFT());
        }

    }

    static int index(byte[] a, int val) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == val) {
                return i;
            }
        }
        return -1;
    }

    static String getMove(State pre, State nex) {
        byte pre_tiles[] = pre.tiles;
        byte nex_tiles[] = nex.tiles;
        int old_index = index(pre_tiles, 0);
        int new_index = index(nex_tiles, 0);
        if (new_index > old_index) {
            switch (new_index - old_index) {
                case 1:
                    return "right";
                case 3:
                    return "down";
            }
        } else {
            switch (old_index - new_index) {
                case 1:
                    return "left";
                case 3:
                    return "up";
            }
        }
        return "unknown";
    }

    // Return the Manhatten distance between of tile with index.
    static int manhattanDistance(int index, int tile) {
        return Math.abs(index / 3 - tile / 3) + Math.abs(index % 3 - tile % 3);
    }

    // For our A* heuristic, we can use max of Manhatten distances of all tiles.
    // or sum of all Manhattan distances of all Tiles.
    static int heuristic(byte[] tiles) {
        int h = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] != 0) {
                //h = Math.max(h, manhattanDistance(i, tiles[i]));
                h += manhattanDistance(i, tiles[i]);
            }
        }
        return h;
    }

    //return the number of tiles that out of place in goal state.
    
    static int tileOutOfPlaceHeuristic(byte[] tiles) {
        int tileInWrongPlace = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] != 0) {
                if (index(tiles, tiles[i]) != index(goalTiles, tiles[i])) {
                    tileInWrongPlace++;
                }
            }
        }
        return tileInWrongPlace;
    }

    public static void main(String[] args) {

        // This is a harder puzzle than the SO example
       // byte[] initial = {2, 5, 6, 4, 3, 1, 0, 8, 7};

        // This is taken from the SO example.
        //byte [] initial = { 1, 4, 2, 3, 0, 5, 6, 7, 8 };
        //new Puzzle().solve(initial);
        /*while (!list.isEmpty()) {
            State s1 = list.pollFirst();
            State s2 = s1;
            if (!list.isEmpty()) {
                s2 = list.peekFirst();
            }
            System.out.print(getMove(s1, s2) + "\t\t");
            s1.print();

        }*/

    }
    
    public class FGH {
        int g, h, h2;
        public FGH(int g, int h, int h2) {
            this.g = g;
            this.h = h;
            this.h2 = h2;
        }
    }

    public class State {

        final byte[] tiles;    // Tiles left to right, top to bottom.
        final int spaceIndex;   // Index of zero in tiles  
        int g=0;            // Number of moves from start.
        final int h;            // Heuristic value , difference from goal
        final State prev;       // Previous state in solution .

        // A* priority function => F=g+h.
        int priority() {
            return g + h;
        }

        // Build a start state.
        State(byte[] initial) {
            tiles = initial;
            spaceIndex = index(tiles, 0);
            g = 0;
            h = heuristic(tiles);
            prev = null;
        }

        // Build a successor to prev by sliding tile from index.
        State(State prev, int slideFromIndex) {
            tiles = Arrays.copyOf(prev.tiles, prev.tiles.length);
            tiles[prev.spaceIndex] = tiles[slideFromIndex];
            tiles[slideFromIndex] = 0;
            spaceIndex = slideFromIndex;
            g = prev.g + 1;
            h = heuristic(tiles);
            this.prev = prev;
        }

        // Return true if this is the goal state.
        boolean isGoal() {
            return Arrays.equals(tiles, goalTiles);
        }

        // Successor states due to UP, DOWN, RIGHT, and LEFT moves.
        State moveUP() {
            return spaceIndex > 2 ? new State(this, spaceIndex - 3) : null;
        }

        State moveDOWN() {
            return spaceIndex < 6 ? new State(this, spaceIndex + 3) : null;
        }

        State moveLEFT() {
            return spaceIndex % 3 > 0 ? new State(this, spaceIndex - 1) : null;
        }

        State moveRIGHT() {
            return spaceIndex % 3 < 2 ? new State(this, spaceIndex + 1) : null;
        }

        // Print this state.
        void print() {
            System.out.println("p = " + priority() + " = g+h = " + g + "+" + h);
            for (int i = 0; i < 9; i += 3) {
                System.out.println(
                        (tiles[i] == 0 ? " " : tiles[i]) + " "
                        + (tiles[i + 1] == 0 ? " " : tiles[i + 1]) + " "
                        + (tiles[i + 2] == 0 ? " " : tiles[i + 2]));
            }
        }

        // Print the solution chain with start state first.
        void printAll() {
            if (prev != null) {
                prev.printAll();
            }
            System.out.println();
            print();
        }

        void addToList() {
            if (prev != null) {
                prev.addToList();
            }
            add();
        }

        void add() {
            list.add(new State(tiles));
            list2.add(new FGH(new State(tiles).g , tileOutOfPlaceHeuristic(tiles) , new State(tiles).h));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof State) {
                State other = (State) obj;
                return Arrays.equals(tiles, other.tiles);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(tiles);
        }
    }

}
