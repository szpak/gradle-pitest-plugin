package pitest.sample.multimodule.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shared {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    public Shared(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String readProperty() {
        logger.info("to fail on broken dependency");
        return "important";
    }
}
