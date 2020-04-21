package com.automationanywhere.cognitive.iqbot;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import com.automationanywhere.cognitive.iqbot.parser.Reader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class ReportReader {

  private List<String> fileList = new ArrayList<>();
  private List<String> fileNameList;
  private final String filePath;
  private final String fileExt;

  private Reader reader;

  public ReportReader(String filePath, String fileExt, Reader reader) {
    this.filePath = filePath;
    this.fileExt = fileExt;
    this.reader = reader;
    init();
  }

  private void init() {
    try (Stream<Path> walk = Files.walk(Paths.get(filePath))) {
      fileList = walk.map(x -> x.toString())
          .filter(f -> f.endsWith(fileExt)).collect(Collectors.toList());

      fileList.forEach(System.out::println);
      fileNameList = new ArrayList<>();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, List<AggregateData>> processor() {
    Map<String, List<AggregateData>> map = new LinkedHashMap<>();
    for (String file : fileList) {
      String fileName = getFileName(file);
      fileNameList.add(fileName);
      map.put(fileName, reader.readFile(file, true));
    }
    return map;
  }

  public ReportData getReportData(Map<String, List<AggregateData>> map) {

    ReportData rData = new ReportData();
    for (Map.Entry<String, List<AggregateData>> data : map.entrySet()) {
      List<AggregateData> aggregateDataList = data.getValue();
      for (AggregateData aggregateData : aggregateDataList) {
        if(StringUtils.isNotBlank(aggregateData.getLabel())) {
          // Sample
          rData.setSampleValueInMap(aggregateData);
          // Average
          rData.setAvgValueInMap(aggregateData);
          // Median
          rData.setMedianValueInMap(aggregateData);
          // 90 Percentile
          rData.setPerct90ValueInMap(aggregateData);
          // Min
          rData.setMinValueInMap(aggregateData);
          // Max
          rData.setMaxValueInMap(aggregateData);
          // Error
          rData.setErrorValueInMap(aggregateData);
        }
      }
    }
    return rData;
  }

  public String getFileName(String f) {
    File file = new File(f);
    String fileName = file.getName();
    int pos = fileName.lastIndexOf(".");
    if (pos > 0 && pos < (fileName.length() - 1)) { // If '.' is not the first or last character.
      fileName = fileName.substring(0, pos);
    }
    return fileName;
  }

  public List<String> getFileNameList() {
    return fileNameList;
  }
}
