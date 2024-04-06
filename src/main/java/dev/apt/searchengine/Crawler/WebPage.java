package dev.apt.searchengine.Crawler;
public class WebPage {
  public WebPage(String URL, String compactString, String category, boolean isRefreshed) {
      this.URL = URL;
      this.compactString = compactString;
      this.category = category;
      this.isRefreshed = isRefreshed;
  }
  int id;
  public String URL;
  public String compactString;
  public String category;
  public boolean isRefreshed;
}