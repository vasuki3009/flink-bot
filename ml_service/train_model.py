import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, confusion_matrix
from sklearn.ensemble import RandomForestClassifier
import joblib

print("=" * 50)
print("TRAINING FAKE LINK DETECTION MODEL")
print("=" * 50)

# Step 1: Load dataset
print("\n[1/4] Loading dataset...")
df = pd.read_csv("features_dataset.csv")

# Separate features and label
X = df.drop("label", axis=1)
y = df["label"]

print(f"✓ Total samples: {len(df)}")

# Step 2: Split data
print("\n[2/4] Splitting dataset...")
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Step 3: Train model
print("\n[3/4] Training model...")
model = RandomForestClassifier()
model.fit(X_train, y_train)

print("✓ Model trained successfully!")

# Step 4: Test model
print("\n[4/4] Testing model...")
y_pred = model.predict(X_test)

accuracy = accuracy_score(y_test, y_pred)
print(f"\nAccuracy: {accuracy * 100:.2f}%")

# Confusion Matrix
cm = confusion_matrix(y_test, y_pred)
print("\nConfusion Matrix:")
print(cm)

# Save model
joblib.dump(model, "phishing_model.pkl")
print("\n✓ Model saved as phishing_model.pkl")

# Test with sample URL
from feature_extractor import extract_features

print("\nTesting sample URLs...")

test_urls = [
    "https://google.com",
    "http://login-bank.xyz"
]

for url in test_urls:
    features = extract_features(url)
    features_array = np.array([list(features.values())])

    prediction = model.predict(features_array)[0]

    result = "🚨 PHISHING" if prediction == 1 else "✅ SAFE"
    print(f"{url} → {result}")