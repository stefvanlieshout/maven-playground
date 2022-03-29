package com.learning.demo.samplesheet;

import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
public interface SampleSheet {

    String experimentName();

    List<IlluminaSample> samples();

    static ImmutableSampleSheet.Builder builder() {
        return ImmutableSampleSheet.builder();
    }
}
