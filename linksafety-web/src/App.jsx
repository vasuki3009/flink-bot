import { useState } from "react";
import axios from "axios";

export default function App() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const checkLink = async () => {
    if (!url) return;
    setLoading(true);
    setResult(null);
    try {
      const res = await axios.post("http://localhost:8017/api/analyze", {
        url,
      });
      setResult(res.data);
    } catch (err) {
      setResult({ error: "Could not connect to server!" });
    }
    setLoading(false);
  };

  const getRiskColor = (score) => {
    if (score >= 60) return "#ff4444";
    if (score >= 30) return "#ffaa00";
    return "#00cc44";
  };

  const getRiskLabel = (level) => {
    if (level === "DANGEROUS") return "🔴 DANGEROUS";
    if (level === "SUSPICIOUS") return "🟡 SUSPICIOUS";
    return "🟢 SAFE";
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #0f0c29, #302b63, #24243e)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          background: "rgba(255,255,255,0.05)",
          borderRadius: "20px",
          padding: "40px",
          width: "500px",
          boxShadow: "0 8px 32px rgba(0,0,0,0.3)",
          border: "1px solid rgba(255,255,255,0.1)",
        }}
      >
        <h1
          style={{ color: "#fff", textAlign: "center", marginBottom: "10px" }}
        >
          🛡️ LinkSafety
        </h1>
        <p style={{ color: "#aaa", textAlign: "center", marginBottom: "30px" }}>
          Paste any link to check if it's safe
        </p>

        <input
          type="text"
          placeholder="https://example.com"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && checkLink()}
          style={{
            width: "100%",
            padding: "14px",
            borderRadius: "10px",
            border: "1px solid rgba(255,255,255,0.2)",
            background: "rgba(255,255,255,0.1)",
            color: "#fff",
            fontSize: "16px",
            marginBottom: "15px",
            boxSizing: "border-box",
          }}
        />

        <button
          onClick={checkLink}
          disabled={loading}
          style={{
            width: "100%",
            padding: "14px",
            borderRadius: "10px",
            border: "none",
            background: loading
              ? "#555"
              : "linear-gradient(90deg, #6c63ff, #3f3d99)",
            color: "#fff",
            fontSize: "16px",
            cursor: loading ? "not-allowed" : "pointer",
            fontWeight: "bold",
          }}
        >
          {loading ? "Analyzing... ⏳" : "Check Link 🔍"}
        </button>

        {result && !result.error && (
          <div
            style={{
              marginTop: "25px",
              padding: "20px",
              borderRadius: "12px",
              background: "rgba(255,255,255,0.05)",
              border: `1px solid ${getRiskColor(result.score)}`,
            }}
          >
            <h2
              style={{ color: getRiskColor(result.score), textAlign: "center" }}
            >
              {getRiskLabel(result.level)}
            </h2>
            <p
              style={{
                color: "#fff",
                textAlign: "center",
                fontSize: "24px",
                fontWeight: "bold",
              }}
            >
              Risk Score: {result.score}/100
            </p>
            <ul style={{ color: "#aaa", textAlign: "left", marginTop: "10px" }}>
              {result.reasons &&
                result.reasons.map((r, i) => <li key={i}>{r}</li>)}
            </ul>
          </div>
        )}

        {result?.error && (
          <div
            style={{
              marginTop: "25px",
              padding: "20px",
              borderRadius: "12px",
              background: "rgba(255,0,0,0.1)",
              border: "1px solid #ff4444",
              color: "#ff4444",
              textAlign: "center",
            }}
          >
            ❌ {result.error}
          </div>
        )}
      </div>
    </div>
  );
}
