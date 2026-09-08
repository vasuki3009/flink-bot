from fastapi import FastAPI
from pydantic import BaseModel
import numpy as np
import joblib
import os

app = FastAPI()

# Load trained Random Forest model
MODEL_PATH = "phishing_model.pkl"
model = None

if os.path.exists(MODEL_PATH):
    model = joblib.load(MODEL_PATH)
    print(f"✅ Loaded trained model: {MODEL_PATH}")
else:
    print("⚠️ No trained model found! Run train_model.py first")

# Import feature extractor
from feature_extractor import extract_features

class URLRequest(BaseModel):
    url: str

def analyze_url(url: str):
    try:
        if model is None:
            return {
                "url": url,
                "score": 0,
                "level": "SAFE",
                "confidence": 0,
                "model": "No model loaded",
                "error": "Run train_model.py first"
            }

        # Extract features using your existing feature_extractor
        features = extract_features(url)
        features_array = np.array([list(features.values())])

        # Get prediction and probability
        prediction = model.predict(features_array)[0]
        probability = model.predict_proba(features_array)[0]

        # probability[1] = phishing probability
        phishing_prob = probability[1] if len(probability) > 1 else probability[0]
        score = int(phishing_prob * 100)

        if score >= 70:
            level = "DANGEROUS"
        elif score >= 35:
            level = "SUSPICIOUS"
        else:
            level = "SAFE"

        return {
            "url": url,
            "score": score,
            "level": level,
            "confidence": round(phishing_prob * 100, 2),
            "prediction": int(prediction),
            "features_used": len(features),
            "model": "Random Forest (trained on real data)"
        }

    except Exception as e:
        print(f"❌ Error analyzing {url}: {e}")
        return {
            "url": url,
            "score": 0,
            "level": "SAFE",
            "confidence": 0,
            "error": str(e)
        }

@app.post("/predict")
async def predict(request: URLRequest):
    return analyze_url(request.url)

@app.get("/predict")
async def predict_get(url: str):
    return analyze_url(url)

@app.get("/health")
def health():
    return {
        "status": "ML service running",
        "model_loaded": model is not None,
        "model_type": "Random Forest"
    }