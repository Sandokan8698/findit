package org.damg.findit.topologies;

import com.digitalpebble.stormcrawler.ConfigurableTopology;
import org.damg.findit.topologies.builders.ESCrawlerTopologyBuilder;

public class ESCrawlTopology extends ConfigurableTopology {

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new ESCrawlTopology(), args);
    }

    @Override
    protected int run(String[] args) {
        return submit("crawl", conf, ESCrawlerTopologyBuilder.builder(args));
    }

}
