import re

def extract_features(url):
    features = {}

    # Clean URL
    url_lower = url.lower()
    domain = re.sub(r'https?://', '', url).split('/')[0]
    path = '/'.join(url.split('/')[3:])

    # 1. URL length
    features["url_length"] = len(url)

    # 2. Has HTTPS
    features["has_https"] = 1 if url.startswith('https') else 0

    # 3. Dot count
    features["dot_count"] = url.count('.')

    # 4. Suspicious words
    suspicious_words = ["login", "verify", "bank", "secure", "account",
                        "update", "confirm", "suspended", "unlock", "claim",
                        "prize", "free", "winner", "gift", "password",
                        "payment", "urgent", "alert", "reward", "phishing"]
    features["suspicious_word"] = 1 if any(
        word in url_lower for word in suspicious_words) else 0

    # 5. Has IP address
    features["has_ip"] = 1 if re.search(
        r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}', url) else 0

    # 6. Hyphen in domain
    features["hyphen_in_domain"] = 1 if '-' in domain else 0

    # 7. Suspicious TLD
    suspicious_tlds = ['.tk', '.ml', '.ga', '.cf', '.gq', '.xyz',
                       '.click', '.top', '.win', '.loan', '.online',
                       '.site', '.club', '.co.cc']
    features["suspicious_tld"] = 1 if any(
        tld in url_lower for tld in suspicious_tlds) else 0

    # 8. URL shortener
    shorteners = ['bit.ly', 'tinyurl', 't.co', 'goo.gl',
                  'ow.ly', 'cutt.ly', 'rb.gy']
    features["is_shortener"] = 1 if any(
        s in url_lower for s in shorteners) else 0

    # 9. Digit count
    features["digit_count"] = sum(c.isdigit() for c in url)

    # 10. Special char count
    features["special_char_count"] = sum(
        c in '@_=?&%#' for c in url)

    # 11. Subdomain count
    features["subdomain_count"] = max(0, len(domain.split('.')) - 2)

    # 12. Path length
    features["path_length"] = len(path)

    # 13. Brand in subdomain (typosquatting)
    brands = ['paypal', 'amazon', 'google', 'facebook', 'apple',
              'microsoft', 'netflix', 'instagram', 'twitter',
              'sbi', 'hdfc', 'icici', 'nios', 'irctc']
    subdomain_part = domain.split('.')[0] if '.' in domain else ''
    features["brand_in_subdomain"] = 1 if any(
        brand in subdomain_part.lower() for brand in brands) else 0

    # 14. Number substitution
    features["number_substitution"] = 1 if re.search(
        r'[a-z]0[a-z]|[a-z]1[a-z]', url_lower) else 0

    # 15. Fake domain pattern
    fake_patterns = ['nios-ac', 'old-nios', 'sbi-net', 'hdfc-net',
                     'icici-bank', 'paypal-', 'amazon-', 'flipkart-',
                     'phishing', 'scam-', '-fake', 'fraud-']
    features["fake_domain_pattern"] = 1 if any(
        p in url_lower for p in fake_patterns) else 0

    # 16. No HTTPS (HTTP only)
    features["is_http"] = 1 if url.startswith('http://') else 0

    # 17. Long URL
    features["is_long_url"] = 1 if len(url) > 75 else 0

    # 18. Has @ symbol
    features["has_at_symbol"] = 1 if '@' in url else 0

    return features


if __name__ == "__main__":
    import pandas as pd
    df = pd.read_csv("dataset.csv")
    feature_list = []
    for url in df["url"]:
        feature_list.append(extract_features(url))
    features_df = pd.DataFrame(feature_list)
    features_df["label"] = df["label"]
    features_df.to_csv("features_dataset.csv", index=False)
    print(f"✅ Features extracted! Shape: {features_df.shape}")