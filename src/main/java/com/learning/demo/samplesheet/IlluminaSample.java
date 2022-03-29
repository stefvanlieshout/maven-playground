package com.learning.demo.samplesheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableIlluminaSample.class)
public interface IlluminaSample {

    @JsonProperty(value = "Sample_Name")
    String sample();

    @JsonProperty(value = "Sample_ID")
    String barcode();

    @JsonProperty(value = "Sample_Project")
    String project();

    static ImmutableIlluminaSample.Builder builder() {
        return ImmutableIlluminaSample.builder();
    }
}