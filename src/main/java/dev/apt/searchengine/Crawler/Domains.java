package dev.apt.searchengine.Crawler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Domains {
    Domains () {
        String content;
        String domains = System.getProperty("user.dir") + "/domains.json";
        try {
            content = new String(Files.readAllBytes(Paths.get(domains)));
            JSONObject jsonObject = new JSONObject(content);

            newsDomains = toList(jsonObject.getJSONArray("newsDomains"));
            socialMediaDomains = toList(jsonObject.getJSONArray("socialMediaDomains"));
            shoppingDomains = toList(jsonObject.getJSONArray("shoppingDomains"));
            sportsDomains = toList(jsonObject.getJSONArray("sportsDomains"));
            educationDomains = toList(jsonObject.getJSONArray("educationDomains"));
            scienceDomains = toList(jsonObject.getJSONArray("scienceDomains"));
            programmingToolsDomains = toList(jsonObject.getJSONArray("programmingToolsDomains"));
            healthDomains = toList(jsonObject.getJSONArray("healthDomains"));
            streamingDomains = toList(jsonObject.getJSONArray("streamingDomains"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> toList(JSONArray jsonArray) {
        return Arrays.asList(jsonArray.toString().replace("[", "").replace("]", "").split(","));
    }

    public List<String> newsDomains;
    public List<String> socialMediaDomains;
    public List<String> shoppingDomains;
    public List<String> sportsDomains;
    public List<String> educationDomains;
    public List<String> scienceDomains;
    public List<String> programmingToolsDomains;
    public List<String> healthDomains;
    public List<String> streamingDomains;
}
