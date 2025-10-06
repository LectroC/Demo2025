document.addEventListener("DOMContentLoaded", function () {
  const fieldSelect = document.getElementById("fieldSelect");
  const form = document.getElementById("inputForm");

  const label1Container = document.getElementById("label1-container");
  const label2Container = document.getElementById("label2-container");
  const label3Container = document.getElementById("label3-container");

  // ✅ Function to show/hide fields based on dropdown
  function updateVisibleFields() {
    const selectedValue = fieldSelect.value;

    // Hide all initially
    label1Container.style.display = "none";
    label2Container.style.display = "none";
    label3Container.style.display = "none";

    // Show fields depending on selection
    if (selectedValue === "label1") {
      label1Container.style.display = "grid";
    } else if (selectedValue === "label2") {
      label2Container.style.display = "grid";
    } else if (selectedValue === "label12") {
      label1Container.style.display = "grid";
      label2Container.style.display = "grid";
    } else if (selectedValue === "all") {
      label1Container.style.display = "grid";
      label2Container.style.display = "grid";
      label3Container.style.display = "grid";
    }
  }

  // ✅ Run once on page load
  updateVisibleFields();

  // ✅ Update fields when selection changes
  fieldSelect.addEventListener("change", updateVisibleFields);

  // ✅ Handle form submission
  form.addEventListener("submit", function (e) {
    e.preventDefault();

    const val1 = document.getElementById("label1").value;
    const val2 = document.getElementById("label2").value;
    const val3 = document.getElementById("label3").value;

    // Store values in variables (example usage)
    const storedValues = { val1, val2, val3 };
    console.log("Stored values:", storedValues);
    alert("Values stored successfully!");
  });

  // ✅ 🌙 Dark mode toggle
  const toggleBtn = document.getElementById("theme-toggle");
  toggleBtn.addEventListener("click", function () {
    document.body.classList.toggle("dark-mode");

    // Change button text/icon dynamically
    if (document.body.classList.contains("dark-mode")) {
      toggleBtn.textContent = "☀️ Light Mode";
    } else {
      toggleBtn.textContent = "🌙 Dark Mode";
    }
  });
});
