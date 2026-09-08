// ============================================
//  LINK GUARD — monitor.js
//  Handles: WebSocket, popups, activity log,
//           stats counter, logout
// ============================================

// ── Auth guard ──
const currentUser = localStorage.getItem('linkguard_user');
if (!currentUser) {
  window.location.href = '/login.html';
}
// ── Platform Connection State ──
const platformState = {
  whatsapp: false,
  telegram: false
};

function toggleConnect(platform) {
  const btn       = document.getElementById(`btn-${platform}`);
  const statusEl  = document.getElementById(`status-${platform}`);
  const card      = document.getElementById(`card-${platform}`);
  const warning   = document.getElementById('connectWarning');

  if (platformState[platform]) {
    // DISCONNECT
    platformState[platform] = false;
    card.classList.remove('connected');
    statusEl.className = 'connection-status disconnected';
    statusEl.textContent = '○ Not Connected';
    btn.className = 'connect-btn';
    btn.textContent = `Connect ${platform.charAt(0).toUpperCase() + platform.slice(1)}`;

    // Save to localStorage
    localStorage.removeItem(`linkguard_${platform}`);

  } else {
    // CONNECTING animation
    btn.className = 'connect-btn connecting';
    btn.textContent = 'Connecting...';
    statusEl.className = 'connection-status connecting';
    statusEl.textContent = '◌ Connecting...';
    btn.disabled = true;

    // Simulate handshake (replace with real API call if needed)
    setTimeout(() => {
      platformState[platform] = true;
      card.classList.add('connected');
      statusEl.className = 'connection-status connected';
      statusEl.textContent = '✓ Connected';
      btn.className = 'connect-btn connected';
      btn.textContent = 'Disconnect';
      btn.disabled = false;

      // Save to localStorage
      localStorage.setItem(`linkguard_${platform}`, 'true');

      // Hide warning if at least one connected
      updateWarning();

      // Update header status
      setStatus('active', '🟢 MONITORING ACTIVE');
    }, 1500);

    return; // don't call updateWarning yet
  }

  updateWarning();
}

function updateWarning() {
  const warning = document.getElementById('connectWarning');
  const anyConnected = Object.values(platformState).some(v => v);
  if (anyConnected) {
    warning.classList.add('hidden');
  } else {
    warning.classList.remove('hidden');
    setStatus('disconnected', '🔴 NO PLATFORM CONNECTED');
  }
}

// Restore connections from localStorage on page load
function restoreConnections() {
  ['whatsapp', 'telegram'].forEach(platform => {
    if (localStorage.getItem(`linkguard_${platform}`) === 'true') {
      platformState[platform] = true;
      document.getElementById(`card-${platform}`).classList.add('connected');
      document.getElementById(`status-${platform}`).className = 'connection-status connected';
      document.getElementById(`status-${platform}`).textContent = '✓ Connected';
      document.getElementById(`btn-${platform}`).className = 'connect-btn connected';
      document.getElementById(`btn-${platform}`).textContent = 'Disconnect';
      document.getElementById('connectWarning').classList.add('hidden');
    }
  });
}

restoreConnections();
// Show logged-in user
const userBadgeEl = document.getElementById('userBadge');
if (userBadgeEl) userBadgeEl.textContent = currentUser;

// ── Stats ──
const stats = { total: 0, dangerous: 0, suspicious: 0, safe: 0 };

function updateStats(level) {
  stats.total++;
  const key = level.toLowerCase();
  if (stats[key] !== undefined) stats[key]++;

  document.getElementById('statTotal').textContent      = stats.total;
  document.getElementById('statDangerous').textContent  = stats.dangerous;
  document.getElementById('statSuspicious').textContent = stats.suspicious;
  document.getElementById('statSafe').textContent       = stats.safe;
}

// ── WebSocket (STOMP over SockJS) ──
let stompClient = null;

function connectWebSocket() {
  setStatus('reconnecting', '🔄 CONNECTING...');

  // SockJS + STOMP — matches WebSocketConfig endpoint "/ws"
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // suppress console spam

  stompClient.connect({}, function () {
    setStatus('active', '🟢 MONITORING ACTIVE');

    // Subscribe to alerts pushed by AlertService
    stompClient.subscribe('/topic/alerts', function (message) {
      const alert = JSON.parse(message.body);
      showPopup(alert);
      addToActivityLog(alert);
      updateStats(alert.level);
    });

  }, function (error) {
    console.error('STOMP error:', error);
    setStatus('disconnected', '🔴 DISCONNECTED');
    // Auto-reconnect after 4 seconds
    setTimeout(connectWebSocket, 4000);
  });
}

function setStatus(type, text) {
  const badge = document.getElementById('statusBadge');
  const label = document.getElementById('statusText');
  if (!badge || !label) return;
  badge.className = 'status-badge';
  if (type === 'reconnecting') badge.classList.add('reconnecting');
  if (type === 'disconnected')  badge.classList.add('disconnected');
  label.textContent = text;
}

// Connect on load
connectWebSocket();

