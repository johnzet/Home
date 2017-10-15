package net.zhome.home.persistence.repository;

import net.zhome.home.persistence.model.Sample;

import java.util.List;

/**
 * Created by John Zehetner on 10/14/17.
 */


public interface SampleRepositoryCustom {
    List<Sample> findCurrentSamples();
}
