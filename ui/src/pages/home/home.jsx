import React, { useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { Link } from "react-router-dom";
import "./home.css";

const HomePage = () => {
  const [myFiles, setMyFiles] = useState([]);
  const [sharedFiles, setSharedFiles] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [userID, setUserId] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("authToken");
    if (token) {
      const userInfo = jwtDecode(token);
      setUserId(userInfo.data.id);
    }
  }, []);

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        setLoading(true);

        const myFilesResponse = await fetch(
          `http://localhost:8080/files/listFile?userID=${userID}`
        );
        if (myFilesResponse.ok) {
          const myFilesData = await myFilesResponse.json();
          setMyFiles(myFilesData);
        } else {
          setError("Failed to fetch your uploaded files.");
        }

        const sharedFilesResponse = await fetch(
          `http://localhost:8080/files/shareTo?userID=${userID}`
        );
        if (sharedFilesResponse.ok) {
          const sharedFilesData = await sharedFilesResponse.json();
          setSharedFiles(sharedFilesData);
        } else {
          setError("Failed to fetch shared files.");
        }
      } catch (err) {
        setError("Error fetching files: " + err.message);
      } finally {
        setLoading(false);
      }
    };

    if (userID) {
      fetchFiles();
    }
  }, [userID]);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedFile(null);
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      alert("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append("userID", userID);

    try {
      setLoading(true);
      const response = await fetch("http://localhost:8080/files/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        console.log("File uploaded successfully");
        closeModal();

        const updatedFilesResponse = await fetch(
          `http://localhost:8080/files/listFile?userID=${userID}`
        );
        if (updatedFilesResponse.ok) {
          const updatedFilesData = await updatedFilesResponse.json();
          setMyFiles(updatedFilesData);
        }
      } else {
        setError("Failed to upload file");
      }
    } catch (err) {
      setError("Error uploading file: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="home-container">
      <h1>Home Page</h1>
      {error && <p className="error-message">{error}</p>}
      <button className="upload-file-btn" onClick={openModal}>
        Upload File
      </button>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="files-section">
          <div className="my-files">
            <h2>Files avaliable</h2>
            {myFiles.length === 0 ? (
              <p>No files uploaded yet.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>My Files</th>
                  </tr>
                </thead>
                <tbody>
                  {myFiles.map((file) => (
                    <tr key={file.id}>
                      <td>
                        <Link to={`/files/${file.id}?userID=${userID}`} target="_blank">
                          {file.documentName}
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
          <div className="shared-files">
            <h2>Shared Files</h2>
            {sharedFiles.length === 0 ? (
              <p>No files shared with you yet.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>File Name</th>
                    <th>Shared By</th>
                  </tr>
                </thead>
                <tbody>
                  {sharedFiles.map((file) => (
                    <tr key={file.id}>
                      <td>
                        <Link to={`/files/${file.id}?userID=${userID}`}>
                          {file.documentName}
                        </Link>
                      </td>
                      <td>{file.ownerName}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {isModalOpen && (
        <div className="modal">
          <div className="modal-content">
            <h2>Upload File</h2>
            <form onSubmit={handleFileUpload}>
              <input
                type="file"
                onChange={(e) => setSelectedFile(e.target.files[0])}
                accept="application/pdf"
                required
              />
              <div className="modal-buttons">
                <button type="submit" disabled={loading}>
                  {loading ? "Uploading..." : "Upload"}
                </button>
                <button type="button" onClick={closeModal}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default HomePage;
