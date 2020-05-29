package net.zhome.home.job.sensorPoller;

import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.util.ZLogger;

import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Local
@Stateful
public class SensorPollerManager {
    private final ZLogger log = ZLogger.getLogger(this.getClass());
    private Map<Long, SensorPoller> polledSensors = new HashMap<>();
    private boolean stop = false;

    private SensorHostRepository sensorHostRepository;

    @Inject
    void setSensorHostRepository(SensorHostRepository sensorHostRepository) {
        this.sensorHostRepository = sensorHostRepository;
    }

    @Inject
    private SensorHostInterface sensorHostInterface;

    @Inject
    private SampleRepository sampleRepository;

    private Runnable runnable = ()  ->  {
        while(!stop) {
            try {
                Thread.sleep(60 * 1000);

                loop();

                startNewPollers();
            }
            catch (InterruptedException e) {
                log.warn("SensorPoller thread was interrupted: " + e);
                stop = true;
            }
            catch (Throwable t) {
                log.error("Caught exception in SensorPoller thread: ", t);
            }
        }
    };
    private Thread thread = new Thread(runnable);

//    public void stop() {
//        stop = true;
//        log.warn("Normal: Stopping poller manager");
//    }

    public void start() {
        thread.start();
    }

//    public boolean isAlive() {
//        return thread.isAlive();
//    }

    private void startNewPollers() {
        for (SensorPoller poller : polledSensors.values()) {
            if (!poller.isAlive()) {
                poller.start();
                log.warn("Normal: Started poller " + poller.getName());
            }
        }
    }

    void loop() {
        List<SensorHost> activeHosts = sensorHostRepository.findByActive(true);

        removeInactivePollers(sensorHostRepository);

        addActivePollers(activeHosts);
    }

    int getSensorPollerCount() {
        return this.polledSensors.size();
    }

    private void addActivePollers(List<SensorHost> activeHosts) {
        for (SensorHost host : activeHosts) {
            if (!polledSensors.containsKey(host.getId())) {
                SensorPoller poller = new SensorPoller(host, sensorHostInterface, sampleRepository);
//                if (beanFactory != null) beanFactory.autowireBean(poller);
                polledSensors.put(host.getId(), poller);
                log.warn("Normal: Added sensor poller " + poller.getName() + "  For sensor host " + host.getLocation());
            }
        }
    }

    private void removeInactivePollers(SensorHostRepository sensorHostRepo) {
        List<Long> pollersToStop = new ArrayList<>();
        for (Long pollerId : polledSensors.keySet()) {
            SensorHost sensorHost = sensorHostRepo.findById(pollerId);
            if (sensorHost == null || !sensorHost.getActive()) {
                pollersToStop.add(pollerId);
            }
        }
        for (Long pollerId : pollersToStop) {
            SensorPoller poller = polledSensors.get(pollerId);
            if (poller != null) {
                poller.setStop();
                log.warn("Normal: Removed sensor poller " + poller.getName());
            }
            polledSensors.remove(pollerId);
        }
    }
}