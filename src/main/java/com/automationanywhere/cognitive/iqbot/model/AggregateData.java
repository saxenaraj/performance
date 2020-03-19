package com.automationanywhere.cognitive.iqbot.model;

import com.opencsv.bean.CsvBindByName;

public class AggregateData {

  @CsvBindByName(column = "Label")
  private String label;
  @CsvBindByName(column = "# Samples")
  private int	noOfSamples;
  @CsvBindByName(column = "Average")
  private int average;
  @CsvBindByName(column = "Median")
  private int median;
  @CsvBindByName(column = "90% Line")
  private int percentile90;
  @CsvBindByName(column = "95% Line")
  private int percentile95;
  @CsvBindByName(column = "99% Line")
  private int percentile99;
  @CsvBindByName(column = "Min")
  private int min;
  @CsvBindByName(column = "Max")
  private int max;
  @CsvBindByName(column = "Error %")
  private String error;
  @CsvBindByName(column = "Throughput")
  private double throughput;
  @CsvBindByName(column = "Received KB/sec")
  private double received;
  @CsvBindByName(column = "Sent KB/sec")
  private double sent;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getNoOfSamples() {
    return noOfSamples;
  }

  public void setNoOfSamples(int noOfSamples) {
    this.noOfSamples = noOfSamples;
  }

  public int getAverage() {
    return average;
  }

  public void setAverage(int average) {
    this.average = average;
  }

  public int getMedian() {
    return median;
  }

  public void setMedian(int median) {
    this.median = median;
  }

  public int getPercentile90() {
    return percentile90;
  }

  public void setPercentile90(int percentile90) {
    this.percentile90 = percentile90;
  }

  public int getPercentile95() {
    return percentile95;
  }

  public void setPercentile95(int percentile95) {
    this.percentile95 = percentile95;
  }

  public int getPercentile99() {
    return percentile99;
  }

  public void setPercentile99(int percentile99) {
    this.percentile99 = percentile99;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public double getThroughput() {
    return throughput;
  }

  public void setThroughput(double throughput) {
    this.throughput = throughput;
  }

  public double getReceived() {
    return received;
  }

  public void setReceived(double received) {
    this.received = received;
  }

  public double getSent() {
    return sent;
  }

  public void setSent(double sent) {
    this.sent = sent;
  }



  public AggregateData(String label, int noOfSamples, int average, int median, int percentile90,
      int percentile95, int percentile99, int min, int max, String error, double throughput,
      double received, double sent) {
    this.label = label;
    this.noOfSamples = noOfSamples;
    this.average = average;
    this.median = median;
    this.percentile90 = percentile90;
    this.percentile95 = percentile95;
    this.percentile99 = percentile99;
    this.min = min;
    this.max = max;
    this.error = error;
    this.throughput = throughput;
    this.received = received;
    this.sent = sent;
  }
  public AggregateData(){}

}
