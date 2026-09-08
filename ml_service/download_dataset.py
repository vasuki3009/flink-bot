import pandas as pd
from feature_extractor import extract_features

# ── Phishing URLs (label = 1) ──
phishing_urls = [
    "http://paypal-verify.tk/login",
    "http://amaz0n-prizes.tk/claim",
    "http://sbi-netbanking-verify.ml/login",
    "http://apple-id-suspended.cf/unlock",
    "http://secure-banking-update.ml/verify",
    "http://whatsapp-free-gift.ga/claim",
    "http://netflix-account-suspended.tk",
    "http://free-money-win-now.click",
    "http://nios-ac.in",
    "http://old-nios-ac.in",
    "http://login-bank.xyz",
    "http://verify-paypal-alert.com",
    "http://paypal.com.phishing-site.net/login",
    "http://apple.com-id-verify.net/signin",
    "http://accounts.google.com.verify-now.tk",
    "http://amazon.com-deals-today.net/offer",
    "http://secure.paypal.com.phishing-site.net",
    "http://login.facebook.com-security.net",
    "http://192.168.1.1/bank/login",
    "http://bit.ly/3xFreeGift",
    "http://tinyurl.com/freeiphone15",
    "http://hdfc-netbanking-verify.ml",
    "http://icici-bank-verify.cf/login",
    "http://sretaw.com/login",
    "http://toysrus.com.co/offer",
    "http://br-icloud.com.br/verify",
    "http://www.pashminaonline.com/pure-pashminas",
    "http://mp3raid.com/music/krizz_kaliko.html",
    "http://killsometime.com/games/free",
    "http://blog.demo.net/dashboard/login",
    "http://www.realtytrac.com.co/property",
    "http://microsoft.com-update-required.com",
    "http://support.apple.com-verify.tk/id",
    "http://login.amazon.com-secure.net",
    "http://update.paypal.com-security.net",
    "http://signin.ebay.com-verify.tk",
    "http://secure.hdfc.com-netbanking.ml",
    "http://verify.sbi.com-update.cf",
    "http://nios.ac.in-examresult.in",
    "http://pmkisan-gov.in/beneficiary",
    "http://192.168.0.1/admin/login",
    "http://10.0.0.1/bank",
    "http://paytm-offers.in/cashback",
    "http://flipkart-sale.in/discount",
    "http://amazon-offers.in/deal",
    "http://irctc-railways.in/booking",
    "http://gov-yojana.in/apply",
    "http://bit.ly/win-prize-2024",
    "http://tinyurl.com/claim-reward",
    "http://goo.gl/free-gift",
]

# ── Safe URLs (label = 0) ──
safe_urls = [
    "https://www.google.com",
    "https://www.youtube.com",
    "https://www.github.com",
    "https://www.amazon.in",
    "https://www.flipkart.com",
    "https://www.wikipedia.org",
    "https://www.stackoverflow.com",
    "https://www.linkedin.com",
    "https://www.twitter.com",
    "https://www.instagram.com",
    "https://www.microsoft.com",
    "https://www.apple.com",
    "https://www.paypal.com",
    "https://www.netflix.com",
    "https://www.spotify.com",
    "https://nios.ac.in",
    "https://www.sbi.co.in",
    "https://www.hdfcbank.com",
    "https://www.icicibank.com",
    "https://www.irctc.co.in",
    "https://www.paytm.com",
    "https://www.myntra.com",
    "https://www.zomato.com",
    "https://www.swiggy.com",
    "https://www.ola.com",
    "https://docs.python.org",
    "https://spring.io/projects",
    "https://mvnrepository.com",
    "https://www.w3schools.com",
    "https://developer.mozilla.org",
    "https://www.amazon.com",
    "https://www.ebay.com",
    "https://www.reddit.com",
    "https://www.medium.com",
    "https://www.quora.com",
    "https://www.coursera.org",
    "https://www.udemy.com",
    "https://www.hackerrank.com",
    "https://www.leetcode.com",
    "https://www.geeksforgeeks.org",
    "https://www.javatpoint.com",
    "https://www.tutorialspoint.com",
    "https://www.npmjs.com",
    "https://www.docker.com",
    "https://www.atlassian.com",
    "https://www.notion.so",
    "https://www.figma.com",
    "https://www.canva.com",
    "https://www.zoom.us",
    "https://www.slack.com",
]

print("=" * 50)
print("GENERATING DATASET")
print("=" * 50)

rows = []

print("\n[1/2] Processing phishing URLs...")
for url in phishing_urls:
    try:
        features = extract_features(url)
        features['label'] = 1
        rows.append(features)
        print(f"  ✅ {url}")
    except Exception as e:
        print(f"  ❌ {url} — {e}")

print("\n[2/2] Processing safe URLs...")
for url in safe_urls:
    try:
        features = extract_features(url)
        features['label'] = 0
        rows.append(features)
        print(f"  ✅ {url}")
    except Exception as e:
        print(f"  ❌ {url} — {e}")

import pandas as pd
df = pd.DataFrame(rows)
df.to_csv("features_dataset.csv", index=False)

print(f"\n{'=' * 50}")
print(f"✅ Dataset saved! Total: {len(df)} URLs")
print(f"🔴 Phishing: {len(df[df['label']==1])}")
print(f"✅ Safe:     {len(df[df['label']==0])}")
print(f"{'=' * 50}")