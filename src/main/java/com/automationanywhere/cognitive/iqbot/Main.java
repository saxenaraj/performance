package com.automationanywhere.cognitive.iqbot;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import com.automationanywhere.cognitive.iqbot.parser.Reader;
import com.automationanywhere.cognitive.iqbot.parser.impl.ReadCSV;
import com.automationanywhere.cognitive.iqbot.processor.impl.ReportGenerator;
import com.automationanywhere.cognitive.iqbot.util.AppUtil;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

  public static void main(String[] args) {
    Properties properties = null;
    try {
      FileReader propReader=new FileReader("app.properties");

      properties=new Properties();

      properties.load(propReader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Parse input file
    Reader reader = new ReadCSV();
    ReportReader rr = new ReportReader(properties.getProperty("inputFilePath"), properties.getProperty("inputFileExt"), reader);
    Map<String, List<AggregateData>> map = rr.processor();
    List<String> fileNames = rr.getFileNameList();
    ReportData reportData = rr.getReportData(map);

    // Process and generate report
    ReportGenerator rg = new ReportGenerator(properties.getProperty("outputFilePath"), properties.getProperty("outputFileName"), Double.parseDouble(properties.getProperty("topNonPerformerApiThreshold")));
    //Get Headers
    List<String> headers = AppUtil.getHeaderList(properties.getProperty("mainHeader"));
    List<String> errorHeaders = AppUtil.getHeaderList(properties.getProperty("errorHeader"));
    List<String> timeConsumptionHeader = AppUtil.getHeaderList(properties.getProperty("apiThresholdHeader"));

    rg.fileProcessor(reportData, fileNames, headers, errorHeaders, timeConsumptionHeader);
  }



}
