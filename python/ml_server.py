"""
Python ML Server — does ONE job only: ML prediction.
Java handles everything else.

Run: python ml_server.py
"""
from flask import Flask, request, jsonify
import pickle, os, math, re
from urllib.parse import urlparse

app = Flask(__name__)

# ── Load or train model ──────────────────────────────────────────────────────
MODEL_PATH = "ml_model.pkl"

def get_entropy(text):
    if not text: return 0
    from collections import Counter
    freq = Counter(text)
    return -sum((c/len(text)) * math.log2(c/len(text)) for c in freq.values())

def extract_features(url):
    """Minimal feature extraction for ML model — Java does the full version."""
    u = url.lower()
    parsed = urlparse(url)
    host = parsed.netloc.replace("www.", "")
    parts = host.split(".")
    domain = parts[-2] if len(parts) >= 2 else host
    tld    = parts[-1] if len(parts) > 1 else ""
    SUSP   = ['login','verify','account','free','win','banking','secure','update']
    STLDS  = ['tk','ml','ga','cf','gq','xyz','top','club']
    return [
        len(url),
        url.count('.'),
        url.count('-'),
        1 if not url.startswith('https') else 0,
        1 if re.match(r'\d+\.\d+\.\d+\.\d+', host) else 0,
        sum(w in u for w in SUSP),
        1 if tld in STLDS else 0,
        round(get_entropy(domain), 3),
        1 if '@' in url else 0,
    ]

def load_or_train_model():
    if os.path.exists(MODEL_PATH):
        with open(MODEL_PATH, "rb") as f:
            return pickle.load(f)
    # Train a simple model if none exists
    from sklearn.ensemble import RandomForestClassifier
    import numpy as np
    # Sample training data
    phishing = [
        "http://paypal-secure-login.xyz/verify",
        "http://192.168.1.1/banking/login",
        "http://amazon-prize.tk/free",
        "http://win-iphone.club/claim",
        "http://secure-login-facebook.ml/confirm",
        "http://google-account-verify.xyz/update",
        "http://apple-id-suspended.cf/restore",
        "http://netflix-billing.ga/fix",
    ]
    safe = [
        "https://google.com", "https://amazon.com",
        "https://github.com", "https://stackoverflow.com",
        "https://youtube.com", "https://linkedin.com",
        "https://microsoft.com", "https://apple.com",
    ]
    X = [extract_features(u) for u in phishing + safe]
    y = [1]*len(phishing) + [0]*len(safe)
    model = RandomForestClassifier(n_estimators=50, random_state=42)
    model.fit(X, y)
    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model, f)
    print("✅ ML model trained and saved!")
    return model

model = load_or_train_model()

# ── API endpoints ────────────────────────────────────────────────────────────

@app.route("/predict", methods=["POST"])
def predict():
    """Java calls this — returns only the ML score (0.0 to 1.0)."""
    url = request.json.get("url", "")
    if not url:
        return jsonify({"error": "No URL"}), 400
    features = [extract_features(url)]
    ml_score = float(model.predict_proba(features)[0][1])
    return jsonify({"ml_score": round(ml_score, 3)})

@app.route("/health")
def health():
    return jsonify({"status": "ok", "model": "RandomForest", "role": "ML prediction only"})

if __name__ == "__main__":
    print("🟣 Python ML Server starting on port 5000...")
    print("   Role: ML prediction only (Java handles everything else)")
    app.run(port=5000, debug=False)
