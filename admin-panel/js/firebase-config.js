// Firebase config for MI ESPORT Admin Panel
// Replace these values with your actual Firebase project config
// (Firebase Console > Project Settings > General > Your apps > Web app)

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";
import { getDatabase } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-database.js";

// For Firebase JS SDK v7.20.0 and later, measurementId is optional
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
