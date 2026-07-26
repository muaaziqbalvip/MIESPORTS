import { db } from "./firebase-config.js";
import {
  collection, onSnapshot, doc, updateDoc
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const regSelect = document.getElementById("regTournamentSelect");
const regList = document.getElementById("registrationsList");
let unsubscribe = null;

function renderRegistrations(regs) {
  regList.innerHTML = regs.map(r => `
    <div class="glass-card list-row">
      <div>
        <div class="list-row-title">${r.inGameName} (UID: ${r.uidGame})</div>
        <div class="list-row-sub">${r.region || "—"} ${r.teamName ? "• Team: " + r.teamName : ""}</div>
        ${r.screenshotUrl ? `<a href="${r.screenshotUrl}" target="_blank" class="list-row-sub">View Screenshot</a>` : ""}
      </div>
      <div class="list-row-actions">
        <span class="badge ${r.paymentStatus === 'VERIFIED' ? 'badge-approved' : r.paymentStatus === 'REJECTED' ? 'badge-rejected' : 'badge-pending'}">${r.paymentStatus}</span>
        ${r.paymentStatus === 'PENDING' ? `
          <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__verifyReg('${r._tournamentId}','${r.id}')">Verify</button>
          <button class="btn-danger" onclick="window.__rejectReg('${r._tournamentId}','${r.id}')">Reject</button>
        ` : ""}
      </div>
    </div>
  `).join("") || `<p class="hint-text">Is tournament mein koi registration nahi hai</p>`;
}

function loadRegistrations(tournamentId) {
  if (unsubscribe) unsubscribe();
  if (!tournamentId) { regList.innerHTML = ""; return; }
  unsubscribe = onSnapshot(
    collection(db, "tournaments", tournamentId, "registrations"),
    (snap) => {
      const regs = snap.docs.map(d => ({ id: d.id, _tournamentId: tournamentId, ...d.data() }));
      renderRegistrations(regs);
    }
  );
}

regSelect.addEventListener("change", () => loadRegistrations(regSelect.value));
window.addEventListener("tournaments-updated", () => {
  if (regSelect.value) loadRegistrations(regSelect.value);
});

window.__verifyReg = async (tournamentId, regId) => {
  await updateDoc(doc(db, "tournaments", tournamentId, "registrations", regId), { paymentStatus: "VERIFIED" });
};
window.__rejectReg = async (tournamentId, regId) => {
  await updateDoc(doc(db, "tournaments", tournamentId, "registrations", regId), { paymentStatus: "REJECTED" });
};
