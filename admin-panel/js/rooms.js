import { rtdb } from "./firebase-config.js";
import { ref, set } from "https://www.gstatic.com/firebasejs/10.13.0/firebase-database.js";

const publishBtn = document.getElementById("publishRoomBtn");
const updateLiveBtn = document.getElementById("updateLiveBtn");
const roomTournamentSelect = document.getElementById("roomTournamentSelect");

publishBtn.addEventListener("click", async () => {
  const tournamentId = roomTournamentSelect.value;
  const roomId = document.getElementById("roomIdInput").value.trim();
  const roomPassword = document.getElementById("roomPasswordInput").value.trim();

  if (!tournamentId) { alert("Pehle tournament select karein"); return; }
  if (!roomId || !roomPassword) { alert("Room ID aur password dono zaroori hain"); return; }

  await set(ref(rtdb, `room_reveal/${tournamentId}`), { roomId, roomPassword });
  alert("Room ID publish ho gaya! Users ko turant dikh jayega.");
});

updateLiveBtn.addEventListener("click", async () => {
  const tournamentId = roomTournamentSelect.value;
  const youtubeVideoId = document.getElementById("youtubeVideoIdInput").value.trim();
  const isLive = document.getElementById("isLiveCheckbox").checked;

  if (!tournamentId) { alert("Pehle tournament select karein"); return; }

  await set(ref(rtdb, `live_status/${tournamentId}`), {
    tournamentId,
    isLive,
    youtubeVideoId,
    currentPhase: isLive ? "In Progress" : "Waiting",
    updatedAt: Date.now()
  });
  alert("Live status update ho gaya!");
});
