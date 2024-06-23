import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import React, { useState } from "react";
import ThemeContext from "ThemeContext";
import Home from "./screens/Home/Home";
import Results from "screens/Results/Results";

function App() {
  const [isDark, setIsDark] = useState(1);
  const setDark = () => {
    setIsDark(1);
    document.querySelector('body').setAttribute('data-theme', 'dark');
  }
  const setLight = () => {
    setIsDark(0);
    document.querySelector('body').setAttribute('data-theme', 'light');
  }

  return (
    <ThemeContext.Provider value={{ isDark, setDark, setLight }}>
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/results" element={<Results/>} />
        </Routes>
      </Router>
    </ThemeContext.Provider>
  );
}

export default App;
