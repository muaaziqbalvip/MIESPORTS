import { db, auth } from "./firebase-config.js";
import {
  collection, query, orderBy, onSnapshot, addDoc
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const chatListEl = document.getElementById("supportChatsList");
const chatWindowEl = document.getElementById("supportChatWindow");
const chatInput = document.getElementById("supportChatInput");
const chatSendBtn = document.getElementById("supportChatSendBtn");
const chatHeaderEl = document.getElementById("supportChatHeader");

let activeChatId = null;
let unsubscribeMessages = null;
let knownChatIds = new Set();

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function renderChatList(chatIds) {
  if (!chatListEl) return;
  chatListEl.innerHTML = chatIds.map(id => {
    const uid = id.replace("support_", "");
    const isActive = id === activeChatId;
    return `
      <div class="glass-card list-row" style="cursor:pointer; ${isActive ? 'border-color: var(--green);' : ''}" onclick="window.__openSupportChat('${id}')">
        <div>
          <div class="list-row-title">Player: ${escapeHtml(uid.slice(0, 12))}...</div>
          <div class="list-row-sub">Tap to open chat</div>
        </div>
      </div>
    `;
  }).join("") || `<p class="hint-text">Abhi koi support chat active nahi hai</p>`;
}

function renderMessages(messages) {
  if (!chatWindowEl) return;
  chatWindowEl.innerHTML = messages.map(m => `
    <div style="display:flex; justify-content:${m.isAdmin ? 'flex-end' : 'flex-start'}; margin-bottom:10px;">
      <div style="max-width:70%; background:${m.isAdmin ? 'rgba(52,227,154,0.15)' : 'var(--surface-elevated)'}; padding:10px 14px; border-radius:14px;">
        ${!m.isAdmin ? `<div style="color:var(--green); font-size:12px; font-weight:700; margin-bottom:4px;">${escapeHtml(m.senderName || 'Player')}</div>` : ""}
        ${m.imageUrl ? `<img src="${escapeHtml(m.imageUrl)}" style="max-width:100%; border-radius:8px; margin-bottom:6px;" onerror="this.style.display='none'" />` : ""}
        ${m.text ? `<div style="color:var(--text);">${escapeHtml(m.text)}</div>` : ""}
      </div>
    </div>
  `).join("") || `<p class="hint-text">Is chat mein abhi koi message nahi hai</p>`;
  chatWindowEl.scrollTop = chatWindowEl.scrollHeight;
}

window.__openSupportChat = (chatId) => {
  activeChatId = chatId;
  if (chatHeaderEl) chatHeaderEl.textContent = `Chat: ${chatId.replace("support_", "").slice(0, 16)}...`;

  if (unsubscribeMessages) unsubscribeMessages();

  try {
    const q = query(collection(db, "chats", chatId, "messages"), orderBy("createdAt", "asc"));
    unsubscribeMessages = onSnapshot(q, (snap) => {
      const messages = snap.docs.map(d => ({ id: d.id, ...d.data() }));
      renderMessages(messages);
    }, (error) => {
      console.error("Support chat messages error:", error);
      if (chatWindowEl) chatWindowEl.innerHTML = `<p class="hint-text">Messages load nahi ho sake</p>`;
    });
  } catch (e) {
    console.error("Open support chat error:", e);
  }

  renderChatList(Array.from(knownChatIds));
};

async function sendReply() {
  if (!activeChatId || !chatInput) return;
  const text = chatInput.value.trim();
  if (!text) return;

  try {
    await addDoc(collection(db, "chats", activeChatId, "messages"), {
      senderId: auth.currentUser?.uid || "admin",
      senderName: "MI ESPORT Support",
      text,
      imageUrl: "",
      isAdmin: true,
      createdAt: Date.now()
    });
    chatInput.value = "";
  } catch (e) {
    console.error("Send reply error:", e);
    alert("Message bhejne mein masla hua: " + e.message);
  }
}

if (chatSendBtn) chatSendBtn.addEventListener("click", sendReply);
if (chatInput) {
  chatInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") sendReply();
  });
}

// Discover active support chats via a lightweight index collection
// (support_chat_index/{chatId}) that gets touched whenever a player sends
// their first support message. Keeps this listener cheap instead of scanning
// every possible chat document.
try {
  const indexQuery = query(collection(db, "support_chat_index"), orderBy("lastMessageAt", "desc"));
  onSnapshot(indexQuery, (snap) => {
    knownChatIds = new Set(snap.docs.map(d => d.id));
    renderChatList(Array.from(knownChatIds));
  }, (error) => {
    console.error("Support chat index error:", error);
    if (chatListEl) chatListEl.innerHTML = `<p class="hint-text">Chat list load nahi ho saki</p>`;
  });
} catch (e) {
  console.error("Support chat index init error:", e);
}
