import { db } from "./firebase-config.js";
import {
  collection, onSnapshot, doc, updateDoc
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const usersList = document.getElementById("usersList");
const searchInput = document.getElementById("userSearch");
let allUsers = [];

function render(users) {
  usersList.innerHTML = users.map(u => `
    <div class="glass-card list-row">
      <div>
        <div class="list-row-title">${u.gamingName || "Unnamed"} ${u.isBanned ? '<span class="badge badge-rejected">BANNED</span>' : ""}</div>
        <div class="list-row-sub">${u.email || "—"} • Balance: Rs.${u.walletBalance || 0} • Wins: ${u.wins || 0}</div>
      </div>
      <div class="list-row-actions">
        ${u.isBanned
          ? `<button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__unbanUser('${u.id}')">Unban</button>`
          : `<button class="btn-danger" onclick="window.__banUser('${u.id}')">Ban</button>`
        }
      </div>
    </div>
  `).join("") || `<p class="hint-text">Koi user nahi mila</p>`;
}

onSnapshot(collection(db, "users"), (snap) => {
  allUsers = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  render(allUsers);
  document.getElementById("statUsers").textContent = allUsers.length;
});

searchInput.addEventListener("input", () => {
  const term = searchInput.value.toLowerCase();
  render(allUsers.filter(u =>
    (u.gamingName || "").toLowerCase().includes(term) ||
    (u.email || "").toLowerCase().includes(term)
  ));
});

window.__banUser = async (uid) => {
  if (confirm("Is user ko ban karna hai?")) {
    await updateDoc(doc(db, "users", uid), { isBanned: true });
  }
};
window.__unbanUser = async (uid) => {
  await updateDoc(doc(db, "users", uid), { isBanned: false });
};
