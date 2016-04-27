package com.tsatsatzu.subwar.game.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.tsatsatzu.subwar.game.data.SWGameBean;
import com.tsatsatzu.subwar.game.data.SWGameDetailsBean;
import com.tsatsatzu.subwar.game.data.SWPingBean;
import com.tsatsatzu.subwar.game.data.SWPositionBean;
import com.tsatsatzu.subwar.game.data.SWUserBean;
import com.tsatsatzu.subwar.game.logic.ai.IComputerPlayer;
import com.tsatsatzu.subwar.game.logic.ai.SimplePlayer;

public class GameLogic
{
    private static Random mRND = new Random();
    private static List<SWGameBean> mGames = new ArrayList<>();

    public static SWGameDetailsBean getGameDetails(int inGame, String userID)
    {
        if ((inGame < 0) || (inGame >= mGames.size()))
            return null;
        SWGameBean game = mGames.get(inGame);
        SWGameDetailsBean details = new SWGameDetailsBean();
        details.setEast(game.getEast());
        details.setWest(game.getWest());
        details.setNorth(game.getNorth());
        details.setSouth(game.getSouth());
        details.setMaxDepth(game.getMaxDepth());
        details.setUserPosition(game.getShips().get(userID));
        return details;
    }

    public static String joinGame(SWUserBean user)
    {
        if (user.getInGame() >= 0)
            return "already in game";
        // find a game with room
        synchronized (mGames)
        {
            int game = 0;
            while (game < mGames.size())
                if (mGames.get(game).getShips().size() < GameConstLogic.MAX_SHIPS_PER_GAME)
                    break;
            if (game == mGames.size())
                mGames.add(newGame());
            updateGame(mGames.get(game));
            doJoinGame(mGames.get(game), user.getUserID());
            user.setInGame(game);
        }
        return null;
    }

    public static String move(SWUserBean user, int dLat, int dLon, int dDep)
    {
        if (user.getInGame() < 0)
            return "not in game";
        SWGameBean game = mGames.get(user.getInGame());
        String err = doMoveShip(user.getUserID(), dLat, dLon, dDep, game);
        return err;
    }

    public static String listen(SWUserBean user)
    {
        if (user.getInGame() < 0)
            return "not in game";
        SWGameBean game = mGames.get(user.getInGame());
        updateGame(game);
        SWPositionBean pos = game.getShips().get(user.getUserID());
        if (pos == null)
            return "you have been destroyed";
        List<SWPingBean> pings = doListen(user.getUserID(), game, System.currentTimeMillis());
        pos.getSoundings().addAll(pings);
        return null;
    }

    public static String ping(SWUserBean user)
    {
        if (user.getInGame() < 0)
            return "not in game";
        SWGameBean game = mGames.get(user.getInGame());
        updateGame(game);
        SWPositionBean pos = game.getShips().get(user.getUserID());
        if (pos == null)
            return "you have been destroyed";
        List<SWPingBean> pings = doPing(user.getUserID(), game, System.currentTimeMillis());
        pos.getSoundings().addAll(pings);
        return null;
    }
    
    private static SWGameBean newGame()
    {
        SWGameBean game = new SWGameBean();
        game.setNorth(-GameConstLogic.GAME_HEIGHT/2);
        game.setSouth(GameConstLogic.GAME_HEIGHT/2);
        game.setEast(GameConstLogic.GAME_WIDTH/2);
        game.setWest(-GameConstLogic.GAME_WIDTH/2);
        game.setMaxDepth(GameConstLogic.GAME_DEPTH);
        updateGame(game);
        return game;
    }

    public static void doJoinGame(SWGameBean game, String id)
    {
        synchronized (game)
        {
            SWPositionBean pos = new SWPositionBean();
            pos.setDepth(0);
            pos.setLongitude(mRND.nextInt(GameConstLogic.GAME_WIDTH/2) - GameConstLogic.GAME_WIDTH/4);
            pos.setLattitude(mRND.nextInt(GameConstLogic.GAME_HEIGHT/2) - GameConstLogic.GAME_HEIGHT/4);
            pos.setLastMove(System.currentTimeMillis());
            pos.setTorpedoes(GameConstLogic.MAX_TORPEDOES);
            game.getShips().put(id, pos);
        }
    }

