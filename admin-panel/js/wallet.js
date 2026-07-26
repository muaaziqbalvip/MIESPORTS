import { db } from "./firebase-config.js";
import {
  collection, query, where, onSnapshot, doc, updateDoc, getDoc, runTransaction
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const walletList = document.getElementById("walletList");

function render(transactions) {
  walletList.innerHTML = transactions.map(tx => `
    <div class="glass-card list-row">
      <div>
        <div class="list-row-title">${tx.type} — Rs. ${tx.amount}</div>
        <div class="list-row-sub">User: ${tx.userId} • Method: ${tx.method || "—"}</div>
      </div>
      <div class="list-row-actions">
        <span class="badge ${tx.status === 'APPROVED' ? 'badge-approved' : tx.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending'}">${tx.status}</span>
        ${tx.status === 'PENDING' ? `
          <button class="btn-secondary" style="width:auto;padding:8px 14px;" onclick="window.__approveTx('${tx.id}','${tx.userId}',${tx.amount},'${tx.type}')">Approve</button>
          <button class="btn-danger" onclick="window.__rejectTx('${tx.id}')">Reject</button>
        ` : ""}
      </div>
    </div>
  `).join("") || `<p class="hint-text">Koi pending wallet request nahi hai</p>`;
}

const q = query(collection(db, "wallet_transactions"), where("status", "==", "PENDING"));
onSnapshot(q, (snap) => {
  const txs = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  render(txs);
  document.getElementById("statPendingTx").textContent = txs.length;
});

window.__approveTx = async (txId, userId, amount, type) => {
  const delta = (type === "DEPOSIT" || type === "PRIZE" || type === "BONUS") ? amount : -amount;
  await runTransaction(db, async (transaction) => {
    const userRef = doc(db, "users", userId);
    const userSnap = await transaction.get(userRef);
    const balance = userSnap.data()?.walletBalance || 0;
    transaction.update(doc(db, "wallet_transactions", txId), { status: "APPROVED" });
    transaction.update(userRef, { walletBalance: balance + delta });
  });
};

window.__rejectTx = async (txId) => {
  await updateDoc(doc(db, "wallet_transactions", txId), { status: "REJECTED" });
};
