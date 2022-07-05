package org.damg.findit.topologies.builders;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.bolt.*;
import com.digitalpebble.stormcrawler.elasticsearch.bolt.DeletionBolt;
import com.digitalpebble.stormcrawler.elasticsearch.bolt.IndexerBolt;
import com.digitalpebble.stormcrawler.elasticsearch.metrics.StatusMetricsBolt;
import com.digitalpebble.stormcrawler.elasticsearch.persistence.AggregationSpout;
import com.digitalpebble.stormcrawler.elasticsearch.persistence.StatusUpdaterBolt;
import com.digitalpebble.stormcrawler.spout.FileSpout;
import com.digitalpebble.stormcrawler.util.URLStreamGrouping;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class ESCrawlerTopologyBuilder {

    public static TopologyBuilder builder(String... params) {

        TopologyBuilder builder = new TopologyBuilder();
        int numWorkers = 1;


        // set to the real number of shards ONLY if es.status.routing is set to
        // true in the configuration
        int numShards = 1;

        Fields key = new Fields("url");


        builder.setSpout("filespout", new FileSpout(params[0], "*", true));

        builder.setBolt("filter", new URLFilterBolt()).localOrShuffleGrouping("filespout", Constants.StatusStreamName);


        builder.setSpout("spout", new AggregationSpout(), numShards);

        builder.setBolt("status_metrics", new StatusMetricsBolt()).shuffleGrouping("spout");

        builder.setBolt("partitioner", new URLPartitionerBolt(), numWorkers).shuffleGrouping("spout");

        builder.setBolt("fetch", new FetcherBolt(), 3)
                 .setNumTasks(3)
                 .fieldsGrouping("partitioner", new Fields("key"));

        builder.setBolt("sitemap", new SiteMapParserBolt(), numWorkers).localOrShuffleGrouping("fetch");

        builder.setBolt("parse", new JSoupParserBolt(), numWorkers).localOrShuffleGrouping("sitemap");

      /*  builder.setBolt("shunt", new RedirectionBolt()).localOrShuffleGrouping("parse");*/

     /*   builder.setBolt("tika", new ParserBolt()).localOrShuffleGrouping("shunt", "tika");*/

        builder.setBolt("indexer", new IndexerBolt(), numWorkers)
               .localOrShuffleGrouping("parse")
               /* .localOrShuffleGrouping("shunt")*/
                /*.localOrShuffleGrouping("tika")*/
        ;

        builder.setBolt("status", new StatusUpdaterBolt(), numWorkers)
                .fieldsGrouping("fetch", Constants.StatusStreamName, key)
                .fieldsGrouping("sitemap", Constants.StatusStreamName, key)
                .fieldsGrouping("parse", Constants.StatusStreamName, key)
              /*  .fieldsGrouping("tika", Constants.StatusStreamName, key)*/
                .fieldsGrouping("indexer", Constants.StatusStreamName, key)
                .customGrouping("filter", Constants.StatusStreamName, new URLStreamGrouping())
                .fieldsGrouping("filespout", Constants.StatusStreamName, key);

        builder.setBolt("deleter", new DeletionBolt(), numWorkers).localOrShuffleGrouping("status",
                Constants.DELETION_STREAM_NAME);

        return builder;

    }

    public static StormTopology build() {
        return ESCrawlerTopologyBuilder.builder().createTopology();
    }

}
