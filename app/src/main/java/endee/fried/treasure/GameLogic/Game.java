package endee.fried.treasure.GameLogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import endee.fried.treasure.HexMap;

/**
 * Created by leslie on 05/08/14.
 */
public class Game {
    private final static int HEX_SIZE = 15;
    private final static int MAX_ITEMS = 10;

    public enum State {
        IN_PROGRESS,
        WINNER,
        LOSER
    }

    private State state;

    private HexMap hexMap;

    private List<Player> players;
    private Map<Integer, Tile> tiles;
    private Queue<Item> unplacedItemQueue;
    private List<Action> currentActions;
    private Set<Integer> itemInhabitedTiles;

    private Random random;

    private int treasureTile;
    private int keyTile;
    private int localPlayer;

    public Game(int numPlayers, int localPlayer, long seed) {
        this.localPlayer = localPlayer;
        this.random = new Random(seed);

        hexMap = new HexMap(HEX_SIZE);

        hexMap.generate(random);

        players = new ArrayList<Player>(numPlayers - 1);
        for(int i = 0; i < numPlayers; i++) {
            players.add(new Player(i == localPlayer, hexMap.getStartTile()));
        }

        unplacedItemQueue = new LinkedList<Item>();
        currentActions = new ArrayList<Action>();
        itemInhabitedTiles = new HashSet<Integer>();

        state = State.IN_PROGRESS;

        // place treasure chest and key randomly
        // getRandomTile wont choose these tiles
        // so make them -1 so it can spawn on 0 tile
        keyTile = -1;
        treasureTile = -1;
        treasureTile = getEmptyRandomTile();
        keyTile = getEmptyRandomTile();

        // Add a bunch of items to itemQueue
        // Shuffle the queue
        // spawnItems until we hit max items
        //TODO

    }

    public void update() {
        // Play all actions
        for(Action a : currentActions) {
            a.doAction();
        }

        currentActions.clear();

        // Check if player has key and is on treasure tile then that player wins
        for(Player p : players) {
            if(p.hasKey() && p.getTile() == treasureTile) {
                state = p.isLocal()? State.WINNER : State.LOSER;
            }
        }

        // Give players item or key on there tile
        // If 2 players are on the same tile add item directly to Queue
        // If 2 players are together and 1 has key, put key on random tile
        //TODO


        // Spawn next item on queue in random tile if we have less than max items
        if(itemInhabitedTiles.size() < MAX_ITEMS) {
            spawnItem();
        }


        // Give players there action points
        for(Player p : players) {
            p.giveActionPoint();
        }
    }

    public void movePlayer(int tile) {
        currentActions.add(new MoveAction(players.get(0), this, tile));
    }

    public void recycleItem(Item item) {
        unplacedItemQueue.add(item);
    }

    public int waitingOnNumOpponent() {
        //TODO
        return 0;
    }

    public State getGameState() {
        return state;
    }

    /**
     * Returns a random tile with the following conditions
     *  - will not contain a player
     *  - will not be adjacent to a player
     *  - will not be the start tile
     *  - will not contain the key, treasure, or an item
     *
     * @return tile index
     */
    private int getEmptyRandomTile() {
        int[] tilesArray = hexMap.getAllTiles();
        List<Integer> unoccupiedTiles = new ArrayList<Integer>(tilesArray.length);

        List<Integer> occupiedTiles = new ArrayList<Integer>();

        occupiedTiles.add(hexMap.getStartTile());
        occupiedTiles.add(keyTile);
        occupiedTiles.add(treasureTile);

        occupiedTiles.addAll(itemInhabitedTiles);

        for(Player p : players) {
            occupiedTiles.add(p.getTile());
            occupiedTiles.addAll(hexMap.getNeighbours(p.getTile()));
        }

        for(int i = 0; i < tilesArray.length; i++) {
            if(!occupiedTiles.contains(tilesArray[i])) {
                unoccupiedTiles.add(tilesArray[i]);
            }
        }

        return unoccupiedTiles.get(random.nextInt(unoccupiedTiles.size()));
    }

    private void spawnItem() {
        int tile = getEmptyRandomTile();
        tiles.get(tile).item = unplacedItemQueue.poll();
        itemInhabitedTiles.add(tile);
    }

}
