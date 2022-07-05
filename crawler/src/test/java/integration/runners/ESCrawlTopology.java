package integration.runners;


import com.digitalpebble.stormcrawler.ConfigurableTopology;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.elasticsearch.metrics.MetricsConsumer;
import com.digitalpebble.stormcrawler.persistence.Status;
import integration.utils.TopologyRunner;
import org.apache.storm.Config;
import org.apache.storm.metric.LoggingMetricsConsumer;
import org.apache.storm.topology.TopologyBuilder;
import org.damg.findit.topologies.builders.ESCrawlerTopologyBuilder;


/**
 * Dummy topology to play with the spouts and bolts
 */
public class ESCrawlTopology extends ConfigurableTopology {

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new ESCrawlTopology(), args);
    }

    @Override
    protected int run(String[] args) {
        return submit("crawl", conf, ESCrawlerTopologyBuilder.builder(args));
    }

    @Override
    protected int submit(String name, Config conf, TopologyBuilder builder) {

        // register for serialization with Kryo
        Config.registerSerialization(conf, Metadata.class);
        Config.registerSerialization(conf, Status.class);

        conf.registerMetricsConsumer(MetricsConsumer.class);
        conf.registerMetricsConsumer(LoggingMetricsConsumer.class);

        try {
            TopologyRunner.run(name, conf, builder.createTopology());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
