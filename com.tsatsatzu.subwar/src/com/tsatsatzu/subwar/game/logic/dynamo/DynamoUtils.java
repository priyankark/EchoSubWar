/*
 * Copyright 2016 Jo Jaquinta, TsaTsaTzu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tsatsatzu.subwar.game.logic.dynamo;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

// TODO: Auto-generated Javadoc
/**
 * The Class DynamoUtils.
 */
public class DynamoUtils
{
    
    /**
     * Gets the string.
     *
     * @param data the data
     * @param name the name
     * @return the string
     */
    public static String getString(Map<String, AttributeValue> data, String name)
    {
        return getString(data, name, "");
    }

    /**
     * Gets the string.
     *
     * @param data the data
     * @param name the name
     * @param def the def
     * @return the string
     */
    public static String getString(Map<String, AttributeValue> data, String name, String def)
    {
        AttributeValue val = data.get(name);
        if (val == null)
            return def;
        String snum = val.getS();
        if (snum == null)
            return def;
        return snum;
    }
    
    /**
     * Gets the int.
     *
     * @param data the data
     * @param name the name
     * @return the int
     */
    public static int getInt(Map<String, AttributeValue> data, String name)
    {
        return getInt(data, name, 0);
    }
    
    /**
     * Gets the int.
     *
     * @param data the data
     * @param name the name
     * @param def the def
     * @return the int
     */
    public static int getInt(Map<String, AttributeValue> data, String name, int def)
    {
        AttributeValue val = data.get(name);
        if (val == null)
            return def;
        String snum = val.getN();
        if (snum == null)
            return def;
        try
        {
            int inum = Integer.parseInt(snum);
            return inum;
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

    /**
     * Gets the long.
     *
     * @param data the data
     * @param name the name
     * @return the long
     */
    public static long getLong(Map<String, AttributeValue> data, String name)
    {
        return getLong(data, name, 0);
    }

    /**
     * Gets the long.
     *
     * @param data the data
     * @param name the name
     * @param def the def
     * @return the long
     */
    public static long getLong(Map<String, AttributeValue> data, String name, long def)
    {
        AttributeValue val = data.get(name);
        if (val == null)
            return def;
        String snum = val.getN();
        if (snum == null)
            return def;
        try
        {
            long inum = Long.parseLong(snum);
            return inum;
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

}
