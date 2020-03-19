package com.automationanywhere.cognitive.iqbot.parser.impl;

import com.automationanywhere.cognitive.iqbot.model.AggregateData;
import com.automationanywhere.cognitive.iqbot.parser.Reader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReadCSV implements Reader {

  public List<AggregateData> readFile(String csvFile, boolean isFileWithHeader) {
    CSVReader reader = null;
    List<AggregateData> aggregateList = null;

    try {
      if (isFileWithHeader) {
        CsvToBean<AggregateData> csvToBean = new CsvToBeanBuilder(new FileReader(csvFile))
            .withType(AggregateData.class)
            .withIgnoreLeadingWhiteSpace(true).build();

        aggregateList = csvToBean.parse();
      }
    } catch (Exception ex) {
      System.out.println(ex);
    }
    return aggregateList;
  }
}
