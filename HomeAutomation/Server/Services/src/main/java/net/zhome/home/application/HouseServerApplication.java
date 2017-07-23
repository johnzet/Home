package net.zhome.home.application;

import net.zhome.home.job.sensorPoller.SensorPollerManager;
import net.zhome.home.persistence.repository.PersistenceJPAConfig;
import net.zhome.home.util.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableAutoConfiguration
@EntityScan(basePackages =            {"net.zhome.home.persistence.model"} )
@EnableJpaRepositories(basePackages = {"net.zhome.home.persistence.repository"})
@ComponentScan(basePackages =         {"net.zhome.home.service", "net.zhome.home.job.sensorPoller"})
@Import(PersistenceJPAConfig.class)
public class HouseServerApplication extends SpringBootServletInitializer {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    private SensorPollerManager sensorPollerManager;

    @Autowired
    public void setSensorPollerMnaager(SensorPollerManager sensorPollerManager) {
        this.sensorPollerManager = sensorPollerManager;
        this.sensorPollerManager.start();
    }


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(HouseServerApplication.class);
    }

    public static void main(String[] args) throws Exception {
        ZLogger log = ZLogger.getLogger(HouseServerApplication.class);
        if (! createDirectories()) {
            log.error("Fatal error - exiting");
            System.exit(1);
        }

        ApplicationContext ctx = new AnnotationConfigApplicationContext(PersistenceJPAConfig.class);
        SpringApplication.run(HouseServerApplication.class, args);
    }

    private static boolean createDirectories() {
        try {
            java.io.File logDir = new java.io.File("/var/HouseServer/log");
            logDir.mkdirs();
        }
        catch (Throwable t) {
            ZLogger log = ZLogger.getLogger(HouseServerApplication.class);
            log.error("Failed to create log directory", t);
            return false;
        }
        return true;
    }

}

