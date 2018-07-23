package net.zhome.home.model;

import net.zhome.home.AbstractIntegrationTest;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.repository.SampleRepository;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SampleRepositoryTest extends AbstractIntegrationTest {

    @Inject
    private SampleRepository sampleRepo;

    @Test
    public void SampleCrDeTest() throws Exception {
        Sample s1 = new Sample(1L, 10L, 12.3F);
        Sample s2 = new Sample(1L, 11L, 12.3F);
        sampleRepo.save(s1);
        sampleRepo.save(s2);
        assertEquals(2, sampleRepo.findBySensorId(1L).size());

        sampleRepo.delete(s2.getId());
        assertEquals(1, sampleRepo.findBySensorId(1L).size());
        sampleRepo.deleteAll();
        assertEquals(0, sampleRepo.findBySensorId(1L).size());
    }

    @Test
    public void SampleGreaterThanTest() throws Exception {
        Sample s1 = new Sample(1, 10L, 12.3F);
        Sample s2 = new Sample(1, 11L, 12.3F);
        Sample s3 = new Sample(2, 11L, 12.3F);
        sampleRepo.save(s1);
        sampleRepo.save(s2);
        sampleRepo.save(s3);
        assertEquals(1, sampleRepo.findBySensorIdAndTimeMsGreaterThanEqualOrderByTimeMsAsc(1L, 11L).size());

        sampleRepo.deleteAll();
        assertEquals(0, sampleRepo.findBySensorId(1L).size());
    }

    @Ignore
    @Test
    public void scaleTest() {
        long start = new Date().getTime();
        long count = 1000;
        long batch = 100;
        List<Sample> samples = new ArrayList<>();
        for (long i=0; i<count; i+=batch) {
            samples.clear();
            for (long j = 0; j<batch; j++) {
                Sample s = new Sample(1, 100L + i, 42.5F);
                samples.add(s);
            }
            sampleRepo.saveAll(samples);
        }
        assertEquals(count, sampleRepo.findBySensorId(1L).size());
        sampleRepo.deleteAll();
        assertEquals(0, sampleRepo.findBySensorId(1L).size());

        long end = new Date().getTime();
        System.out.println("Test duration = " + (end-start)/1000 + " Seconds");
    }

}