import { useState, useEffect } from "react";
import { useLocation } from 'react-router-dom';

import Input from "components/Input";
import Logo from "components/Logo";
import ThemeButton from "components/ThemeButton";
import ResultsList from "./sub/ResultsList";

import "./Results.css";
import Pagination from "components/pagination";

function Results() {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const query = queryParams.get("query");
  const [results, setResults] = useState([]);
  const baseURL = process.env.REACT_APP_BASE_URL;
  // pagination logic variables
  const [currentPage, setCurrentPage] = useState(1);
  const resultsPerPage = 10;
  const [pageResults, setPageResults] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  // stop watch logic variable
  const [stopWatch, setStopWatch] = useState(0);

  useEffect(() => {
    const currentTime = new Date().getTime();
    console.log("Results.js: useEffect() currentTime", currentTime);
    fetch(`${baseURL}/api/v1/query`, {
      method: "POST",
      headers: {
        'Content-Type': 'text/plain',
      },
      body: query,
    })
      .then((response) => {
        console.log("Results.js: useEffect() response", response);
        if (!response.ok) {
          setResults([]);
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        console.log("Results.js: useEffect() data", data);
        setResults(data);
        setPageResults(data.slice(0, resultsPerPage));
        setTotalPages(Math.ceil(data.length / resultsPerPage));
        const newTime = new Date().getTime();
        const timeDiff = newTime - currentTime;
        const seconds = timeDiff / 1000;
        setStopWatch(seconds);
        console.log("Results.js: useEffect() timeDiff", timeDiff);
      })
      .catch((e) => {
        console.error("Can't reach server");
      });
  }, [query]);

  useEffect(() => {
    const lastResultIndex = currentPage * resultsPerPage;
    const firstResultIndex = lastResultIndex - resultsPerPage;
    setPageResults(results.slice(firstResultIndex, lastResultIndex));
  }, [currentPage]);

  return (
    <>
      <ThemeButton />
      <div id="Gap-Container-Top">
        <div className="Gap-Container">
          <div className="Gap" />
          <div id="Logo-and-Input">
            <Logo showText={false} size={10} />
            <Input />
          </div>
        </div>
        <div id="Horizontal-line-container">
          <div id="Horizontal-Line" />
        </div>
      </div>
      <p id="Results-Count">
        {results.length} results in {stopWatch}s
      </p>
      <ResultsList results={pageResults} query={query} />
      <Pagination
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        totalPages={totalPages}
      />
    </>
  );
}

export default Results;
