import { useContext } from "react";
import ThemeContext from "ThemeContext";

import Moon from "imgs/moon.png";
import Sun from "imgs/sun.png";

import "./ThemeButton.css"

function ThemeButton() {
  const { isDark, setDark, setLight } = useContext(ThemeContext);

  return (
    <div id="Theme-Switch" onClick={isDark ? setLight : setDark}>
      <img src={isDark ? Sun : Moon} alt="theme switching icon" />
    </div>
  );
}

export default ThemeButton;
