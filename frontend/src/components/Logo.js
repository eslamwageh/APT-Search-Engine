import { useContext } from "react";
import ThemeContext from "ThemeContext";

import whiteLogo from "imgs/sherlock-white.svg";
import darkLogo from "imgs/sherlock-black.svg";

import "./Logo.css"

function Logo({ showText=true, size=25}) {
  const { isDark } = useContext(ThemeContext);
  return (
    <>
      <img
        src={isDark ? whiteLogo : darkLogo}
        className="App-logo"
        alt="logo"
        style={{height:`${size}vmin`}}
      />
      {showText && <p id="wordmark">Mr Searchlock Holmes</p>}
    </>
  );
}

export default Logo;
