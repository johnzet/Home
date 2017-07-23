package net.zhome.home.model;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.repository.SampleRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
public class SampleRepositoryTest {

    @Autowired
    private
    SampleRepository sampleRepo;

    @Test
    public void SampleCrDeTest() throws Exception {
        Sample s1 = new Sample(1L, 10L, 12.3F);
        Sample s2 = new Sample(1L, 11L, 12.3F);
        sampleRepo.saveAndFlush(s1);
        sampleRepo.saveAndFlush(s2);
        assertEquals(2, sampleRepo.findBySensorId(1L).size());

        sampleRepo.delete(s2);
        assertEquals(1, sampleRepo.findBySensorId(1L).size());
        sampleRepo.deleteAll();
        assertEquals(0, sampleRepo.findBySensorId(1L).size());
    }

    @Test
    public void SampleGreaterThanTest() throws Exception {
        Sample s1 = new Sample(1, 10L, 12.3F);
        Sample s2 = new Sample(1, 11L, 12.3F);
        Sample s3 = new Sample(2, 11L, 12.3F);
        sampleRepo.saveAndFlush(s1);
        sampleRepo.saveAndFlush(s2);
        sampleRepo.saveAndFlush(s3);
        assertEquals(1, sampleRepo.findBySensorIdAndTimeMsGreaterThanEqual(1L, 11L).size());

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
            sampleRepo.save(samples);
        }
        sampleRepo.flush();
        assertEquals(count, sampleRepo.findBySensorId(1L).size());
        sampleRepo.deleteAll();
        assertEquals(0, sampleRepo.findBySensorId(1L).size());

        long end = new Date().getTime();
        System.out.println("Test duration = " + (end-start)/1000 + " Seconds");
    }

}