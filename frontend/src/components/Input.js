import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import send from "imgs/send.svg";
import "./Input.css";

function Input() {
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(useLocation().search);
  let initialQuery = queryParams.get("query") || "";
  const [query, setQuery] = useState(initialQuery);
  const sendQuery = () =>
    navigate(`/results?query=${encodeURIComponent(query)}`);
  const insertQuery = (e) => setQuery(e.target.value);

  useEffect(() => {
    initialQuery = queryParams.get("query") || "";
    setQuery(initialQuery);
  }, [queryParams.get("query")]);

  return (
    <div id="Input-Field-Container">
      <input
        type="text"
        placeholder="Enter your Query"
        id="Query-Input"
        onChange={insertQuery}
        value={query}
        onKeyDown={event => {
          if (event.key === 'Enter') {
            sendQuery();
          }
        }}
      />
      <div id="Send-Button-Container" onClick={sendQuery}>
        <img src={send} id="Send-Button" alt="The query sending button"></img>
      </div>
    </div>
  );
}

export default Input;
