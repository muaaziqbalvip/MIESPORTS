import { db, uploadToImgBB } from "./firebase-config.js";
import {
  collection, addDoc, doc, updateDoc, deleteDoc, onSnapshot, query, orderBy
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const listEl = document.getElementById("paymentMethodsList");
const modal = document.getElementById("paymentMethodModal");
const modalTitle = document.getElementById("paymentMethodModalTitle");
const newBtn = document.getElementById("newPaymentMethodBtn");
const saveBtn = document.getElementById("paymentMethodSaveBtn");
const cancelBtn = document.getElementById("paymentMethodCancelBtn");
const qrFileInput = document.getElementById("pmQrFile");
const qrPreview = document.getElementById("pmQrPreview");

let editingId = null;
let uploadedQrUrl = "";
let currentMethods = [];

function openModal(method = null) {
  editingId = method?.id || null;
  uploadedQrUrl = method?.qrCodeUrl || "";
  modalTitle.textContent = method ? "Edit Payment Method" : "Add Payment Method";
  document.getElementById("pmName").value = method?.name || "";
  document.getElementById("pmAccountTitle").value = method?.accountTitle || "";
  document.getElementById("pmAccountNumber").value = method?.accountNumber || "";
  document.getElementById("pmInstructions").value = method?.instructions || "";
  qrFileInput.value = "";
  if (uploadedQrUrl) {
    qrPreview.src = uploadedQrUrl;
    qrPreview.classList.remove("hidden");
  } else {
    qrPreview.classList.add("hidden");
  }
  saveBtn.disabled = false;
  saveBtn.textContent = "Save";
  modal.classList.remove("hidden");
}

function closeModal() {
  modal.classList.add("hidden");
  editingId = null;
  uploadedQrUrl = "";
}

newBtn.addEventListener("click", () => openModal());
cancelBtn.addEventListener("click", closeModal);

qrFileInput.addEventListener("change", async () => {
  const file = qrFileInput.files[0];
  if (!file) return;

  // Show local preview immediately, then swap to hosted URL once uploaded
  const localPreview = URL.createObjectURL(file);
  qrPreview.src = localPreview;
  qrPreview.classList.remove("hidden");

  saveBtn.disabled = true;
  saveBtn.textContent = "Uploading QR...";
  try {
    uploadedQrUrl = await uploadToImgBB(file);
  } catch (e) {
    alert("QR upload fail hui: " + e.message + ". Dobara try karein.");
    uploadedQrUrl = "";
  } finally {
    saveBtn.disabled = false;
    saveBtn.textContent = "Save";
  }
});

saveBtn.addEventListener("click", async () => {
  const name = document.getElementById("pmName").value.trim();
  if (!name) {
    alert("Method name zaroori hai (e.g. JazzCash)");
    return;
  }

  const data = {
    name,
    accountTitle: document.getElementById("pmAccountTitle").value.trim(),
    accountNumber: document.getElementById("pmAccountNumber").value.trim(),
    instructions: document.getElementById("pmInstructions").value.trim(),
    qrCodeUrl: uploadedQrUrl,
    isActive: true
  };

  saveBtn.disabled = true;
  try {
    if (editingId) {
      await updateDoc(doc(db, "payment_methods", editingId), data);
    } else {
      await addDoc(collection(db, "payment_methods"), {
        ...data,
        sortOrder: currentMethods.length
      });
    }
    closeModal();
  } catch (e) {
    alert("Save fail hua: " + e.message);
  } finally {
    saveBtn.disabled = false;
  }
});

function render() {
  listEl.innerHTML = currentMethods.map(m => `
    <div class="glass-card list-row">
      <div style="display:flex; align-items:center; gap:14px;">
        ${m.qrCodeUrl ? `<img src="${m.qrCodeUrl}" style="width:56px;height:56px;object-fit:cover;border-radius:8px;" />` : ""}
        <div>
          <div class="list-row-title">${m.name}</div>
          <div class="list-row-sub">${m.accountTitle || "—"} ${m.accountNumber ? "• " + m.accountNumber : ""}</div>
        </div>
      </div>
      <div class="list-row-actions">
        <span class="badge ${m.isActive ? 'badge-approved' : 'badge-rejected'}">${m.isActive ? 'ACTIVE' : 'HIDDEN'}</span>
        <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__editPaymentMethod('${m.id}')">Edit</button>
        <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__togglePaymentMethod('${m.id}', ${m.isActive})">${m.isActive ? 'Hide' : 'Show'}</button>
        <button class="btn-danger" onclick="window.__deletePaymentMethod('${m.id}')">Delete</button>
      </div>
    </div>
  `).join("") || `<p class="hint-text">Koi payment method nahi hai. "+ Add Method" se QR code add karein.</p>`;
}

window.__editPaymentMethod = (id) => {
  const m = currentMethods.find(x => x.id === id);
  if (m) openModal(m);
};
window.__togglePaymentMethod = async (id, isActive) => {
  try {
    await updateDoc(doc(db, "payment_methods", id), { isActive: !isActive });
  } catch (e) {
    alert("Update fail hua: " + e.message);
  }
};
window.__deletePaymentMethod = async (id) => {
  if (confirm("Ye payment method delete karna hai?")) {
    try {
      await deleteDoc(doc(db, "payment_methods", id));
    } catch (e) {
      alert("Delete fail hua: " + e.message);
    }
  }
};

try {
  const q = query(collection(db, "payment_methods"), orderBy("sortOrder"));
  onSnapshot(q, (snap) => {
    currentMethods = snap.docs.map(d => ({ id: d.id, ...d.data() }));
    render();
  }, (error) => {
    console.error("Payment methods listener error:", error);
    listEl.innerHTML = `<p class="hint-text">Data load karne mein masla hua. Page refresh karein.</p>`;
  });
} catch (e) {
  console.error("Payment methods init error:", e);
}