    public static void doLeaveGame(SWGameBean game, String id)
    {
        synchronized (game)
        {
            if (game.getAI().containsKey(id))
                game.getAI().get(id).term(game, id);
            game.getShips().remove(id);
            game.getAI().remove(id);
        }
    }

    public static int doTorpedo(String id, SWGameBean game, Integer fireDLon,
            Integer fireDLat, long now)
    {
        SWPositionBean pos = game.getShips().get(id);
        if (pos.getTorpedoes() <= 0)
            return 0;
        int tLon = pos.getLongitude();
        int tLat = pos.getLattitude();
        for (int i = 0; i < GameConstLogic.TORPEDO_RANGE; i++)
        {
            tLon += fireDLon;
            tLat += fireDLat;
            System.out.println("Torpedo at "+tLon+","+tLat+","+pos.getDepth());
            List<String> hits = findShipsAt(game, tLon, tLat, pos.getDepth());
            if (hits.size() > 0)
            {
                System.out.println("Torpedo hits "+hits);
                for (String hit : hits)
                    doDie(game, hit);
                doBoom(game, tLon, tLat, pos.getDepth(), now);
                return hits.size();
            }
        }
        System.out.println("Torpedo misses");
        return 0;
    }

    private static List<String> findShipsAt(SWGameBean game, int tLon, int tLat, int depth)
    {
        List<String> hits = new ArrayList<>();
        for (String id : game.getShips().keySet())
        {
            SWPositionBean pos = game.getShips().get(id);
            if ((pos.getLongitude() == tLon) && (pos.getLattitude() == tLat) && (pos.getDepth() == depth))
                hits.add(id);
        }
        return hits;
    }

    private static void doDie(SWGameBean game, String id)
    {
        doLeaveGame(game, id);
    }

    public static void doBoom(SWGameBean game, int lon, int lat, int dep, long now)
    {
        for (String shipID : game.getShips().keySet())
        {
            SWPositionBean shipPos = game.getShips().get(shipID);
            SWPingBean boom = makePing(shipPos, lon, lat, dep, SWPingBean.BOOM, now);
            if (boom.getDistance() <= GameConstLogic.TORPEDO_RANGE)
                shipPos.getSoundings().add(boom);
        }
    }

    public static List<SWPingBean> doPing(String id, SWGameBean game, long now)
    {
        List<SWPingBean> pings = new ArrayList<>();
        SWPositionBean pos = game.getShips().get(id);
        if (pos == null)
            return pings;
        for (String shipID : game.getShips().keySet())
        {
            if (shipID.equals(id))
                continue;
            SWPositionBean shipPos = game.getShips().get(shipID);
            SWPingBean ping = makePing(pos, shipPos, SWPingBean.PING, now);
            if (ping.getDistance() <= GameConstLogic.PING_RANGE)
            {
                pings.add(ping);
                SWPingBean pong = makePing(shipPos, pos, SWPingBean.PONG, now);
                shipPos.getSoundings().add(pong);
            }
        }
        return pings;
    }

    public static List<SWPingBean> doListen(String id, SWGameBean game, long now)
    {
        List<SWPingBean> pings = new ArrayList<>();
        SWPositionBean pos = game.getShips().get(id);
        if (pos == null)
            return pings;
        for (String shipID : game.getShips().keySet())
        {
            if (shipID.equals(id))
                continue;
            SWPositionBean shipPos = game.getShips().get(shipID);
            SWPingBean ping = makePing(pos, shipPos, SWPingBean.LISTEN, now);
            if (ping.getDistance() <= GameConstLogic.LISTEN_RANGE)
                pings.add(ping);
        }
        return pings;
    }

    private static SWPingBean makePing(SWPositionBean pinger,
            SWPositionBean pingee, int type, long time)
    {
        return makePing(pinger, pingee.getLongitude(), pingee.getLattitude(), pingee.getDepth(), type, time);
    }
    
