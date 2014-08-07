package endee.fried.treasure.GameLogic;

import java.util.List;
import java.util.Map;

import endee.fried.treasure.HexMap;

/**
 * Created by leslie on 05/08/14.
 */
public class Game {
    private Player localPlayer;
    private List<Player> otherPlayers;
    private HexMap hexMap;
    private Map<Integer, Tile> tiles;
}
