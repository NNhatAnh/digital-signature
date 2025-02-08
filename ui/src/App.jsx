import { useState } from "react";
import reactLogo from "./assets/react.svg";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import viteLogo from "/vite.svg";
import "./App.css";

import LoginPage from "./components/login/login";
import HomePage from "./pages/home/home";
import FileViewer from "./pages/file/fileViewer";

function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route exact path="/" element={<LoginPage />} />
          <Route exact path="/home" element={<HomePage />} />
          <Route exact path="/files/:fileID" element={<FileViewer />} />
        </Routes>
      </Router>
    </>
  );
}

export default App;
