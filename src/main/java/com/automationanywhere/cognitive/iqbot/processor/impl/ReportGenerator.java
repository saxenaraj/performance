package com.automationanywhere.cognitive.iqbot.processor.impl;

import com.automationanywhere.cognitive.iqbot.model.Average;
import com.automationanywhere.cognitive.iqbot.model.Error;
import com.automationanywhere.cognitive.iqbot.model.Max;
import com.automationanywhere.cognitive.iqbot.model.Median;
import com.automationanywhere.cognitive.iqbot.model.Min;
import com.automationanywhere.cognitive.iqbot.model.Perct90;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import com.automationanywhere.cognitive.iqbot.model.Sample;
import com.automationanywhere.cognitive.iqbot.processor.FileProcessor;
import com.automationanywhere.cognitive.iqbot.util.AppUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReportGenerator implements FileProcessor {

  private final String outputFilePath;
  private final String outputFileName;
  public XSSFWorkbook workbook;
  private static DecimalFormat df = new DecimalFormat("0.00");
  private static final String outputFileExt=".xlsx";
  private Map<String, List<String>> transactionMap;
  private Map<String, Double> timeConsumptionMap;
  private Map<String, String> apiTransactionMap;
  private List<String> apiList;
  Double limit;

  public ReportGenerator(String outputFilePath, String outputFileName, Double nonPerformerApiRTThreshold) {
    this.outputFilePath = outputFilePath;
    this.outputFileName = outputFileName;
    createWorkBook();
    transactionMap = new LinkedHashMap<>();
    timeConsumptionMap = new HashMap<>();
    apiTransactionMap = new LinkedHashMap<>();
    apiList = new ArrayList<>();
    limit = nonPerformerApiRTThreshold;
  }

  public void createWorkBook() {
    workbook = new XSSFWorkbook();
    df.setRoundingMode(RoundingMode.UP);
  }

  private void populateApiTransactionMap(String apiName) {
    if(!Character.isDigit(apiName.charAt(0))) {
      apiList.stream().forEach(api -> apiTransactionMap.put(api, apiName));
      apiList = new ArrayList<>();
    } else {
      apiList.add(apiName);
    }
  }

  @Override
  public void fileProcessor(ReportData reportData, List<String> fileNames, List<String> headers, List<String> errorHeaders, List<String> timeConsumptionHeader) {
    // There are different sheets need to be generated.
    // We need to first create the raw data sheet.
    createRawSheet(reportData, fileNames, headers);
    // Now create detail sheet
    createDetailSheet(reportData, fileNames, headers);
    // Error sheet
    createErrorSheet(reportData, errorHeaders);
    // Most time consuming api sheet
    createTimeConsumptionSheet(timeConsumptionHeader);

    try (FileOutputStream outputStream = new FileOutputStream(AppUtil.getOutputFile(outputFilePath, outputFileName + outputFileExt))) {
      workbook.write(outputStream);
      System.out.println("Report Generated Successfully.");
    } catch (FileNotFoundException e) {
      System.out.println("Error while generating report:::");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Error while generating report:::");
      e.printStackTrace();
    }
  }

  public void createTimeConsumptionSheet(List<String> headers) {
    if(!timeConsumptionMap.isEmpty()) {
      Map<String, Double> timeConsumptionSortedMap = sortByValue(timeConsumptionMap);
      XSSFSheet sheet = workbook.createSheet("Top Time Consuming APIs");
      int rowCount = 2;
      Row row = sheet.createRow(++rowCount);
      AtomicInteger colCount = new AtomicInteger(0);
      AtomicReference<Cell> cell = null;

      for(int i=0; i<headers.size(); i++) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(headers.get(i));
      }

      for(Map.Entry<String, Double> entry : timeConsumptionSortedMap.entrySet()) {
        row = sheet.createRow(++rowCount);
        colCount.set(0);
        String apiName = entry.getKey();
        cell.set(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(apiTransactionMap.get(apiName));
        cell.set(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(apiName);
        cell.set(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(entry.getValue());
      }
    }
  }

  public void createErrorSheet(ReportData reportData, List<String> headers) {

    XSSFSheet sheet = workbook.createSheet("RT & Errors");
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(-1);
    AtomicReference<Cell> cell = null;
    for(int i=0; i<headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
    }

    Map<String, List<Sample>> sampleMap = reportData.getSampleMap();
    for(Map.Entry<String, List<String>> transData : transactionMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      colCount.set(-1);
      cell.set(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(transData.getKey());
      Row finalRow = row;
      List<String> values = transData.getValue();
      AtomicReference<Cell> finalCell = cell;
      values.stream().forEach(val -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(val);
      });
    }

  }

  public void createRawSheet(ReportData reportData, List<String> fileNames, List<String> headers) {

    XSSFSheet sheet = workbook.createSheet("Raw");
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(0);
    AtomicReference<Cell> cell = null;
    for(int i=0; i<headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
      colCount.set(colCount.get()+fileNames.size()-1);
    }
    row = sheet.createRow(++rowCount);
    colCount.set(-1);
    cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
    cell.get().setCellValue("API");
    for(int i=0; i<headers.size(); i++) {
      for (String fileName : fileNames) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(fileName);
      }
    }
    Map<String, List<Sample>> sampleMap = reportData.getSampleMap();
    for(Map.Entry<String, List<Sample>> sam : sampleMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      String apiName = sam.getKey();
      populateApiTransactionMap(apiName);
      colCount.set(-1);
      cell.set(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(apiName);
      Row finalRow = row;
      List<Sample> samples = sam.getValue();
      AtomicReference<Cell> finalCell = cell;
      samples.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.noOfSample);
      });

      List<Average> averages = reportData.getAvgMap().get(apiName);
      averages.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.average);
      });

      List<Median> medians = reportData.getMedianMap().get(apiName);
      medians.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.median);
      });

      List<Perct90> perct90s = reportData.getPercentile90Map().get(apiName);
      perct90s.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.perct90);
      });

      List<Min> mins = reportData.getMinMap().get(apiName);
      mins.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.min);
      });

      List<Max> maxes = reportData.getMaxMap().get(apiName);
      maxes.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.max);
      });

      List<Error> errors = reportData.getErrorMap().get(apiName);
      errors.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.error);
      });
    }


  }



  public void createDetailSheet(ReportData reportData, List<String> fileNames, List<String> headers) {

    XSSFSheet sheet = workbook.createSheet("Details");
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(0);
    AtomicReference<Cell> cell = null;
    for(int i=0; i<headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
      colCount.set(i==0? (colCount.get()+fileNames.size()-1) : (colCount.get()+fileNames.size()+1));
    }
    row = sheet.createRow(++rowCount);
    colCount.set(-1);
    cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
    cell.get().setCellValue("API");
    for(int i=0; i<headers.size(); i++) {
      for (String fileName : fileNames) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(fileName);
      } if(i>0) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue("Average RT (ms)");
        if(i<headers.size()-1) {
          cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue("Average RT (sec)");
        }
      }
    }
    Map<String, List<Sample>> sampleMap = reportData.getSampleMap();
    Double rowAvg = 0.0d;
    String transVal;
    for(Map.Entry<String, List<Sample>> sam : sampleMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      String apiName = sam.getKey();
      boolean isTransaction = !Character.isDigit(apiName.charAt(0));

      colCount.set(-1);
      cell.set(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(apiName);
      Row finalRow = row;
      List<Sample> samples = sam.getValue();
      AtomicReference<Cell> finalCell = cell;
      samples.stream().forEach(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.noOfSample);
      });

      List<Average> averages = reportData.getAvgMap().get(apiName);
      rowAvg = averages.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.average);
        return sample.average;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg,false);
      setTransaction(isTransaction, transVal, apiName, false);

      List<Median> medians = reportData.getMedianMap().get(apiName);
      rowAvg = medians.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.median);
        return sample.median;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg,false);
      setTransaction(isTransaction, transVal, apiName, false);

      List<Perct90> perct90s = reportData.getPercentile90Map().get(apiName);
      rowAvg = perct90s.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.perct90);
        return sample.perct90;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, true);

      List<Min> mins = reportData.getMinMap().get(apiName);
      rowAvg = mins.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.min);
        return sample.min;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false);

      List<Max> maxes = reportData.getMaxMap().get(apiName);
      rowAvg = maxes.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.max);
        return sample.max;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false);

      List<Error> errors = reportData.getErrorMap().get(apiName);
      rowAvg = errors.stream().mapToDouble(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.error);
        return getNumberValue(sample.error);
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, true);
      setTransaction(isTransaction, transVal, apiName, false);
    }
  }

  private void setTransaction(boolean isTransaction, String transVal, String apiName,
      boolean isTimeConsumingMatrix) {
    if(isTransaction) {
      if(!transactionMap.containsKey(apiName)) {
        transactionMap.put(apiName, new ArrayList<>());
      }
      transactionMap.get(apiName).add(transVal);
    } else {
      if(isTimeConsumingMatrix) {
        Double val = Double.parseDouble(transVal);
        if(val >= limit) {
          timeConsumptionMap.put(apiName, Double.parseDouble(transVal));
        }
      }
    }
  }

  private double getNumberValue(String errorStr) {
    return Double.parseDouble(errorStr.replace("%", ""));
  }

  private String setAverageColumn(AtomicReference<Cell> finalCell, Row finalRow,
      AtomicInteger colCount, Double avg, boolean isPercentage) {
    final String retStr;
    if(!isPercentage) {
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      finalCell.get().setCellValue(df.format(avg));
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      retStr = df.format(avg/100);
      finalCell.get().setCellValue(retStr);
    } else {
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      retStr = df.format(avg)+"%";
      finalCell.get().setCellValue(retStr);
    }
    return retStr;
  }

  public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Entry.comparingByValue());


    Map<K, V> result = new LinkedHashMap<>();
    for(int i=list.size()-1; i>-1; i--) {
      Entry<K, V> entry = list.get(i);
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
