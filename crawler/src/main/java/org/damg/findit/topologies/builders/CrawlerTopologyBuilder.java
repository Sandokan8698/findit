package org.damg.findit.topologies.builders;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.bolt.*;
import com.digitalpebble.stormcrawler.indexing.StdOutIndexer;
import com.digitalpebble.stormcrawler.persistence.StdOutStatusUpdater;
import com.digitalpebble.stormcrawler.spout.MemorySpout;
import com.digitalpebble.stormcrawler.tika.ParserBolt;
import com.digitalpebble.stormcrawler.tika.RedirectionBolt;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class CrawlerTopologyBuilder {

    public static TopologyBuilder builder() {

        TopologyBuilder builder = new TopologyBuilder();

        String[] testURLs = new String[]{"https://www.xvideos.com/"};

        builder.setSpout("spout", new MemorySpout(testURLs));

        builder.setBolt("partitioner", new URLPartitionerBolt()).shuffleGrouping("spout");

        builder.setBolt("fetch", new FetcherBolt()).fieldsGrouping("partitioner", new Fields("key"));

        builder.setBolt("sitemap", new SiteMapParserBolt()).localOrShuffleGrouping("fetch");

        builder.setBolt("feeds", new FeedParserBolt()).localOrShuffleGrouping("sitemap");

        builder.setBolt("parse", new JSoupParserBolt()).localOrShuffleGrouping("feeds");

        builder.setBolt("shunt", new RedirectionBolt()).localOrShuffleGrouping("parse");

        builder.setBolt("tika", new ParserBolt()).localOrShuffleGrouping("shunt", "tika");

        builder.setBolt("index", new StdOutIndexer()).localOrShuffleGrouping("shunt").localOrShuffleGrouping("tika");

        Fields furl = new Fields("url");

        // can also use MemoryStatusUpdater for simple recursive crawls
        builder.setBolt("status", new StdOutStatusUpdater())
                .fieldsGrouping("fetch", Constants.StatusStreamName, furl)
                .fieldsGrouping("sitemap", Constants.StatusStreamName, furl)
                .fieldsGrouping("feeds", Constants.StatusStreamName, furl)
                .fieldsGrouping("parse", Constants.StatusStreamName, furl)
                .fieldsGrouping("tika", Constants.StatusStreamName, furl)
                .fieldsGrouping("index", Constants.StatusStreamName, furl);

        return builder;
    }

    public static StormTopology build() {
        return CrawlerTopologyBuilder.builder().createTopology();
    }
}
