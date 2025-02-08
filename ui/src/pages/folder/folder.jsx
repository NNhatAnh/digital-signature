import React, { useEffect, useState } from 'react';
import './folder.css';

const FolderPage = ({ folderId }) => {
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const response = await fetch(`http://localhost:8080/folders/${folderId}/files`);
        if (response.ok) {
          const data = await response.json();
          setFiles(data);
        } else {
          console.error('Failed to fetch files');
        }
      } catch (error) {
        console.error('Error fetching files:', error);
      }
    };

    if (folderId) {
      fetchFiles();
    }
  }, [folderId]);

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();

    if (!selectedFile) {
      alert('Please select a file to upload');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fetch(`http://localhost:8080/folders/${folderId}/upload`, {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        console.log('File uploaded successfully');
        setSelectedFile(null);

        const updatedFiles = await fetch(`http://localhost:8080/folders/${folderId}/files`);
        if (updatedFiles.ok) {
          const data = await updatedFiles.json();
          setFiles(data);
        }
      } else {
        console.error('Error uploading file');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="folder-container">
      <h1>Folder: {folderId}</h1>

      <div className="file-upload-section">
        <h2>Upload a File</h2>
        <form onSubmit={handleFileUpload}>
          <input type="file" onChange={handleFileChange} />
          <button type="submit">Upload</button>
        </form>
      </div>

      <div className="files-list-section">
        <h2>Files in this Folder</h2>
        {files.length === 0 ? (
          <p>No files in this folder.</p>
        ) : (
          <ul>
            {files.map((file, index) => (
              <li key={index}>
                <a href={`http://localhost:8080/files/${file.id}`} target="_blank" rel="noopener noreferrer">
                  {file.name}
                </a>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default FolderPage;
