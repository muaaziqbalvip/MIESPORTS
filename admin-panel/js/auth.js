import { auth, db } from "./firebase-config.js";
import {
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js";
import { doc, getDoc } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const loginView = document.getElementById("loginView");
const dashboardView = document.getElementById("dashboardView");
const loginBtn = document.getElementById("loginBtn");
const loginError = document.getElementById("loginError");
const logoutBtn = document.getElementById("logoutBtn");

/**
 * Admin access is gated by a Firestore doc: admins/{uid}.
 * Create this doc manually in Firebase Console for each admin account,
 * e.g. admins/<uid> = { role: "superadmin" }
 */
async function isAdmin(uid) {
  const snap = await getDoc(doc(db, "admins", uid));
  return snap.exists();
}

loginBtn.addEventListener("click", async () => {
  const email = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPassword").value;
  loginError.textContent = "";

  if (!email || !password) {
    loginError.textContent = "Email aur password dono zaroori hain";
    return;
  }

  try {
    const cred = await signInWithEmailAndPassword(auth, email, password);
    const adminAllowed = await isAdmin(cred.user.uid);
    if (!adminAllowed) {
      loginError.textContent = "Ye account admin panel access nahi rakhta";
      await signOut(auth);
      return;
    }
    // onAuthStateChanged below handles the view switch
  } catch (e) {
    loginError.textContent = e.message || "Sign-in fail ho gaya";
  }
});

logoutBtn.addEventListener("click", () => signOut(auth));

onAuthStateChanged(auth, async (user) => {
  if (user && (await isAdmin(user.uid))) {
    loginView.classList.add("hidden");
    dashboardView.classList.remove("hidden");
    window.dispatchEvent(new CustomEvent("admin-ready", { detail: { uid: user.uid } }));
  } else {
    loginView.classList.remove("hidden");
    dashboardView.classList.add("hidden");
  }
});
