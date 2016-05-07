package com.tsatsatzu.subwar.audio.logic;

import java.util.ArrayList;
import java.util.List;

import com.tsatsatzu.subwar.audio.data.SWInvocationBean;
import com.tsatsatzu.subwar.game.data.SWGameDetailsBean;
import com.tsatsatzu.subwar.game.data.SWOperationBean;
import com.tsatsatzu.subwar.game.data.SWPingBean;
import com.tsatsatzu.subwar.game.data.SWPositionBean;
import com.tsatsatzu.subwar.game.logic.GameConstLogic;

public class PlayLogic
{
    static void doStartGame(SWInvocationBean ssn)
            throws SWAudioException
    {
        InvocationLogic.game(ssn, SWOperationBean.ENTER_GAME);
        ssn.addSound(AudioConstLogic.SOUND_SHIP_LAUNCH);
        ssn.addText("We�ve launched!");
        ssn.getState().setState(AudioConstLogic.STATE_GAME_BASE);
        describeGame(ssn);
    }

    static void describeGame(SWInvocationBean ssn)
    {
        SWGameDetailsBean game = ssn.getGame();
        SWPositionBean pos = ssn.getGame().getUserPosition();
        if (pos == null)
        {
            ssn.addSound(AudioConstLogic.SOUND_EXPLOSION);
            ssn.addText("Oh, no!");
            ssn.addText("We weren't quick enough.");
            ssn.addText("We were sunk.");
            ssn.getState().setState(AudioConstLogic.STATE_PRE_GAME);
            return;
        }
        describeLocation(ssn, game, pos);
        describeTorpedos(ssn, pos);
        describeSoundings(ssn);
    }

    private static void describeLocation(SWInvocationBean ssn,
            SWGameDetailsBean game, SWPositionBean pos)
    {
        if ((pos.getLattitude() == 0) && (pos.getLongitude() == 0))
            ssn.addText("{Ship} is in the exact mid point of the straits");
        else
        {
            if (pos.getLattitude() == game.getNorth())
                ssn.addText("{Ship} is on the most northerly edge of the straits");
            else if (pos.getLattitude() == game.getSouth())
                ssn.addText("{Ship} is on the most southerly edge of the straits");
            else if (pos.getLattitude() < 0)
            {
                int fromNorth = -(game.getNorth() - pos.getLattitude());
                if (fromNorth < -game.getNorth()/2)
                    ssn.addText("{Ship} is "+fromNorth+" kilometers from the Northern end of the straits");
                else
                    ssn.addText("{Ship} is "+(-pos.getLattitude())+" kilometers North of the strait's midpoint");
            }
            else if (pos.getLattitude() > 0)
            {
                int fromSouth = (game.getSouth() - pos.getLattitude());
                if (fromSouth < game.getSouth()/2)
                    ssn.addText("{Ship} is "+fromSouth+" kilometers from the Southern end of the straits");
                else
                    ssn.addText("{Ship} is "+(pos.getLattitude())+" kilometers South of the strait's midpoint");
            }
            else //if (pos.getLattitude() == 0)
                ssn.addText("{Ship} is");
            if (pos.getLongitude() == 0)
                ssn.addText(".");
            else
            {
                ssn.addText("and");
                if (pos.getLongitude() == game.getWest())
                    ssn.addText("right next to the Western coast.");
                else if (pos.getLongitude() == game.getEast())
                    ssn.addText("right next to the East coast.");
                else if (pos.getLongitude() < 0)
                {
                    int fromWest = -(game.getWest() - pos.getLongitude());
                    if (fromWest < -game.getWest()/2)
                        ssn.addText(""+fromWest+" kilometers from the Western coast.");
                    else
                        ssn.addText(""+(-pos.getLongitude())+" kilometers West of the strait's midpoint.");
                }
                else if (pos.getLongitude() > 0)
                {
                    int fromEast = (game.getEast() - pos.getLongitude());
                    if (fromEast < game.getEast()/2)
                        ssn.addText(""+fromEast+" kilometers from the Eastern coast");
                    else
                        ssn.addText(""+(pos.getLongitude())+" kilometers East of the strait's midpoint.");
                }
            }
        }
    }

    private static void describeTorpedos(SWInvocationBean ssn,
            SWPositionBean pos)
    {
        if (ssn.getState().getLastReportedTorpedos() != pos.getTorpedoes())
        {
            if (pos.getTorpedoes() == 0)
                ssn.addText("We are out of torpedos.");
            else if (pos.getTorpedoes() == 1)
                ssn.addText("We are down to a single torpedo.");
            else if (pos.getTorpedoes() == GameConstLogic.MAX_TORPEDOES)
                ssn.addText("We have a full complement of "+pos.getTorpedoes()+" torpedos.");
            else
                ssn.addText("We have "+pos.getTorpedoes()+" torpedos left.");
            ssn.getState().setLastReportedTorpedos(pos.getTorpedoes());
        }
    }

    public static void describeSoundings(SWInvocationBean ssn)
    {
        SWPositionBean pos = ssn.getGame().getUserPosition();
        List<String> listens = new ArrayList<>();
        List<String> pings = new ArrayList<>();
        List<String> pongs = new ArrayList<>();
        List<String> booms = new ArrayList<>();
        for (SWPingBean ping : pos.getSoundings())
            switch (ping.getType())
            {
                case SWPingBean.LISTEN:
                    listens.add(describePing(ping));
                    break;
                case SWPingBean.PING:
                    pings.add(describePing(ping));
                    break;
                case SWPingBean.PONG:
                    pongs.add(describePing(ping));
                    break;
                case SWPingBean.BOOM:
                    booms.add(describePing(ping));
                    break;
            }
        if (listens.size() > 0)
        {
            ssn.addSound(AudioConstLogic.SOUND_LISTEN_SHIP);
            ssn.addText("We can hear a ship's screw "+ResponseLogic.andList(listens)+".");
        }
        if (pings.size() == 1)
            ssn.addText("Our sonar hear's a signal return "+ResponseLogic.andList(pings)+".");
        else if (pings.size() > 1)
            ssn.addText("Our sonar hear's signal returns "+ResponseLogic.andList(pings)+".");
        if (pongs.size() == 1)
            ssn.addText("Someone else is pinging us "+ResponseLogic.andList(pongs)+".");
        else if (pongs.size() > 1)
            ssn.addText("Other people are pinging us "+ResponseLogic.andList(pongs)+".");
        if (booms.size() == 1)
            ssn.addText("Something just exploded "+ResponseLogic.andList(booms)+".");
        else if (booms.size() > 1)
            ssn.addText("I hear explosions "+ResponseLogic.andList(booms)+".");
        pos.getSoundings().clear();
    }
    
    private static String describePing(SWPingBean ping)
    {
        StringBuffer txt = new StringBuffer();
        if (ping.getDirection() == 0)
            txt.append("right on top of us");
        else
        {
            txt.append(SWPingBean.DIRECTIONS[ping.getDirection()]);
            txt.append(" at about "+(int)(ping.getDistance())+" kilometers");
        }
        if (ping.getAltitude() < 0)
            txt.append(" and slightly below us");
        else if (ping.getAltitude() < 0)
            txt.append(" and slightly above us");
        return txt.toString();
    }
}
