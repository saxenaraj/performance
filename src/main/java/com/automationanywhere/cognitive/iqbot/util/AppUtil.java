package com.automationanywhere.cognitive.iqbot.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class AppUtil {

  public static List<String> getHeaderList(String header) {
    return Arrays.asList(header.split(","));
  }

  public static File getOutputFile(String dir, String fileName) {
    boolean success = false;
    File directory = new File(dir);
    if (directory.exists()) {
      System.out.println("Directory already exists ...");
    } else {
      System.out.println("Directory not exists, creating now");
      success = directory.mkdir();
      if (success) {
        System.out.printf("Successfully created new directory : %s%n", dir);
      } else {
        System.out.printf("Failed to create new directory: %s%n", dir);
      }
    }
    File f = new File(dir+fileName);
    if (f.exists()) {
      System.out.println("File already exists");
    } else {
      System.out.println("No such file exists, creating now");
      try {
        success = f.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (success) {
        System.out.printf("Successfully created new file: %s%n", f);
      } else {
        System.out.printf("Failed to create new file: %s%n", f);
      }
    }
    return f.getAbsoluteFile();
  }

  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Entry.comparingByValue());

    Map<K, V> result = new LinkedHashMap<>();
    for(int i=list.size()-1; i>-1; i--) {
      Entry<K, V> entry = list.get(i);
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static double getNumberValue(String errorStr) {
    return Double.parseDouble(errorStr.replace("%", ""));
  }

  public static boolean isTransaction(String apiName) {
    boolean containsDigit = false;

    if (StringUtils.isNotBlank(apiName)) {
      if(apiName.matches(".*[_][0-9]*[\\s]*[\\/].*")) {
        containsDigit = true;
      }
    }
    return !containsDigit;
  }
}
