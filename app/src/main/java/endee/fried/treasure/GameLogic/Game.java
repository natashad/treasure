package endee.fried.treasure.GameLogic;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import endee.fried.treasure.HexMap;
import endee.fried.treasure.UI.Callback;

/**
 * Created by leslie on 05/08/14.
 */
public class Game {
    // todo: change these based on number of players/options
    private final static int HEX_SIZE = 15;
    private final static int MAX_ITEMS = 10;

    public enum State {
        IN_PROGRESS,
        WINNER,
        LOSER
    }

    private final HexMap hexMap;

    private final Random random;
    private final Callback onChange;

    private final List<Player> players;
    private final Map<Integer, Tile> tiles;
    private final Queue<Item> unplacedItemQueue;
    private final List<Action> currentActions;
    private final Set<Integer> itemInhabitedTiles;

    private State state;
    private int treasureTile;
    private int keyTile;
    private int localPlayer;
    private boolean madeMove;


    public Game(int numPlayers, int localPlayer, long seed, Context context, Callback onChange) {
        this.localPlayer = localPlayer;
        this.onChange = onChange;
        state = State.IN_PROGRESS;
        madeMove = false;
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
        tiles = new HashMap<Integer, Tile>();

        int[] tileArray = hexMap.getAllTiles();

        for(int i = 0; i < tileArray.length; i++) {
            tiles.put(tileArray[i], new Tile());
        }


        // place treasure chest and key randomly
        // getRandomTile wont choose these tiles
        // so make them -1 so it can spawn on 0 tile
        keyTile = -1;
        treasureTile = -1;
        treasureTile = getEmptyRandomTile();
        keyTile = getEmptyRandomTile();

        // Add a bunch of items to itemQueue
        // TODO: Shuffle the queue
        // spawnItems until we hit max items
        Item toastItem = new MakeToastItem(context);

        for(int i = 0; i < 25; i++) {
            unplacedItemQueue.add(toastItem);
        }

        for(int i = 0; i < MAX_ITEMS; i++) {
            spawnItem();
        }

        // Discover initial tiles
        discoverNearbyTiles();
    }

    public static int getHexSize() {
        return HEX_SIZE;
    }

    public HexMap getHexMap() {
        return hexMap;
    }

    public Player getLocalPlayer() {
        return players.get(localPlayer);
    }

    public Tile getTile(int tile) {
        return tiles.get(tile);
    }

    public void update() {
        // Play all actions
        for(Action a : currentActions) {
            a.doAction();
        }

        currentActions.clear();

        // Allow player to make a move next turn
        madeMove = false;

        // Check if player has key and is on treasure tile then that player wins
        for(Player p : players) {
            if(p.hasKey() && p.getTile() == treasureTile) {
                state = p.isLocal()? State.WINNER : State.LOSER;
            }
        }

        // TODO: trigger traps

        // Give players item or key on there tile
        // If 2 players are on the same tile add item directly to Queue
        // If 2 players are together and 1 has key, put key on random tile
        for(Player p1 : players) {
            for(Player p2 : players) {
                if(p1 == p2) continue;

                if(p1.getTile() == p2.getTile()) {
                    Item item = tiles.get(p1.getTile()).getItem();

                    if(item != null) {
                        recycleItem(item);
                        itemInhabitedTiles.remove(p1.getTile());
                        tiles.get(p1.getTile()).setItem(null);
                    }

                    if(p1.hasKey() || keyTile == p1.getTile()) {
                        keyTile = getEmptyRandomTile();
                    }
                }
            }

            if(p1.getTile() == keyTile) {
                p1.setHasKey(true);
                keyTile = -1;
            }

            Item item = tiles.get(p1.getTile()).getItem();

            if(item != null) {
                p1.giveItem(item);
                itemInhabitedTiles.remove(p1.getTile());
                tiles.get(p1.getTile()).setItem(null);
            }
        }


        // Spawn next item on queue in random tile if we have less than max items
        if(itemInhabitedTiles.size() < MAX_ITEMS) {
            spawnItem();
        }

        discoverNearbyTiles();


        // Give players there action points
        for(Player p : players) {
            p.giveActionPoint();
        }

        // Tell view to redraw
        onChange.doAction();
    }

    public void movePlayer(int tile) {
        if(!madeMove) {
            Action action = new MoveAction(players.get(localPlayer), tile);
            currentActions.add(action);

            // TODO: Send action to other phones about players action

            madeMove = true;

            if(waitingOnNumOpponent() == 0) {
                update();
            } else {
                onChange.doAction();
            }
        }
    }

    public boolean useItem(int itemIndex) {
        if(!madeMove && players.get(localPlayer).canUseItem(itemIndex)) {
            Action action = new UseItemAction(players.get(localPlayer), this, itemIndex);
            currentActions.add(action);

            // TODO: Send action to other phones about players action

            madeMove = true;

            if(waitingOnNumOpponent() == 0) {
                update();
            } else {
                onChange.doAction();
            }

            return true;
        }

        return false;
    }

    public void recycleItem(Item item) {
        unplacedItemQueue.add(item);
    }

    public int waitingOnNumOpponent() {
        //TODO
        return 0;
    }

    public boolean hasMadeMove() {
        return madeMove;
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
        tiles.get(tile).setItem(unplacedItemQueue.poll());
        itemInhabitedTiles.add(tile);
    }

    private void discoverNearbyTiles() {
        int playerTile = players.get(localPlayer).getTile();

        List<Integer> neighbours = hexMap.getNeighbours(playerTile);

        tiles.get(playerTile).discover();
        for(int i : neighbours) {
            tiles.get(i).discover();
        }
    }
}
