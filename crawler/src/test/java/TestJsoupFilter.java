import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.parse.JSoupFilters;
import com.digitalpebble.stormcrawler.parse.ParseData;
import com.digitalpebble.stormcrawler.parse.ParseResult;
import org.apache.commons.io.IOUtils;
import org.apache.storm.Config;
import org.apache.storm.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.digitalpebble.stormcrawler.util.ConfUtils.extractConfigElement;

public class TestJsoupFilter {

    @Test
    public void shouldParseXvideos() throws IOException {

        final String url = "https://www.xvideos.com/";
        Config conf = new Config();

        Map<String, Object> defaultSCConfig = Utils.findAndReadConfigFile("filters/jsoup/xpath-conf.yaml", false);
        conf.putAll(extractConfigElement(defaultSCConfig));

        JSoupFilters filters = JSoupFilters.fromConf(conf);
        byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream("sites/xvideos.html"));

        ParseResult parse = new ParseResult();

        Document doc = Jsoup.parse(new String(content), url);

        filters.filter(url, content, doc, parse);


    }
}
