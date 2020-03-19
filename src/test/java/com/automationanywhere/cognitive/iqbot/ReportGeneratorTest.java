package com.automationanywhere.cognitive.iqbot;

import static org.testng.Assert.*;

import com.automationanywhere.cognitive.iqbot.parser.impl.ReadCSV;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.testng.annotations.Test;

public class ReportGeneratorTest {

  @InjectMocks
  private ReportReader rg;

  @Before
  public void setup() {
    rg = new ReportReader("D:\\Perf Test Docs\\final docs\\aggregate", ".csv", new ReadCSV());
  }

  @Test
  public void testProcessor() {
  }

  @Test
  public void testGetFileName() {
    String name = rg.getFileName("D:\\file.txt");
    assertEquals(name, "file");
  }
}