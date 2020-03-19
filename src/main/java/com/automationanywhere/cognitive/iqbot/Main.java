package com.automationanywhere.cognitive.iqbot;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import com.automationanywhere.cognitive.iqbot.parser.Reader;
import com.automationanywhere.cognitive.iqbot.parser.impl.ReadCSV;
import com.automationanywhere.cognitive.iqbot.processor.impl.ReportGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    Reader reader = new ReadCSV();
    ReportReader rr = new ReportReader("D:\\Perf Test Docs\\final docs\\aggregate", ".csv", reader);
    Map<String, List<AggregateData>> map = rr.processor();
    List<String> fileNames = rr.getFileNameList();
    ReportData reportData = rr.getReportData(map);
    ReportGenerator rg = new ReportGenerator("D:\\Perf Test Docs\\final docs\\output", "11.3.4.2_1VU_1MB_20200318", 1.00d);
    List<String> headers = new ArrayList<String>() {{
      add("Sample Count");
      add("Average RT (ms)");
      add("Median RT (ms)");
      add("90 Percentile RT (ms)");
      add("Min RT (ms)");
      add("Max RT (ms)");
      add("Error %");
    }};

    List<String> errorHeaders = new ArrayList<String>() {{
      add("Transactions");
      add("Average RT (sec)");
      add("Median RT (sec)");
      add("90 Percentile RT (sec)");
      add("Min RT (sec)");
      add("Max RT (sec)");
      add("Error %");
    }};

    List<String> timeConsumptionHeader = new ArrayList<String>() {{
      add("Transactions");
      add("API");
      add("RT (sec)");
    }};
    rg.fileProcessor(reportData, fileNames, headers, errorHeaders, timeConsumptionHeader);
  }

}
