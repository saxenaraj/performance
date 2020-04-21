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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReportGenerator implements FileProcessor {

  private final String outputFilePath;
  private final String outputFileName;
  private final String SHEETNAME_RAW = "Raw";
  private final String SHEETNAME_DETAILS = "Details";
  private final String SHEETNAME_TRANS = "Transaction RT";
  private final String SHEETNAME_ERROR = "RT & Errors";
  private final String SHEETNAME_TOPAPI = "Top Time Consuming APIs";
  private final String TRANSACTION = "Transaction";
  public XSSFWorkbook workbook;
  private static DecimalFormat df = new DecimalFormat("0.00");
  private static final String outputFileExt = ".xlsx";
  private Map<String, Map<String, List<String>>> transactionMap;
  private Map<String, Map<String, Double>> timeConsumptionMap;
  private Map<String, Map<String, List<String>>> apiTransactionMap;
  private List<String> apiList;
  private Map<String, Object> retMap;
  Double limit;

  public ReportGenerator(String outputFilePath, String outputFileName,
      Double nonPerformerApiRTThreshold) {
    this.outputFilePath = outputFilePath;
    this.outputFileName = outputFileName;
    createWorkBook();
    apiList = new ArrayList<>();
    limit = nonPerformerApiRTThreshold;
  }

  public void createWorkBook() {
    workbook = new XSSFWorkbook();
    df.setRoundingMode(RoundingMode.UP);
  }

  @Override
  public Map<String, Object> fileProcessor(Map<String, ReportData> reportData,
      Map<String, List<String>> fileNameMap, List<String> headers, List<String> errorHeaders,
      List<String> timeConsumptionHeader) {
    retMap = new LinkedHashMap<>();
    transactionMap = new LinkedHashMap<>();
    timeConsumptionMap = new LinkedHashMap<>();
    apiTransactionMap = new LinkedHashMap<>();

    // There are separate sheets need to be generated.
    // We need to first create the raw data sheet
    // and detail sheet for all the inputs.
    for (Map.Entry<String, ReportData> data : reportData.entrySet()) {
      // raw data sheet
      createRawSheet(data.getValue(), fileNameMap.get(data.getKey()), headers,
          data.getKey() + "_" + SHEETNAME_RAW);
      // Now create detail sheet
      createDetailSheet(data.getValue(), fileNameMap.get(data.getKey()), data.getKey(), headers,
          data.getKey() + "_" + SHEETNAME_DETAILS);
    }

    // Now create the consolidated sheets
    // Error sheet
    createAPIAndTransactionSheet(apiTransactionMap, fileNameMap, errorHeaders, SHEETNAME_ERROR);

    // Transaction sheet
    createAPIAndTransactionSheet(transactionMap, fileNameMap, errorHeaders, SHEETNAME_TRANS);

    // Most time consuming api sheet
    createTimeConsumptionSheet(timeConsumptionHeader, SHEETNAME_TOPAPI);

    try (FileOutputStream outputStream = new FileOutputStream(
        AppUtil.getOutputFile(outputFilePath, outputFileName + outputFileExt))) {
      workbook.write(outputStream);
      System.out.println("Report Generated Successfully.");
    } catch (FileNotFoundException e) {
      System.out.println("Error while generating report:::");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Error while generating report:::");
      e.printStackTrace();
    }
    return retMap;
  }

  public void createAPIAndTransactionSheet(Map<String, Map<String, List<String>>> inputMap,
      Map<String, List<String>> fileNames, List<String> headers, String sheetName) {

    XSSFSheet sheet = workbook.createSheet(sheetName);
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(-1);
    AtomicReference<Cell> cell = null;
    for (int i = 0; i < headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
      colCount.set(i > 0 ? (colCount.get() + fileNames.size() - 1) : (colCount.get()));
    }
    row = sheet.createRow(++rowCount);
    colCount.set(-1);
    cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
    for (int i = 0; i < headers.size() - 1; i++) {
      for (Map.Entry<String, List<String>> fileName : fileNames.entrySet()) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(fileName.getKey());
      }
    }

    for (Map.Entry<String, Map<String, List<String>>> transEntry : inputMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      colCount.set(-1);
      cell.set(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(transEntry.getKey());
      Map<String, List<String>> dataMap = transEntry.getValue();
      Row finalRow = row;
      AtomicReference<Cell> finalCell = cell;
      AtomicInteger finalColCount = colCount;

      dataMap.forEach((key, val) -> {
        val.stream().forEach(value -> {
          finalCell.set(finalRow.createCell(finalColCount.incrementAndGet()));
          finalCell.get().setCellValue(value);
        });
      });
    }
  }

  public void createTimeConsumptionSheet(List<String> headers, String sheetName) {
    if (!timeConsumptionMap.isEmpty()) {
      XSSFSheet sheet = workbook.createSheet(sheetName);
      int rowCount = -1;
      for (Map.Entry<String, Map<String, Double>> map : timeConsumptionMap.entrySet()) {
        Map<String, Double> timeConsumptionData = AppUtil.sortByValue(map.getValue());
        rowCount += 3;

        Row row = sheet.createRow(++rowCount);
        AtomicInteger colCount = new AtomicInteger(0);
        AtomicReference<Cell> cell = null;
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        CellStyle style = workbook.createCellStyle();
        // Setting Background color
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, colCount.get(),
            (colCount.get() + headers.size() - 1)));
        cell.get()
            .setCellValue(map.getKey() + " - Top Time Consuming APIs (Response Time in seconds)");
        cell.get().setCellStyle(style);
        row = sheet.createRow(++rowCount);
        colCount.set(0);

        style = workbook.createCellStyle();
        // Setting Background color
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);

        for (int i = 0; i < headers.size(); i++) {
          cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue(headers.get(i));
          cell.get().setCellStyle(style);
        }

        for (Map.Entry<String, Double> entry : timeConsumptionData.entrySet()) {
          row = sheet.createRow(++rowCount);
          colCount.set(0);
          String[] apiArr = getTransactionAndAPI(entry.getKey());
          cell.set(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue(apiArr[0]);
          cell.set(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue(apiArr[1]);
          cell.set(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue(df.format(entry.getValue()));
        }
      }
    }
  }

  private String[] getTransactionAndAPI(String key) {
    String[] retArr = new String[2];
    int slashIndex = key.indexOf("/");
    String tempStr = key.substring(0, slashIndex);
    int underscoreIndex = tempStr.lastIndexOf("_");
    retArr[0] = key.substring(0, underscoreIndex);
    retArr[1] = key.substring(underscoreIndex + 1);

    return retArr;
  }


  public void createRawSheet(ReportData reportData, List<String> fileNames, List<String> headers,
      String sheetName) {

    XSSFSheet sheet = workbook.createSheet(sheetName);
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(0);
    AtomicReference<Cell> cell = null;
    for (int i = 0; i < headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
      colCount.set(colCount.get() + fileNames.size() - 1);
    }
    row = sheet.createRow(++rowCount);
    colCount.set(-1);
    cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
    cell.get().setCellValue("API");
    for (int i = 0; i < headers.size(); i++) {
      for (String fileName : fileNames) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(fileName);
      }
    }
    Map<String, List<Sample>> sampleMap = reportData.getSampleMap();
    for (Map.Entry<String, List<Sample>> sam : sampleMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      String apiName = sam.getKey();
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

  public void createDetailSheet(ReportData reportData, List<String> fileNames,
      String parentName, List<String> headers, String sheetName) {

    XSSFSheet sheet = workbook.createSheet(sheetName);
    int rowCount = -1;
    Row row = sheet.createRow(++rowCount);
    AtomicInteger colCount = new AtomicInteger(0);
    AtomicReference<Cell> cell = null;
    for (int i = 0; i < headers.size(); i++) {
      cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
      cell.get().setCellValue(headers.get(i));
      colCount.set(i == 0 ? (colCount.get() + fileNames.size() - 1)
          : (colCount.get() + fileNames.size() + 1));
    }
    row = sheet.createRow(++rowCount);
    colCount.set(-1);
    cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
    cell.get().setCellValue("API");
    for (int i = 0; i < headers.size(); i++) {
      for (String fileName : fileNames) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue(fileName);
      }
      if (i > 0) {
        cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
        cell.get().setCellValue("Average RT (ms)");
        if (i < headers.size() - 1) {
          cell = new AtomicReference<>(row.createCell(colCount.incrementAndGet()));
          cell.get().setCellValue("Average RT (sec)");
        }
      }
    }
    Map<String, List<Sample>> sampleMap = reportData.getSampleMap();
    Double rowAvg = 0.0d;
    String transVal;
    for (Map.Entry<String, List<Sample>> sam : sampleMap.entrySet()) {
      row = sheet.createRow(++rowCount);
      String apiName = sam.getKey();
      boolean isTransaction = AppUtil.isTransaction(apiName);

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
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false, "average", parentName);

      List<Median> medians = reportData.getMedianMap().get(apiName);
      rowAvg = medians.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.median);
        return sample.median;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false, "median", parentName);

      List<Perct90> perct90s = reportData.getPercentile90Map().get(apiName);
      rowAvg = perct90s.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.perct90);
        return sample.perct90;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, true, "perct90s", parentName);

      List<Min> mins = reportData.getMinMap().get(apiName);
      rowAvg = mins.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.min);
        return sample.min;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false, "mins", parentName);

      List<Max> maxes = reportData.getMaxMap().get(apiName);
      rowAvg = maxes.stream().mapToInt(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.max);
        return sample.max;
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, false);
      setTransaction(isTransaction, transVal, apiName, false, "maxes", parentName);

      List<Error> errors = reportData.getErrorMap().get(apiName);
      rowAvg = errors.stream().mapToDouble(sample -> {
        finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
        finalCell.get().setCellValue(sample.error);
        return AppUtil.getNumberValue(sample.error);
      }).average().orElse(0.0);
      transVal = setAverageColumn(finalCell, finalRow, colCount, rowAvg, true);
      setTransaction(isTransaction, transVal, apiName, false, "errors", parentName);
    }
  }

  private void setTransaction(boolean isTransaction, String transVal, String apiName,
      boolean isTimeConsumingMatrix, String colName, String parentName) {
    if (isTransaction) {
      if (!transactionMap.containsKey(apiName)) {
        transactionMap.put(apiName, new LinkedHashMap<>());
      }
      Map<String, List<String>> dataMap = transactionMap.get(apiName);
      if (!dataMap.containsKey(colName)) {
        dataMap.put(colName, new ArrayList<>());
      }
      dataMap.get(colName).add(transVal);
      transactionMap.put(apiName, dataMap);
    } else {
      if (!apiTransactionMap.containsKey(apiName)) {
        apiTransactionMap.put(apiName, new LinkedHashMap<>());
      }
      Map<String, List<String>> dataMap = apiTransactionMap.get(apiName);
      if (!dataMap.containsKey(colName)) {
        dataMap.put(colName, new ArrayList<>());
      }
      dataMap.get(colName).add(transVal);
      apiTransactionMap.put(apiName, dataMap);

      if (isTimeConsumingMatrix) {
        Double val = Double.parseDouble(transVal);
        if (val >= limit) {
          if (!timeConsumptionMap.containsKey(parentName)) {
            timeConsumptionMap.put(parentName, new HashMap<>());
          }
          timeConsumptionMap.get(parentName).put(apiName, Double.parseDouble(transVal));
        }
      }
    }
  }

  private String setAverageColumn(AtomicReference<Cell> finalCell, Row finalRow,
      AtomicInteger colCount, Double avg, boolean isPercentage) {
    final String retStr;
    if (!isPercentage) {
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      finalCell.get().setCellValue(df.format(avg));
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      retStr = df.format(avg / 100);
      finalCell.get().setCellValue(retStr);
    } else {
      finalCell.set(finalRow.createCell(colCount.incrementAndGet()));
      retStr = df.format(avg) + "%";
      finalCell.get().setCellValue(retStr);
    }
    return retStr;
  }
}
