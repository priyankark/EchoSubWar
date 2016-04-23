package com.tsatsatzu.subwar.audio.data;

import com.tsatsatzu.subwar.audio.logic.AudioConstLogic;
import com.tsatsatzu.subwar.game.data.SWGameDetailsBean;
import com.tsatsatzu.subwar.game.data.SWUserBean;

public class SWInvocationBean
{
    private SWSessionBean   mSession;
    private SWStateBean     mState;
    private SWUserBean        mUser;
    private SWGameDetailsBean   mGame;
    private String              mSpokenText = "";
    private String              mWrittenText = "";
    private String              mRepromptText = "";
    private String              mLargeImage;
    private String              mSmallImage;
    private boolean             mEndSession = false;

    // utility functions

    public void addSound(String mp3)
    {
        addSpoken("<audio src=\""+mp3+"\"/>");
    }
    
    public void addPause()
    {
        addSpoken(AudioConstLogic.SOUND_PAUSE);
        addWritten("\n");
    }
    
    public void addText(String txt)
    {
        if (txt == null)
            return;
        if (getUser().getSubName() != null)
            txt = txt.replace("{ship}", getUser().getSubName());
        if (getUser().getUserName() != null)
            txt = txt.replace("{user}", getUser().getUserName());
        addSpoken(txt);
        addWritten(txt);
    }
    
    public void addSpoken(String txt)
    {
        mSpokenText = mSpokenText.trim() + " " + txt.trim();
    }
    
    public void addWritten(String txt)
    {
        mWrittenText += txt;
    }
    
    // getters and setters
    
    public SWSessionBean getSession()
    {
        return mSession;
    }

    public void setSession(SWSessionBean session)
    {
        mSession = session;
    }

    public SWGameDetailsBean getGame()
    {
        return mGame;
    }

    public void setGame(SWGameDetailsBean game)
    {
        mGame = game;
    }

    public SWUserBean getUser()
    {
        return mUser;
    }

    public void setUser(SWUserBean user)
    {
        mUser = user;
    }

    public String getSpokenText()
    {
        return mSpokenText;
    }

    public void setSpokenText(String spokenText)
    {
        mSpokenText = spokenText;
    }

    public String getWrittenText()
    {
        return mWrittenText;
    }

    public void setWrittenText(String writtenText)
    {
        mWrittenText = writtenText;
    }

    public String getRepromptText()
    {
        return mRepromptText;
    }

    public void setRepromptText(String repromptText)
    {
        mRepromptText = repromptText;
    }

    public String getLargeImage()
    {
        return mLargeImage;
    }

    public void setLargeImage(String largeImage)
    {
        mLargeImage = largeImage;
    }

    public String getSmallImage()
    {
        return mSmallImage;
    }

    public void setSmallImage(String smallImage)
    {
        mSmallImage = smallImage;
    }

    public boolean isEndSession()
    {
        return mEndSession;
    }

    public void setEndSession(boolean endSession)
    {
        mEndSession = endSession;
    }

    public SWStateBean getState()
    {
        return mState;
    }

    public void setState(SWStateBean state)
    {
        mState = state;
    }
    
}
