document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll("[data-cpa-menu-button]").forEach(function (button) {
    var targetId = button.getAttribute("data-cpa-menu-button");
    var nav = document.getElementById(targetId);
    if (!nav) return;

    button.addEventListener("click", function () {
      var isOpen = nav.classList.toggle("is-open");
      button.setAttribute("aria-expanded", String(isOpen));
    });
  });

  document.querySelectorAll("form[data-cpa-loading]").forEach(function (form) {
    form.addEventListener("submit", function () {
      var button = form.querySelector("button[type='submit']");
      if (!button) return;
      button.disabled = true;
      button.dataset.originalText = button.textContent;
      button.textContent = button.getAttribute("data-loading-text") || "Processando...";
    });
  });

  var responseForm = document.querySelector("[data-cpa-response-form]");
  if (responseForm) {
    var progressBar = document.querySelector("[data-cpa-progress-bar]");
    var progressText = document.querySelector("[data-cpa-progress-text]");
    var requiredGroups = Array.from(responseForm.querySelectorAll("[data-cpa-question]"));

    function questionAnswered(question) {
      var requiredControls = Array.from(question.querySelectorAll("input[required], textarea[required], select[required]"));
      if (!requiredControls.length) {
        var textareas = Array.from(question.querySelectorAll("textarea"));
        return textareas.length ? textareas.some(function (field) { return field.value.trim().length > 0; }) : true;
      }

      var names = new Set(requiredControls.map(function (control) { return control.name; }));
      return Array.from(names).every(function (name) {
        var controls = Array.from(question.querySelectorAll("[name='" + CSS.escape(name) + "']"));
        if (!controls.length) return true;
        var first = controls[0];
        if (first.type === "radio" || first.type === "checkbox") {
          return controls.some(function (control) { return control.checked; });
        }
        return first.value.trim().length > 0;
      });
    }

    function updateProgress() {
      var total = requiredGroups.length;
      var answered = requiredGroups.filter(questionAnswered).length;
      var pct = total ? Math.round((answered / total) * 100) : 0;
      if (progressBar) progressBar.style.width = pct + "%";
      if (progressText) progressText.textContent = answered + " de " + total + " respondidas";
    }

    responseForm.addEventListener("change", updateProgress);
    responseForm.addEventListener("input", updateProgress);
    updateProgress();
  }
});
