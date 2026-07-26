import { db } from "./firebase-config.js";
import {
  collection, addDoc, doc, updateDoc, deleteDoc, onSnapshot, query, orderBy
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const tournamentsList = document.getElementById("tournamentsList");
const modal = document.getElementById("tournamentModal");
const modalTitle = document.getElementById("tournamentModalTitle");
const newBtn = document.getElementById("newTournamentBtn");
const saveBtn = document.getElementById("tournamentSaveBtn");
const cancelBtn = document.getElementById("tournamentCancelBtn");

let editingId = null;
export let currentTournaments = [];

function openModal(tournament = null) {
  editingId = tournament?.id || null;
  modalTitle.textContent = tournament ? "Edit Tournament" : "Create Tournament";
  document.getElementById("tTitle").value = tournament?.title || "";
  document.getElementById("tGame").value = tournament?.game || "PUBG Mobile";
  document.getElementById("tMode").value = tournament?.mode || "SQUAD";
  document.getElementById("tType").value = tournament?.type || "FREE";
  document.getElementById("tEntryFee").value = tournament?.entryFee || 0;
  document.getElementById("tPrizePool").value = tournament?.prizePool || 0;
  document.getElementById("tTotalSlots").value = tournament?.totalSlots || 100;
  document.getElementById("tRules").value = tournament?.rules || "";
  document.getElementById("tMatchDateTime").value = tournament?.matchDateTime
    ? new Date(tournament.matchDateTime).toISOString().slice(0, 16)
    : "";
  modal.classList.remove("hidden");
}

function closeModal() {
  modal.classList.add("hidden");
  editingId = null;
}

newBtn.addEventListener("click", () => openModal());
cancelBtn.addEventListener("click", closeModal);

saveBtn.addEventListener("click", async () => {
  const data = {
    title: document.getElementById("tTitle").value.trim(),
    game: document.getElementById("tGame").value.trim(),
    mode: document.getElementById("tMode").value,
    type: document.getElementById("tType").value,
    entryFee: Number(document.getElementById("tEntryFee").value) || 0,
    prizePool: Number(document.getElementById("tPrizePool").value) || 0,
    totalSlots: Number(document.getElementById("tTotalSlots").value) || 100,
    rules: document.getElementById("tRules").value.trim(),
    matchDateTime: document.getElementById("tMatchDateTime").value
      ? new Date(document.getElementById("tMatchDateTime").value).getTime()
      : 0
  };

  if (!data.title) {
    alert("Title zaroori hai");
    return;
  }

  try {
    if (editingId) {
      await updateDoc(doc(db, "tournaments", editingId), data);
    } else {
      await addDoc(collection(db, "tournaments"), {
        ...data,
        status: "UPCOMING",
        filledSlots: 0,
        roomId: "",
        roomPassword: "",
        bannerUrl: "",
        prizeBreakdown: {},
        createdAt: Date.now()
      });
    }
    closeModal();
  } catch (e) {
    alert("Save fail hua: " + e.message);
  }
});

function statusBadge(status) {
  const cls = status === "LIVE" ? "badge-live" : status === "UPCOMING" ? "badge-upcoming" : "badge-pending";
  return `<span class="badge ${cls}">${status}</span>`;
}

function renderTournaments() {
  tournamentsList.innerHTML = currentTournaments.map(t => `
    <div class="glass-card list-row">
      <div>
        <div class="list-row-title">${t.title} ${statusBadge(t.status)}</div>
        <div class="list-row-sub">${t.mode} • ${t.filledSlots}/${t.totalSlots} slots • Prize: Rs.${t.prizePool}</div>
      </div>
      <div class="list-row-actions">
        <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__editTournament('${t.id}')">Edit</button>
        <button class="btn-danger" onclick="window.__cancelTournament('${t.id}')">Cancel</button>
        <button class="btn-danger" onclick="window.__deleteTournament('${t.id}')">Delete</button>
      </div>
    </div>
  `).join("") || `<p class="hint-text">Koi tournament nahi hai. "+ Create Tournament" se naya banayein.</p>`;
}

window.__editTournament = (id) => {
  const t = currentTournaments.find(x => x.id === id);
  if (t) openModal(t);
};
window.__cancelTournament = async (id) => {
  if (confirm("Ye tournament cancel karna hai?")) {
    await updateDoc(doc(db, "tournaments", id), { status: "CANCELLED" });
  }
};
window.__deleteTournament = async (id) => {
  if (confirm("Permanently delete karna hai? Ye undo nahi ho sakta.")) {
    await deleteDoc(doc(db, "tournaments", id));
  }
};

const q = query(collection(db, "tournaments"), orderBy("createdAt", "desc"));
onSnapshot(q, (snap) => {
  currentTournaments = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  renderTournaments();
  document.getElementById("statTournaments").textContent = currentTournaments.length;
  document.getElementById("statLive").textContent = currentTournaments.filter(t => t.status === "LIVE").length;

  // Populate tournament selects used in Registrations and Rooms tabs
  const options = currentTournaments.map(t => `<option value="${t.id}">${t.title}</option>`).join("");
  const regSelect = document.getElementById("regTournamentSelect");
  const roomSelect = document.getElementById("roomTournamentSelect");
  if (regSelect) regSelect.innerHTML = options;
  if (roomSelect) roomSelect.innerHTML = options;
  window.dispatchEvent(new CustomEvent("tournaments-updated"));
});
