package com.automationanywhere.cognitive.iqbot;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import com.automationanywhere.cognitive.iqbot.parser.Reader;
import com.automationanywhere.cognitive.iqbot.parser.impl.ReadCSV;
import com.automationanywhere.cognitive.iqbot.processor.impl.ReportGenerator;
import com.automationanywhere.cognitive.iqbot.util.AppUtil;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

  private static String filePath;
  private static String fileExt;
  private static String outputPath;
  private static String outputFileName;
  private static String topNonPerformerAPIThreshold;
  private static String mainHeader;
  private static String errorHeader;
  private static String apiThresholdHeader;

  public static void main(String[] args) {
    Properties properties = loadProps();
    readProps(properties);
    Map<String, Object> parentMap = new HashMap<>();
    // Parse input file
    Reader reader = new ReadCSV();

    doProcess(reader);


  }

  private static void readProps(Properties properties) {
    filePath = properties.getProperty("inputFilePath");
    fileExt = properties.getProperty("inputFileExt");
    outputPath = properties.getProperty("outputFilePath");
    outputFileName = properties.getProperty("outputFileName");
    topNonPerformerAPIThreshold = properties.getProperty("topNonPerformerApiThreshold");
    mainHeader = properties.getProperty("mainHeader");
    errorHeader = properties.getProperty("errorHeader");
    apiThresholdHeader = properties.getProperty("apiThresholdHeader");
  }

  private static Properties loadProps() {
    Properties properties = null;
    try {
      FileReader propReader = new FileReader("app.properties");
      properties = new Properties();
      properties.load(propReader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }

  private static  Map<String, Object> doProcess(Reader reader) {
    Map<String, ReportData> reportMap = new LinkedHashMap<>();
    List<String> fileNames = null;
    Map<String, List<String>> fileNameMap = new LinkedHashMap<>();
    File file = new File(filePath);
    if(file.isDirectory()) {
      File[] files = file.listFiles();
      Arrays.sort(files, new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
          int n1 = extractNumberFromFileName(o1.getName());
          int n2 = extractNumberFromFileName(o2.getName());
          return n1-n2;
        }

        private int extractNumberFromFileName(String name) {
          int i = 0;
          try {
            int endIndex = name.indexOf("VU");
            String number = name.substring(0, endIndex);
            i = Integer.parseInt(number);
          } catch(Exception e) {
            i = 0; // if filename does not match the format
            // then default to 0
          }
          return i;
        }
      });
      for (File childFile : files) {
        reader = new ReadCSV();
        if (childFile.isDirectory()) {
          String parentName = childFile.getName();
          ReportReader rr = new ReportReader(filePath + File.separator + parentName, fileExt,
              reader);
          Map<String, List<AggregateData>> map = rr.processor();
          fileNames = rr.getFileNameList();
          reportMap.put(parentName, rr.getReportData(map));
          fileNameMap.put(parentName, fileNames);
        }
      }
    }

    // Process and generate report
    ReportGenerator rg = new ReportGenerator(outputPath,
        outputFileName,
        Double.parseDouble(topNonPerformerAPIThreshold));
    //Get Headers
    List<String> headers = AppUtil.getHeaderList(mainHeader);
    List<String> errorHeaders = AppUtil.getHeaderList(errorHeader);
    List<String> timeConsumptionHeader = AppUtil
        .getHeaderList(apiThresholdHeader);

    return rg.fileProcessor(reportMap, fileNameMap, headers, errorHeaders, timeConsumptionHeader);
  }
}
