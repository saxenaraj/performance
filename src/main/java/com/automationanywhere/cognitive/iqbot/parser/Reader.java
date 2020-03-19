package com.automationanywhere.cognitive.iqbot.parser;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import java.util.List;

public interface Reader {
  public List<AggregateData> readFile(String fileName, boolean isFileWithHeader);
}
