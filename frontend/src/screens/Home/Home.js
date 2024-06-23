import { Link } from "react-router-dom";

import Logo from "../../components/Logo";
import Input from "../../components/Input";

import ThemeButton from "components/ThemeButton";
import "./Home.css";

function App() {
  const SherlockLocation = "https://maps.app.goo.gl/ceR9pVoZEzKFwtLD7";
  return (
    <div className="App">
      <header className="App-header">
        <div></div>
        <div id="Logo-and-Input-Home">
          <Logo/>
          <Input />
        </div>
        <p id="Input-Post-Text">
          Send me your query, and surely the truth will be unveiled, or you may pay me a visit at{" "}
          <a href={SherlockLocation} target="_blank" rel="noopener noreferrer" className="Link">
            221B Baker Street
          </a>
        </p>
        <ThemeButton/>
      </header>
    </div>
  );
}

export default App;
