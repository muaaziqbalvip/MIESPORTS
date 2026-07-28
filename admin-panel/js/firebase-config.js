// Firebase config for MI ESPORT Admin Panel

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";
import { getDatabase } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-database.js";

const firebaseConfig = {
  apiKey: "AIzaSyCi3Ike6mKfmkeHtqkJQ0RQCdD0nRu1vsw",
  authDomain: "mi-esports.firebaseapp.com",
  databaseURL: "https://mi-esports-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "mi-esports",
  storageBucket: "mi-esports.firebasestorage.app",
  messagingSenderId: "309861861246",
  appId: "1:309861861246:web:fdda82f7b71d348d9f952f",
  measurementId: "G-TW65C9H1W5"
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const rtdb = getDatabase(app);

// ImgBB image hosting (used for tournament banners, payment QR codes, chat images)
export const IMGBB_API_KEY = "6bdb23b28e7581721b28e46ce313308b";
export const IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

/**
 * Uploads a File object to ImgBB and returns the public URL.
 * Used across admin panel: tournament banners, payment QR codes.
 */
export async function uploadToImgBB(file) {
  const formData = new FormData();
  formData.append("key", IMGBB_API_KEY);
  formData.append("image", file);

  const response = await fetch(IMGBB_UPLOAD_URL, {
    method: "POST",
    body: formData
  });
  const json = await response.json();
  if (!json.success) {
    throw new Error("ImgBB upload fail hua");
  }
  return json.data.url;
}
