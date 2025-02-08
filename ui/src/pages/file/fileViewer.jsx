import React, { useEffect, useState, useRef } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import "./fileViewer.css";

const FileViewer = () => {
  const { fileID } = useParams();
  const [searchParams] = useSearchParams();
  const [userID, setUserID] = useState();
  const [fileContent, setFileContent] = useState("");
  const [error, setError] = useState("");
  const [isCanvasOpen, setIsCanvasOpen] = useState(false);
  const [isSignatureExists, setSignatureExists] = useState(false);
  const [signed, setSign] = useState(true);
  const [signature, setSignature] = useState(null);
  const canvasRef = useRef(null);
  const ctxRef = useRef(null);
  const [isDrawing, setIsDrawing] = useState(false);

  useEffect(() => {
    const userID = searchParams.get("userID");
    setUserID(userID);

    if (!userID) {
      setError("User ID is missing in the URL.");
      return;
    }

    const fetchFile = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/files/${fileID}?userID=${userID}`
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const contentType = response.headers.get("Content-Type");

        if (contentType.includes("application/json")) {
          const data = await response.json();
          const byteArray = Uint8Array.from(atob(data.fileContent), (c) =>
            c.charCodeAt(0)
          );

          const fileBlob = new Blob([byteArray], { type: "application/pdf" });
          const fileURL = URL.createObjectURL(fileBlob);
          setFileContent(fileURL);

          let listSign = data.signedBy.split("/");
          if (listSign.includes(userID)) {
            setSign(false);
          }
        } else {
          const content = await response.text();
          setFileContent(content);
        }
      } catch (err) {
        setError(err.message);
      }
    };

    const storeSignature = async () => {
      const response = await fetch(
        `http://localhost:8080/signature/storeSign?userID=${userID}`
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      try {
        const data = await response.json();
        setSignature(data);
      } catch (error) {
        console.log("No signature found");
      }
    };

    storeSignature();
    fetchFile();
  }, [fileID, searchParams]);

  const openCanvas = () => {
    if (signature == null) {
      setIsCanvasOpen(true);
      setTimeout(() => {
        const canvas = canvasRef.current;
        if (canvas) {
          const ctx = canvas.getContext("2d");
          ctx.strokeStyle = "black";
          ctx.lineWidth = 2;
          ctx.clearRect(0, 0, canvas.width, canvas.height);
          ctxRef.current = ctx;
        }
      }, 0);
    } else {
      setSignatureExists(true);
    }
  };

  const closeCanvas = () => {
    setIsCanvasOpen(false);
    setSignatureExists(false);
  };

  const startDrawing = (e) => {
    if (!ctxRef.current) return;
    ctxRef.current.beginPath();
    ctxRef.current.moveTo(e.nativeEvent.offsetX, e.nativeEvent.offsetY);
    setIsDrawing(true);
  };

  const draw = (e) => {
    if (!isDrawing || !ctxRef.current) return;
    ctxRef.current.lineTo(e.nativeEvent.offsetX, e.nativeEvent.offsetY);
    ctxRef.current.stroke();
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearCanvas = () => {
    let canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  };

  const saveSignature = async () => {
    const documentID = fileID;

    if (!userID || !documentID) {
      setError("Missing user ID or document ID.");
      return;
    }

    const canvas = canvasRef.current;
    const signatureData = canvas.toDataURL().split(",")[1];

    try {
      const response = await fetch("http://localhost:8080/signature/saveSign", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          documentID: documentID,
          userID: userID,
          signatureData: signatureData,
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      alert("Signature saved successfully!");
      setSignature(`data:image/png;base64,${signatureData}`);

      closeCanvas();
    } catch (err) {
      setError(err.message);
    }
  };

  const confirmSignature = async () => {
    if (confirm("Are you sure want to sign this document ?")) {
      try {
        const response = await fetch(
          "http://localhost:8080/signature/signDocument",
          {
            method: "POST",
            body: new URLSearchParams({
              documentID: fileID,
              userID: userID,
              signatureData: signature.signatureData,
            }),
          }
        );

        if (!response.ok) {
          throw new Error(await response.text());
        } else {
          const data = await response.text();
          window.location.reload();
          console.log("Sign document: " + data);
        }
      } catch (error) {
        setError(error.message);
        console.log(error.message);
      }
    } else {
      console.log("Sign document: OK");
    }
  };

  return (
    <div className="file-viewer">
      <div className="header">
        {signed ? (
          <button onClick={openCanvas}>Sign Document</button>
        ) : (
          <button
            style={{
              backgroundColor: "#754a0d",
              cursor: "You already signed",
              opacity: 0.6,
            }}
            title="You have already signed !"
          >
            Sign Document
          </button>
        )}
      </div>

      <div className="iframe-container">
        <iframe src={fileContent} title="PDF Viewer"></iframe>
      </div>

      {isCanvasOpen && (
        <div className="signature-canvas">
          <h3>Sign Below</h3>
          <canvas
            ref={canvasRef}
            width="500"
            height="200"
            onMouseDown={startDrawing}
            onMouseMove={draw}
            onMouseUp={stopDrawing}
            onMouseLeave={stopDrawing}
          ></canvas>
          <div className="canvas-controls btn">
            <button className="confirm_btn" onClick={saveSignature}>
              Save Signature
            </button>
            <button className="clear_btn" onClick={clearCanvas}>
              Clear template
            </button>
            <button className="cancel_btn" onClick={closeCanvas}>
              Cancel
            </button>
          </div>
        </div>
      )}

      {isSignatureExists && (
        <div className="signature-preview">
          <h3>Signature Preview:</h3>
          <img
            src={`data:image/png;base64,${signature.signatureData}`}
            alt="Signature Preview"
          />
          <div className="btn">
            <button
              className="confrim_btn"
              onClick={() => {
                closeCanvas();
                confirmSignature();
              }}
            >
              Confirm signature
            </button>
            <button
              className="cancel_btn"
              onClick={() => {
                closeCanvas();
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FileViewer;
