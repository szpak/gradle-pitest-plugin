package pitest.sample.multimodule.forreport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForReport {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    public ForReport(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String notUsed() {
        return "notUsed";
    }

    public String readProperty() {
        logger.info("to fail on broken dependency");
        return "important";
    }
}