// ── Popup ──
function showPopup(alert) {
  const overlay = document.getElementById('popupOverlay');
  const content = document.getElementById('popupContent');

  const level     = alert.level.toLowerCase();   // dangerous / suspicious / safe
  const icon      = level === 'dangerous' ? '🔴' : level === 'suspicious' ? '⚠️' : '✅';

  // Reasons block
  let reasonsHTML = '';
  if (alert.reasons && alert.reasons.length > 0) {
    reasonsHTML = `
      <div class="popup-reasons">
        <h4>🔍 Why it's flagged:</h4>
        <ul>${alert.reasons.map(r => `<li>${r}</li>`).join('')}</ul>
      </div>`;
  }

  // Warning banner
  let warningHTML = '';
  if (level === 'dangerous') {
    warningHTML = `<div class="popup-warning">🛑 DO NOT CLICK THIS LINK!</div>`;
  } else if (level === 'suspicious') {
    warningHTML = `<div class="popup-warning caution">⚠️ Proceed with extreme caution!</div>`;
  }

  // Sender row (optional)
  const fromRow = alert.from ? `
    <div class="popup-detail-row">
      <span class="popup-detail-label">From:</span>
      <span class="popup-detail-value">${escapeHtml(alert.from)}</span>
    </div>` : '';

  content.innerHTML = `
    <div class="popup-icon">${icon}</div>
    <div class="popup-title ${level}">${alert.level} LINK DETECTED</div>

    <div class="popup-details">
      <div class="popup-detail-row">
        <span class="popup-detail-label">Risk Level:</span>
        <span class="popup-detail-value">${alert.level}</span>
      </div>
      <div class="popup-detail-row">
        <span class="popup-detail-label">Risk Score:</span>
        <span class="popup-detail-value">${alert.score}/100</span>
      </div>
      <div class="popup-detail-row">
        <span class="popup-detail-label">Analyzed By:</span>
        <span class="popup-detail-value">${escapeHtml(alert.analyzedBy || 'Link Guard')}</span>
      </div>
      ${fromRow}
    </div>

    ${reasonsHTML}
    ${warningHTML}

    <div>
      <strong style="font-size:13px;">URL:</strong>
      <div class="url-display">${escapeHtml(alert.url)}</div>
    </div>

    <div class="popup-buttons">
      <button class="btn btn-secondary" onclick="closePopup()">Dismiss</button>
      <button class="btn btn-primary"   onclick="closePopup()">Got It</button>
    </div>
  `;

  overlay.classList.add('show');
}

function closePopup() {
  document.getElementById('popupOverlay').classList.remove('show');
}

// Close on backdrop click
document.getElementById('popupOverlay').addEventListener('click', function (e) {
  if (e.target === this) closePopup();
});

// Close on ESC
document.addEventListener('keydown', function (e) {
  if (e.key === 'Escape') closePopup();
});

// ── Activity Log ──
function addToActivityLog(alert) {
  const log = document.getElementById('activityLog');

  // Remove empty state
  const empty = log.querySelector('.log-empty');
  if (empty) empty.remove();

  const level = alert.level.toLowerCase();
  const icon  = level === 'dangerous' ? '🔴' : level === 'suspicious' ? '⚠️' : '✅';
  const time  = new Date().toLocaleTimeString();

  const entry = document.createElement('div');
  entry.className = `log-entry ${level}`;
  entry.innerHTML = `
    <div class="log-header">
      <span class="log-level">${icon} ${alert.level} — Score: ${alert.score}/100</span>
      <span class="log-time">${time}</span>
    </div>
    <div class="log-url">${escapeHtml(alert.url)}</div>
  `;

  log.insertBefore(entry, log.firstChild);

  // Keep max 50 entries
  const entries = log.querySelectorAll('.log-entry');
  if (entries.length > 50) entries[entries.length - 1].remove();
}

// ── Test Buttons ──
function testDangerousLink() {
  const alert = {
    level: 'DANGEROUS',
    score: 95,
    url: 'https://paypal-verify.tk/login',
    from: 'Unknown (+91 98765 43210)',
    analyzedBy: 'Java Rule Engine + ML',
    reasons: [
      'Suspicious TLD detected (.tk)',
      'Phishing keywords: verify, login',
      'Domain blacklisted',
      'ML confidence: 95%'
    ]
  };
  showPopup(alert);
  addToActivityLog(alert);
  updateStats(alert.level);
}

function testSafeLink() {
  const alert = {
    level: 'SAFE',
    score: 8,
    url: 'https://www.google.com',
    from: 'Friend (+91 98765 43210)',
    analyzedBy: 'Java Rule Engine',
    reasons: []
  };
  showPopup(alert);
  addToActivityLog(alert);
  updateStats(alert.level);
}

function testSuspiciousLink() {
  const alert = {
    level: 'SUSPICIOUS',
    score: 55,
    url: 'https://bit.ly/3xFreeGift',
    from: 'Unknown Group',
    analyzedBy: 'Java Rule Engine',
    reasons: [
      'URL shortener detected',
      'Cannot verify destination',
      'Suspicious keywords: free, gift'
    ]
  };
  showPopup(alert);
  addToActivityLog(alert);
  updateStats(alert.level);
}

// ── Logout ──
function logout() {
  localStorage.removeItem('linkguard_user');
  window.location.href = '/login.html';
}

// ── Utility ──
function escapeHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

async function loadTwilioInfo() {
    try {
        const res  = await fetch(`/api/onboard/twilio-info?userEmail=${encodeURIComponent(currentUser)}`);
        const data = await res.json();

        const number  = (data.sandboxNumber || '+14155238886').replace('+','');
        const keyword = data.joinMessage || 'join silver-tiger';
        const encoded = encodeURIComponent(keyword);

        // Update the WhatsApp deep link
        document.getElementById('whatsappLink').href =
            `https://wa.me/${number}?text=${encoded}`;

    } catch(e) {
        // defaults already in HTML
    }
    markStep(1, true);
}