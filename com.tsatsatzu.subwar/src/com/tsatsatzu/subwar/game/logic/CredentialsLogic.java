package com.tsatsatzu.subwar.game.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * Application secrets are kept in a AwsCredentials.properties file in the root of the source
 * tree. This is not checked into source control. Several values are contained there.
 * Your AWS Credentials from http://aws.amazon.com/security-credentials:
 * secretKey=
 * accessKey=
 * One or more API keys may be specified here in a comma deliminated list
 * apiKeys=
 */

public class CredentialsLogic
{
    private static final String SYSTEM_SECRETS = "AwsCredentials.properties";
    private static Properties mCredentials = null;
    
    public static String getProperty(String key)
    {
        return getProperty(key, null);
    }
    
    public static String getProperty(String key, String defaultValue)
    {
        // This section is synchronized to ensure that we don't double-load the values
        // in case they are called from multiple threads.
        synchronized (CredentialsLogic.class)
        {
            if (mCredentials == null)
            {
                try
                {
                    mCredentials = new Properties();
                    ClassLoader loader = CredentialsLogic.class.getClassLoader();
                    InputStream is = loader.getResourceAsStream(SYSTEM_SECRETS);
                    mCredentials.load(is);
                    is.close();
                }
                catch (IOException e)
                {
                    // This should never happen. If it does, there is a deployment error.
                    // The system secret file is not checked in. If you have cloned this project, you
                    // need to set up your own one.
                    throw new IllegalStateException("Failure to load system secrets from "+SYSTEM_SECRETS, e);
                }
            }
        }
        if (mCredentials.containsKey(key))
            return mCredentials.getProperty(key);
        if (System.getProperties().containsKey(key))
            return System.getProperties().getProperty(key);
        return defaultValue;
    }

    public static boolean validateAPIKey(String credentials)
    {
        if (credentials == null)
            return false;
        String apiKeys = getProperty("apiKeys");
        if (apiKeys == null)
        {
            // This should never happen. If it does, there is a deployment error, API keys has not been set.
            throw new IllegalStateException("No API keys set in system secrets");
        }
        return apiKeys.indexOf(credentials) >= 0;
    }
}
