package dev.apt.searchengine.Crawler;
public class WebPage {
  public WebPage(String URL, String compactString, String category, boolean isCrawled, boolean isIndexed) {
      this.URL = URL;
      this.compactString = compactString;
      this.category = category;
      this.isCrawled = isCrawled;
      this.isIndexed = isIndexed;
  }
  int id;
  public String URL;
  public String compactString;
  public String category;
  public boolean isCrawled;
  public boolean isIndexed;
}