package dev.apt.searchengine.Crawler;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

public class CrawlerData {
	// Locks
	private Object crawledPagesNumLock;
	// Containers
	public Queue<String> seedURLs;
	public Set<String> uniqueURLs;
	// variables
	int crawledPagesNum;
	CrawlerDB db;

	public CrawlerData(CrawlerDB db) {
		this.db = db;
		LinkedList<String> seeds = db._fetchSeed();
		// initialize the Locks
		crawledPagesNumLock = new Object();
		// initialize the containers
		seedURLs = seeds;
		uniqueURLs = new HashSet<>();
		for (String s : seeds) uniqueURLs.add(s);
		// initialize variables
		crawledPagesNum = seeds.size();
	}

	// public void setCrawledPagesNum(int num) {
	// 	synchronized (crawledPagesNumLock) {
	// 		crawledPagesNum = num;
	// 	}
	// }

	public void increaseCrawledPagesNum() {
		synchronized (crawledPagesNumLock) {
			crawledPagesNum += 1;
		}
	}

	public int getCrawledPagesNum() {
		return crawledPagesNum;
	}

	// public void setSeeds(LinkedList<String> incomingSeeds){
	// 	synchronized(seedURLs) {
	// 		seedURLs = incomingSeeds;
	// 	}
	// }

	public void addSeed(String seed) {
		synchronized (seedURLs) {
			seedURLs.add(seed);
		}
	}

	public String getSeed() {
		synchronized (seedURLs) {
			if (seedURLs.isEmpty())
				return null;
			else
				return seedURLs.poll();
		}
	}

	public boolean isSeedsFinished()
	{
		synchronized (seedURLs) {
			return seedURLs.isEmpty();
		}
	}

	public boolean isUniqueURL(String url) {
		synchronized (uniqueURLs) {
			return !uniqueURLs.contains(url);
		}
	}

	public void addUniqueURL(String url) {
		synchronized (uniqueURLs) {
			uniqueURLs.add(url);
		}
	}
}