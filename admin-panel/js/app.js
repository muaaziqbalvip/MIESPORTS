const navItems = document.querySelectorAll(".nav-item[data-tab]");
const tabSections = document.querySelectorAll(".tab-section");

navItems.forEach(btn => {
  btn.addEventListener("click", () => {
    navItems.forEach(b => b.classList.remove("active"));
    tabSections.forEach(s => s.classList.remove("active"));

    btn.classList.add("active");
    document.getElementById(`tab-${btn.dataset.tab}`).classList.add("active");
  });
});
