package integration.runners;


import com.digitalpebble.stormcrawler.ConfigurableTopology;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.Status;
import integration.utils.TopologyRunner;
import org.apache.storm.Config;
import org.apache.storm.topology.TopologyBuilder;
import org.damg.findit.topologies.builders.CrawlerTopologyBuilder;


public class CrawlTopology extends ConfigurableTopology {

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new CrawlTopology(), args);
    }

    @Override
    protected int run(String[] args) {
        return submit("crawl", conf, CrawlerTopologyBuilder.builder());
    }

    @Override
    protected int submit(String name, Config conf, TopologyBuilder builder) {

        // register for serialization with Kryo
        Config.registerSerialization(conf, Metadata.class);
        Config.registerSerialization(conf, Status.class);

        try {
            TopologyRunner.run(name, conf, builder.createTopology());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
