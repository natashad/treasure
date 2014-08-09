package endee.fried.treasure.GameLogic;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by leslie on 05/08/14.
 */
public class HexMap {
    private final static float VARIANCE = 0.35f;

    private final boolean[] map;
    private final int start;
    private final int size;
    private int numTiles;


    public HexMap(int _size) {
        size = _size;
        start = size * size / 2;
        numTiles = 0;
        map = new boolean[size * size];
    }

    public void generate(Random rand) {
        resetMap();

        for(int i = 0; i < map.length; i++) {
            if(rand.nextFloat() < VARIANCE && i != start) {
                map[i] = false;
            }
        }

        removeUnreachable();
        removeDeadEnds();
    }

    public int getStartTile() {
        return start;
    }

    public int getNumTiles() {
        return numTiles;
    }

    public int[] getAllTiles() {
        int[] tiles = new int[numTiles];
        int index = 0;

        for(int i = 0; i < map.length; i++) {
            if(map[i]) tiles[index++] = i;
        }

        return tiles;
    }

    // Assumes each tile has a diameter of 2
    public float[] getLocation(int tile) {
        float[] loc = new float[2];
        int row = tile / size;
        int col = tile % size;

        loc[0] = col + 0.5f;
        loc[1] = row + 0.5f;

        if(row%2 == 1) loc[0] += 0.5f;

        return loc;
    }

    public List<Integer> getNeighbours(int tile) {
        final int[][][] offsets = {
            { {+1,  0}, { 0, -1}, {-1, -1},
              {-1,  0}, {-1, +1}, { 0, +1} },
            { {+1,  0}, {+1, -1}, { 0, -1},
              {-1,  0}, { 0, +1}, {+1, +1} }
        };

        List<Integer> neighbours = new ArrayList<Integer>();

        int row = tile / size;
        int col = tile % size;

        for(int i = 0; i < 6; i++) {
            int newRow = row + offsets[row%2][i][1];
            int newCol = col + offsets[row%2][i][0];
            int newTile = newRow * size + newCol;

            if(newRow >= 0 && newCol >= 0 && newRow < size && newCol < size && map[newTile]) {
                neighbours.add(newTile);
            }
        }

        return neighbours;
    }

    public void printMap() {
        for(int row = 0; row < size; row++){
            String rowString = "";
            for(int col = 0; col < size; col++) {
                int tile = row * size + col;
                char c = map[tile]? 'x' : '.';

                if(row % 2 == 0) rowString += c + " ";
                else rowString += " " + c;
            }
            Log.d("", "" + ":" + rowString);
        }

        Log.d("", ""+numTiles + " total tiles");
    }

    private void removeUnreachable() {
        List<Integer> toSearch = new ArrayList<Integer>();
        boolean[] searched = new boolean[map.length];

        toSearch.add(start);

        while(!toSearch.isEmpty()) {
            int tile = toSearch.get(0);

            toSearch.remove(0);

            if(!map[tile]|| searched[tile]) continue;

            toSearch.addAll(getNeighbours(tile));

            searched[tile] = true;
        }

        for(int i = 0; i < map.length; i++) {
            if(!searched[i]) {
                map[i] = false;
                numTiles--;
            }
        }
    }

    private void removeDeadEnds() {
        boolean removed = true;

        while(removed) {
            removed = false;
            for(int i = 0; i < map.length; i++) {
                if(!map[i] || i == start) continue;
                if(getNeighbours(i).size() == 1) {
                    removed = true;
                    map[i] = false;
                    numTiles--;
                }
            }
        }
    }

    private void resetMap() {
        numTiles = map.length;
        for(int i = 0; i < map.length; i++) {
            map[i] = true;
        }
    }
}
