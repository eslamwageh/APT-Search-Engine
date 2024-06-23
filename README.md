# Cairo University Advanced Programming Search Engine

## Introduction

This repository contains the source code for a Crawler-based search engine developed as a project for the Advanced Programming Techniques course at Cairo University. The project demonstrates the core functionalities of a search engine:

* Web crawling
* Indexing
* Ranking
* Query processing

## Features

**Crawling:**

* Multithreaded for efficiency.
* Respects robots.txt exclusion rules.
* Resumable crawls after interruptions.
* Crawls up to 6000 pages (adjustable).
* Prioritizes crawlable URLs (e.g., HTML).

**Indexing:**

* Stores words and their importance (title, header, plain text).
* Fast retrieval of documents containing specific words.
* Supports incremental updates for new documents.
* Persistent storage using a chosen data structure or database.

**Query Processing:**

* Preprocesses queries (e.g., stemming).
* Searches for documents containing words or their stems.
* Supports exact-order phrase searching with quotation marks.

**Ranking:**

* Considers relevance (TF-IDF, query word placement) and popularity (potential use of PageRank).
* Combines relevance and popularity scores for a final ranking.

**Web Interface:**

* Accepts user queries and displays ranked search results.
* Includes snippets with query words (bolded).
* Presents results similar to Google/Bing search pages.
* Displays search operation time.
* Supports pagination (e.g., 10 results per page).


## Implementation Details

* **Frontend:** React
* **Beckend:** Spring Boot (Java framework)
* **Database:** MongoDB (NoSQL document database)

<p align="center"> <a href="https://git-scm.com/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/git-scm/git-scm-icon.svg" alt="git" width="40" height="40"/> </a><a href="https://www.w3schools.com/css/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/css3/css3-original-wordmark.svg" alt="css3" width="40" height="40"/> </a>  <a href="https://www.w3.org/html/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/html5/html5-original-wordmark.svg" alt="html5" width="40" height="40"/> </a> <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/javascript/javascript-original.svg" alt="javascript" width="40" height="40"/> </a> <a href="https://reactjs.org/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/react/react-original-wordmark.svg" alt="react" width="40" height="40"/> </a><a href="https://www.mongodb.com/" target="_blank" rel="noreferrer"> <img src="https://user-images.githubusercontent.com/25181517/182884177-d48a8579-2cd0-447a-b9a6-ffc7cb02560e.png" alt="mongo" width="40" height="40"/> </a> <a href="https://spring.io/projects/spring-boot" target="_blank" rel="noreferrer"> <img src="https://user-images.githubusercontent.com/25181517/183891303-41f257f8-6b3d-487c-aa56-c497b880d0fb.png" alt="spring boot" width="40" height="40"/> </a> <a href="https://www.java.com/en/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/jmnote/z-icons/master/svg/java.svg" alt="Java" width="80" height="40"/> </a> </p>




## Initialization

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/eslamwageh/APT-Search-Engine.git
   ```

## Team: 
<table align='center'>
<tr>
    <td align="center" style="word-wrap: break-word; width: 150.0; height: 150.0">
        <a href=https://github.com/eslamwageh>
            <img src=https://avatars.githubusercontent.com/u/53353517?v=4 width="100;"  style="border-radius:50%;align-items:center;justify-content:center;overflow:hidden;padding-top:10px">
            <br />
            <sub style="font-size:14px"><b>Eslam Wageh</b></sub>
        </a>
    </td>
    <td align="center" style="word-wrap: break-word; width: 150.0; height: 150.0">
        <a href=https://github.com/Ashraf-Bahy>
            <img src=https://avatars.githubusercontent.com/u/111181298?v=4 width="100;"  style="border-radius:50%;align-items:center;justify-content:center;overflow:hidden;padding-top:10px">
            <br />
            <sub style="font-size:14px"><b>Ashraf Bahy</b></sub>
        </a>
    </td>
    <td align="center" style="word-wrap: break-word; width: 150.0; height: 150.0">
        <a href=https://github.com/Adham-hussin>
            <img src=https://avatars.githubusercontent.com/u/67987638?v=4 width="100;"  style="border-radius:50%;align-items:center;justify-content:center;overflow:hidden;padding-top:10px">
            <br />
            <sub style="font-size:14px"><b>Adham hussin</b></sub>
        </a>
    </td>
    <td align="center" style="word-wrap: break-word; width: 150.0; height: 150.0">
        <a href=https://github.com/Ahmed-Aladdiin>
            <img src=https://avatars.githubusercontent.com/u/118504851?v=4 width="100;"  style="border-radius:50%;align-items:center;justify-content:center;overflow:hidden;padding-top:10px">
            <br />
            <sub style="font-size:14px"><b>Ahmed Aladdin</b></sub>
        </a>
    </td>
</tr>
</table>
