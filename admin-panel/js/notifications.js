import { db } from "./firebase-config.js";
import {
  collection, addDoc, doc, setDoc
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const sendBtn = document.getElementById("sendNotifBtn");

sendBtn.addEventListener("click", async () => {
  const title = document.getElementById("notifTitle").value.trim();
  const body = document.getElementById("notifBody").value.trim();
  const target = document.getElementById("notifTarget").value;
  const uid = document.getElementById("notifUid").value.trim();

  if (!title || !body) { alert("Title aur message dono zaroori hain"); return; }
  if (target === "single" && !uid) { alert("User UID daalein"); return; }

  const payload = {
    type: "ANNOUNCEMENT",
    title,
    body,
    read: false,
    createdAt: Date.now()
  };

  try {
    if (target === "all") {
      // Written to a broadcast_notifications collection; a Cloud Function should
      // listen here and fan out via FCM topic "all_users".
      await addDoc(collection(db, "broadcast_notifications"), payload);
    } else {
      await addDoc(collection(db, "notifications", uid, "items"), payload);
    }
    alert("Notification queue ho gaya!");
    document.getElementById("notifTitle").value = "";
    document.getElementById("notifBody").value = "";
  } catch (e) {
    alert("Fail hua: " + e.message);
  }
});
