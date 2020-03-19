package com.automationanywhere.cognitive.iqbot.processor;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.model.ReportData;
import java.util.List;
import java.util.Map;

public interface FileProcessor {

  public void fileProcessor(ReportData reportData, List<String> fileNames, List<String> headers,
      List<String> errorHeaders, List<String> timeConsumptionHeader);
}
