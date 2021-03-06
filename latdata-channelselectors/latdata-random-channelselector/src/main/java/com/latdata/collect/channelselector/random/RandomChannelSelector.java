/**
 * Copyright 2017 LatData
 * 
 * This file is part of latdata-collect (LDPlatform).
 *
 * latdata-collect is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * latdata-collect is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with info at latdata dot com
*/

package com.latdata.collect.channelselector;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.AbstractChannelSelector;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomChannelSelector extends AbstractChannelSelector {

    private Logger logger;
    private int numChannels;
    private int lastUsedChannel;
    private int totalChannels;

    /**
     * Constructor.
     */
    public RandomChannelSelector() {
        logger = Logger.getLogger(RandomChannelSelector.class);
    } // RandomChannelSelector

    @Override
    public void setChannels(List<Channel> channels) {
        super.setChannels(channels);
        totalChannels = channels.size();
        logger.debug("setChannels");
    } // setChannels

    @Override
    public void configure(Context context) {
    } // configure

    @Override
    public List<Channel> getOptionalChannels(Event event) {

        logger.debug("Returning empty optional channels");

        return new ArrayList<Channel>();

    } // getOptionalChannels

    @Override
    public List<Channel> getRequiredChannels(Event event) {
        Random random = new Random();
        int index = random.nextInt(totalChannels);
        List<Channel> res = new ArrayList<Channel>(1);
        Channel channel = getAllChannels().get(index);
        res.add(channel);
        logger.debug("Returning " + channel.getName() + " channel");
        return res;
    } // getRequiredChannels

} // RandomChannelSelector