    private static SWPingBean makePing(SWPositionBean pinger,
            int pingeeLon, int pingeeLat, int pingeeDep, int type, long time)
    {
        int deltaLon = pingeeLon - pinger.getLongitude();
        int deltaLat = pingeeLat - pinger.getLattitude();
        double a = SWPingBean.deltaToAngle(deltaLon, deltaLat);
        int dir = SWPingBean.angleToDirection(a);
        double dist = Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);
        SWPingBean ping = new SWPingBean();
        ping.setType(type);
        ping.setTime(time);
        ping.setDirection(dir);
        ping.setDistance(dist);
        if (pinger.getDepth() < pingeeDep)
            ping.setAltitude(SWPingBean.DOWN);
        else if (pinger.getDepth() > pingeeDep)
            ping.setAltitude(SWPingBean.UP);
        else
            ping.setAltitude(SWPingBean.LEVEL);
        System.out.println("Pinger="+pinger.getLongitude()+","+pinger.getLattitude()
            +", pingee="+pingeeLon+","+pingeeLat
            +", delta="+deltaLon+","+deltaLat
            +", a="+(int)(a*128/Math.PI)+", dir="+dir+" "+SWPingBean.DIRECTIONS[dir]
            +", dist="+dist);
        return ping;
    }

    public static String doMoveShip(String id, int dLat, int dLon, int dDep,
            SWGameBean game)
    {
        SWPositionBean pos = game.getShips().get(id);
        if (pos == null)
            return "no in game anymore";
        int newLat = pos.getLattitude() + dLat;
        int newLon = pos.getLongitude() + dLon;
        int newDep = pos.getDepth() - dDep; // -ve = sink, which we actually increment for
        if ((newLat < game.getNorth()) || (newLat > game.getSouth()))
            return "out of bounds";
        if ((newLon < game.getWest()) || (newLon > game.getEast()))
            return "out of bounds";
        if ((newDep < 0) || (newDep > game.getMaxDepth()))
            return "out of bounds";
        pos.setLattitude(newLat);
        pos.setLongitude(newLon);
        pos.setDepth(newDep);
        return null;
    }
    
    // AI stuff
    
    private static int mAICount = 0;
    private static final String PREFIX_AI = "ai://";
    
    private static void updateGame(SWGameBean game)
    {
        long now = System.currentTimeMillis();
        // check if no players
        if (game.getShips().size() == game.getAI().size())
        {
            // just mark moved and return
            for (String id : game.getAI().keySet())
            {
                SWPositionBean pos = game.getShips().get(id);
                pos.setLastMove(now);
            }           
            return;
        }
        // check # of AIs
        while (game.getAI().size() < GameConstLogic.MAX_AIS_PER_GAME)
        {
            String id = PREFIX_AI+(++mAICount);
            doJoinGame(game, id);
            IComputerPlayer ai = new SimplePlayer();
            game.getAI().put(id, ai);
            ai.init(game, id);
        }
        // move ships
        for (String id : game.getAI().keySet().toArray(new String[0]))
        {
            IComputerPlayer ai = game.getAI().get(id);
            if (ai == null)
                continue; // killed
            SWPositionBean pos = game.getShips().get(id);
            if (pos == null)
                ai.term(game, id);
            else
            {
                long tick = pos.getLastMove() + GameConstLogic.AI_MOVE_TICK;
                while (tick < now)
                {
                    ai.move(game, id, tick);
                    tick += GameConstLogic.AI_MOVE_TICK;
                }
            }
        }
    }
    
    // testing stuff

    public static void testResetToSeed(long seed)
    {
        mRND = new Random(seed);
    }

    public static void testAIMove()
    {
        long tick = System.currentTimeMillis() - GameConstLogic.AI_MOVE_TICK - 1;
        for (SWGameBean game : mGames)
        {
            for (String id : game.getAI().keySet())
            {
                SWPositionBean pos = game.getShips().get(id);
                if (pos != null)
                    pos.setLastMove(tick);
            }
            updateGame(game);
        }
    }
}