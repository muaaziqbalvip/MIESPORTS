import { db, uploadToImgBB } from "./firebase-config.js";
import {
  collection, addDoc, doc, updateDoc, deleteDoc, onSnapshot, query, orderBy
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const tournamentsList = document.getElementById("tournamentsList");
const modal = document.getElementById("tournamentModal");
const modalTitle = document.getElementById("tournamentModalTitle");
const newBtn = document.getElementById("newTournamentBtn");
const saveBtn = document.getElementById("tournamentSaveBtn");
const cancelBtn = document.getElementById("tournamentCancelBtn");
const bannerFileInput = document.getElementById("tBannerFile");
const bannerPreview = document.getElementById("tBannerPreview");

let editingId = null;
let pendingBannerUrl = "";
let isSaving = false;
export let currentTournaments = [];

function resetModalFields() {
  document.getElementById("tTitle").value = "";
  document.getElementById("tGame").value = "PUBG Mobile";
  document.getElementById("tMode").value = "SQUAD";
  document.getElementById("tType").value = "FREE";
  document.getElementById("tEntryFee").value = "";
  document.getElementById("tPrizePool").value = "";
  document.getElementById("tTotalSlots").value = "";
  document.getElementById("tRules").value = "";
  document.getElementById("tMatchDateTime").value = "";
  bannerFileInput.value = "";
  bannerPreview.src = "";
  bannerPreview.classList.add("hidden");
  pendingBannerUrl = "";
}

function openModal(tournament = null) {
  resetModalFields();
  editingId = tournament?.id || null;
  modalTitle.textContent = tournament ? "Edit Tournament" : "Create Tournament";
  document.getElementById("tTitle").value = tournament?.title || "";
  document.getElementById("tGame").value = tournament?.game || "PUBG Mobile";
  document.getElementById("tMode").value = tournament?.mode || "SQUAD";
  document.getElementById("tType").value = tournament?.type || "FREE";
  document.getElementById("tEntryFee").value = tournament?.entryFee || "";
  document.getElementById("tPrizePool").value = tournament?.prizePool || "";
  document.getElementById("tTotalSlots").value = tournament?.totalSlots || "";
  document.getElementById("tRules").value = tournament?.rules || "";
  document.getElementById("tMatchDateTime").value = tournament?.matchDateTime
    ? new Date(tournament.matchDateTime).toISOString().slice(0, 16)
    : "";
  if (tournament?.bannerUrl) {
    pendingBannerUrl = tournament.bannerUrl;
    bannerPreview.src = tournament.bannerUrl;
    bannerPreview.classList.remove("hidden");
  }
  modal.classList.remove("hidden");
}

function closeModal() {
  modal.classList.add("hidden");
  editingId = null;
  resetModalFields();
}

newBtn.addEventListener("click", () => openModal());
cancelBtn.addEventListener("click", closeModal);

// Live preview when a banner file is chosen, before upload
bannerFileInput.addEventListener("change", () => {
  const file = bannerFileInput.files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (e) => {
    bannerPreview.src = e.target.result;
    bannerPreview.classList.remove("hidden");
  };
  reader.readAsDataURL(file);
});

saveBtn.addEventListener("click", async () => {
  if (isSaving) return; // prevent double-submit / crash from rapid clicks

  const title = document.getElementById("tTitle").value.trim();
  if (!title) {
    alert("Title zaroori hai");
    return;
  }

  const totalSlots = Number(document.getElementById("tTotalSlots").value);
  if (!totalSlots || totalSlots <= 0) {
    alert("Total Slots ek valid number hona chahiye (0 se zyada)");
    return;
  }

  isSaving = true;
  saveBtn.disabled = true;
  saveBtn.textContent = "Saving...";

  try {
    // Upload banner if a new file was chosen
    const file = bannerFileInput.files?.[0];
    if (file) {
      saveBtn.textContent = "Uploading image...";
      pendingBannerUrl = await uploadToImgBB(file);
    }

    const data = {
      title,
      game: document.getElementById("tGame").value.trim() || "PUBG Mobile",
      mode: document.getElementById("tMode").value,
      type: document.getElementById("tType").value,
      entryFee: Number(document.getElementById("tEntryFee").value) || 0,
      prizePool: Number(document.getElementById("tPrizePool").value) || 0,
      totalSlots,
      rules: document.getElementById("tRules").value.trim(),
      bannerUrl: pendingBannerUrl || "",
      matchDateTime: document.getElementById("tMatchDateTime").value
        ? new Date(document.getElementById("tMatchDateTime").value).getTime()
        : 0
    };

    if (editingId) {
      await updateDoc(doc(db, "tournaments", editingId), data);
    } else {
      await addDoc(collection(db, "tournaments"), {
        ...data,
        status: "UPCOMING",
        filledSlots: 0,
        roomId: "",
        roomPassword: "",
        prizeBreakdown: {},
        createdAt: Date.now()
      });
    }
    closeModal();
  } catch (e) {
    console.error("Tournament save error:", e);
    alert("Save fail hua: " + (e.message || "Kuch masla hua, dobara try karein"));
  } finally {
    isSaving = false;
    saveBtn.disabled = false;
    saveBtn.textContent = "Save";
  }
});

function statusBadge(status) {
  const cls = status === "LIVE" ? "badge-live" : status === "UPCOMING" ? "badge-upcoming" : "badge-pending";
  return `<span class="badge ${cls}">${status || "UPCOMING"}</span>`;
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function renderTournaments() {
  try {
    tournamentsList.innerHTML = currentTournaments.map(t => `
      <div class="glass-card list-row">
        <div style="display:flex; align-items:center; gap:12px;">
          ${t.bannerUrl ? `<img src="${escapeHtml(t.bannerUrl)}" class="thumb" onerror="this.style.display='none'" />` : ""}
          <div>
            <div class="list-row-title">${escapeHtml(t.title)} ${statusBadge(t.status)}</div>
            <div class="list-row-sub">${escapeHtml(t.mode)} • ${t.filledSlots || 0}/${t.totalSlots || 0} slots • Prize: Rs.${t.prizePool || 0}</div>
          </div>
        </div>
        <div class="list-row-actions">
          <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__editTournament('${t.id}')">Edit</button>
          <button class="btn-danger" onclick="window.__cancelTournament('${t.id}')">Cancel</button>
          <button class="btn-danger" onclick="window.__deleteTournament('${t.id}')">Delete</button>
        </div>
      </div>
    `).join("") || `<p class="hint-text">Koi tournament nahi hai. "+ Create Tournament" se naya banayein.</p>`;
  } catch (e) {
    console.error("Render error:", e);
    tournamentsList.innerHTML = `<p class="hint-text">List load karne mein masla hua, page refresh karein.</p>`;
  }
}

window.__editTournament = (id) => {
  const t = currentTournaments.find(x => x.id === id);
  if (t) openModal(t);
};
window.__cancelTournament = async (id) => {
  if (confirm("Ye tournament cancel karna hai?")) {
    try {
      await updateDoc(doc(db, "tournaments", id), { status: "CANCELLED" });
    } catch (e) {
      alert("Cancel nahi ho saka: " + e.message);
    }
  }
};
window.__deleteTournament = async (id) => {
  if (confirm("Permanently delete karna hai? Ye undo nahi ho sakta.")) {
    try {
      await deleteDoc(doc(db, "tournaments", id));
    } catch (e) {
      alert("Delete nahi ho saka: " + e.message);
    }
  }
};

try {
  const q = query(collection(db, "tournaments"), orderBy("createdAt", "desc"));
  onSnapshot(q, (snap) => {
    currentTournaments = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    renderTournaments();
    document.getElementById("statTournaments").textContent = currentTournaments.length;
    document.getElementById("statLive").textContent = currentTournaments.filter(t => t.status === "LIVE").length;

    const options = currentTournaments.map(t => `<option value="${t.id}">${escapeHtml(t.title)}</option>`).join("");
    const regSelect = document.getElementById("regTournamentSelect");
    const roomSelect = document.getElementById("roomTournamentSelect");
    if (regSelect) regSelect.innerHTML = options || `<option value="">Koi tournament nahi hai</option>`;
    if (roomSelect) roomSelect.innerHTML = options || `<option value="">Koi tournament nahi hai</option>`;
    window.dispatchEvent(new CustomEvent("tournaments-updated"));
  }, (error) => {
    console.error("Tournaments listener error:", error);
    tournamentsList.innerHTML = `<p class="hint-text">Tournaments load nahi ho sake. Internet check karein.</p>`;
  });
} catch (e) {
  console.error("Tournaments init error:", e);
}
