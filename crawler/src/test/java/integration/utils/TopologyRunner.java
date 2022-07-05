package integration.utils;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.jetbrains.annotations.Nullable;

public class TopologyRunner {

    public static void run(String id, StormTopology topology, @Nullable Long timeout) throws Exception {
        TopologyRunner.run(id, new Config(), topology, timeout);
    }

    public static void run(String id, Config config, StormTopology topology) throws Exception {
        TopologyRunner.run(id, config, topology, null);
    }


    public static void run(String id, Config config, StormTopology topology, @Nullable Long timeout) throws Exception {
        LocalCluster localCluster = new LocalCluster();

        localCluster.submitTopology(id, config, topology);

        try {
            Thread.sleep(timeout == null ? 150000 : timeout);
        } catch (InterruptedException ignore) {
        }

        localCluster.killTopology(id);
        localCluster.shutdown();
    }
}
