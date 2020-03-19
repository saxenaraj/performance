package com.automationanywhere.cognitive.iqbot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportData {
  private Map<String, List<Sample>> sampleMap = new LinkedHashMap<>();
  private Map<String, List<Average>> avgMap = new LinkedHashMap<>();
  private Map<String, List<Median>> medianMap = new LinkedHashMap<>();
  private Map<String, List<Perct90>> percentile90Map = new LinkedHashMap<>();
  private Map<String, List<Min>> minMap = new LinkedHashMap<>();
  private Map<String, List<Max>> maxMap = new LinkedHashMap<>();
  private Map<String, List<Error>> errorMap = new LinkedHashMap<>();

  public Map<String, List<Sample>> getSampleMap() {
    return sampleMap;
  }

  public void setSampleMap(
      Map<String, List<Sample>> sampleMap) {
    this.sampleMap = sampleMap;
  }



  public Map<String, List<Average>> getAvgMap() {
    return avgMap;
  }

  public void setAvgMap(
      Map<String, List<Average>> avgMap) {
    this.avgMap = avgMap;
  }



  public Map<String, List<Median>> getMedianMap() {
    return medianMap;
  }

  public void setMedianMap(
      Map<String, List<Median>> medianMap) {
    this.medianMap = medianMap;
  }


  public Map<String, List<Perct90>> getPercentile90Map() {
    return percentile90Map;
  }

  public void setPercentile90Map(
      Map<String, List<Perct90>> percentile90Map) {
    this.percentile90Map = percentile90Map;
  }

  public Map<String, List<Min>> getMinMap() {
    return minMap;
  }

  public void setMinMap(
      Map<String, List<Min>> minMap) {
    this.minMap = minMap;
  }

  public Map<String, List<Max>> getMaxMap() {
    return maxMap;
  }

  public void setMaxMap(
      Map<String, List<Max>> maxMap) {
    this.maxMap = maxMap;
  }

  public Map<String, List<Error>> getErrorMap() {
    return errorMap;
  }

  public void setErrorMap(
      Map<String, List<Error>> errorMap) {
    this.errorMap = errorMap;
  }

  public void setSampleValueInMap(AggregateData aggregateData) {
    Map<String, List<Sample>> sampleMap1 = this.getSampleMap();
    List<Sample> sampleList = sampleMap1.get(aggregateData.getLabel());
    if(sampleList== null) {
      sampleList = new ArrayList<>();
    }
    sampleList.add(new Sample(aggregateData.getNoOfSamples()));
    sampleMap1.put(aggregateData.getLabel(), sampleList);
    this.setSampleMap(sampleMap1);
  }

  public void setAvgValueInMap(AggregateData aggregateData) {
    Map<String, List<Average>> avgMap1 = this.getAvgMap();
    List<Average> avgList = avgMap1.get(aggregateData.getLabel());
    if(avgList== null) {
      avgList = new ArrayList<>();
    }
    avgList.add(new Average(aggregateData.getAverage()));
    avgMap1.put(aggregateData.getLabel(), avgList);
    this.setAvgMap(avgMap1);
  }

  public void setMedianValueInMap(AggregateData aggregateData) {
    Map<String, List<Median>> medianMap1 = this.getMedianMap();
    List<Median> medianList = medianMap1.get(aggregateData.getLabel());
    if(medianList== null) {
      medianList = new ArrayList<>();
    }
    medianList.add(new Median(aggregateData.getMedian()));
    medianMap1.put(aggregateData.getLabel(), medianList);
    this.setMedianMap(medianMap1);
  }

  public void setPerct90ValueInMap(AggregateData aggregateData) {
    Map<String, List<Perct90>> percentile90Map1 = this.getPercentile90Map();
    List<Perct90> perct90List = percentile90Map1.get(aggregateData.getLabel());
    if(perct90List== null) {
      perct90List = new ArrayList<>();
    }
    perct90List.add(new Perct90(aggregateData.getPercentile90()));
    percentile90Map1.put(aggregateData.getLabel(), perct90List);
    this.setPercentile90Map(percentile90Map1);
  }

  public void setMinValueInMap(AggregateData aggregateData) {
    Map<String, List<Min>> minMap1 = this.getMinMap();
    List<Min> minList = minMap1.get(aggregateData.getLabel());
    if(minList== null) {
      minList = new ArrayList<>();
    }
    minList.add(new Min(aggregateData.getMin()));
    minMap1.put(aggregateData.getLabel(), minList);
    this.setMinMap(minMap1);
  }

  public void setMaxValueInMap(AggregateData aggregateData) {
    Map<String, List<Max>> maxMap1 = this.getMaxMap();
    List<Max> maxList = maxMap1.get(aggregateData.getLabel());
    if(maxList== null) {
      maxList = new ArrayList<>();
    }
    maxList.add(new Max(aggregateData.getMax()));
    maxMap1.put(aggregateData.getLabel(), maxList);
    this.setMaxMap(maxMap1);
  }

  public void setErrorValueInMap(AggregateData aggregateData) {
    Map<String, List<Error>> errorMap1 = this.getErrorMap();
    List<Error> errorList = errorMap1.get(aggregateData.getLabel());
    if(errorList== null) {
      errorList = new ArrayList<>();
    }
    errorList.add(new Error(aggregateData.getError()));
    errorMap1.put(aggregateData.getLabel(), errorList);
    this.setErrorMap(errorMap1);
  }
}
