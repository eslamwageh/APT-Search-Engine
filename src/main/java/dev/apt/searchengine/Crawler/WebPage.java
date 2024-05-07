package dev.apt.searchengine.Crawler;
public class WebPage {
  public WebPage(String URL, String compactString, String category, boolean isCrawled, boolean isIndexed, String HtmlContent) {
      this.URL = URL;
      this.compactString = compactString;
      this.category = category;
      this.isCrawled = isCrawled;
      this.isIndexed = isIndexed;
      this.HtmlContent = HtmlContent;
  }
  int id;
  public String URL;
  public String compactString;
  public String category;
  public boolean isCrawled;
  public boolean isIndexed;
  public String HtmlContent;
}