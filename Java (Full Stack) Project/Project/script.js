const API = "/api";

async function api(path, options = {}) {
  const response = await fetch(API + path, {
    credentials: "same-origin",
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });

  let data = {};
  try {
    data = await response.json();
  } catch (_) {}

  if (!response.ok) {
    throw new Error(data.message || "Request failed");
  }

  return data;
}

async function getCurrentUser() {
  try {
    return await api("/me");
  } catch (_) {
    return { loggedIn: false };
  }
}

function isProtectedPage() {
  const pages = [
    "/tracking",
    "/transport",
    "/home-energy",
    "/food",
    "/shopping",
    "/dashboard"
  ];

  return pages.includes(location.pathname.replace(/\/$/, ""));
}

function showTimedNotice(message, type = "success") {
  const notice = document.createElement("div");
  notice.className = "alert alert-" + type + " alert-dismissible fade show eco-toast shadow-sm";
  notice.setAttribute("role", "alert");
  notice.innerHTML = `<strong>${escapeHtml(message)}</strong>`;
  document.body.appendChild(notice);

  window.setTimeout(() => {
    notice.classList.remove("show");
    window.setTimeout(() => notice.remove(), 250);
  }, 3000);
}

function renderUserNav(user) {
  const loggedIn = Boolean(user && user.loggedIn);

  document.querySelectorAll("[data-register], [data-login]").forEach(element => {
    element.style.display = loggedIn ? "none" : "block";
  });

  document.querySelectorAll("[data-protected]").forEach(element => {
    element.style.display = loggedIn ? "block" : "none";
  });

  document.querySelectorAll("[data-user-wrap]").forEach(wrapper => {
    wrapper.style.display = loggedIn ? "block" : "none";

    const button = wrapper.querySelector("[data-user-name]");
    const menu = wrapper.querySelector("[data-account-menu]");
    if (!button || !menu) return;

    if (!loggedIn) {
      button.textContent = "";
      menu.innerHTML = "";
      return;
    }

    button.textContent = user.name || "User";
    button.title = "Account options";
    button.setAttribute("aria-label", `Account options for ${user.name || "user"}`);

    menu.innerHTML = `
      <li class="account-profile">
        <div class="account-name">${escapeHtml(user.name || "User")}</div>
        <div class="account-email">${escapeHtml(user.email || "")}</div>
      </li>
      <li><hr class="dropdown-divider"></li>
      <li><button type="button" class="account-action delete" data-delete-account>Delete Account</button></li>
      <li><button type="button" class="account-action logout" data-menu-logout>Logout</button></li>
    `;

    menu.querySelector("[data-delete-account]").addEventListener("click", async event => {
      event.preventDefault();
      event.stopPropagation();
      showConfirmModal({
        title: "Delete account?",
        message: "Are you sure you want to permanently delete your EcoTrack account and its saved calculation data?",
        confirmText: "Delete Account",
        confirmClass: "btn-danger",
        onConfirm: async () => {
          try {
            await api("/delete-account", { method: "POST" });
            localStorage.removeItem("ecoDraft");
            localStorage.removeItem("afterLogin");
            sessionStorage.clear();
            sessionStorage.setItem("accountDeletedMessage", "Account deleted successfully.");
            location.href = "/";
          } catch (error) {
            showTimedNotice(error.message || "Unable to delete account.", "danger");
          }
        }
      });
    });

    menu.querySelector("[data-menu-logout]").addEventListener("click", async event => {
      event.preventDefault();
      event.stopPropagation();
      showConfirmModal({
        title: "Log out?",
        message: "Are you sure you want to log out of your EcoTrack account?",
        confirmText: "Logout",
        confirmClass: "btn-success",
        onConfirm: async () => {
          try {
            await api("/logout", { method: "POST" });
          } catch (_) {}

          localStorage.removeItem("ecoDraft");
          localStorage.removeItem("afterLogin");
          sessionStorage.setItem("logoutMessage", "Logged out successfully.");
          location.href = "/";
        }
      });
    });
  });

  // Backward compatibility if an older page still contains a logout link.
  document.querySelectorAll("[data-logout]").forEach(element => {
    element.style.display = loggedIn ? "block" : "none";
    element.onclick = async event => {
      event.preventDefault();
      showConfirmModal({
        title: "Log out?",
        message: "Are you sure you want to log out of your EcoTrack account?",
        confirmText: "Logout",
        confirmClass: "btn-success",
        onConfirm: async () => {
          try { await api("/logout", { method: "POST" }); } catch (_) {}
          localStorage.removeItem("ecoDraft");
          localStorage.removeItem("afterLogin");
          sessionStorage.setItem("logoutMessage", "Logged out successfully.");
          location.href = "/";
        }
      });
    };
  });
}

function goToRegisterForTracking() {
  localStorage.setItem("afterLogin", "/transport");
  location.href = "/register";
}

function showConfirmModal({ title, message, confirmText, confirmClass, onConfirm }) {
  const existing = document.getElementById("ecoConfirmModal");
  if (existing) existing.remove();

  const wrapper = document.createElement("div");
  wrapper.innerHTML = `
    <div class="modal fade" id="ecoConfirmModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg eco-confirm-modal">
          <div class="modal-body p-4 p-lg-5 text-center">
            <div class="confirm-icon">!</div>
            <h2 class="h4 fw-bold mb-2">${escapeHtml(title)}</h2>
            <p class="text-secondary mb-4">${escapeHtml(message)}</p>
            <div class="d-flex justify-content-center gap-2 flex-wrap">
              <button type="button" class="btn btn-light border px-4" data-bs-dismiss="modal">Cancel</button>
              <button type="button" class="btn ${confirmClass} px-4" id="ecoConfirmAction">${escapeHtml(confirmText)}</button>
            </div>
          </div>
        </div>
      </div>
    </div>`;
  document.body.appendChild(wrapper.firstElementChild);
  const modalElement = document.getElementById("ecoConfirmModal");
  const modal = window.bootstrap ? new bootstrap.Modal(modalElement) : null;
  const action = document.getElementById("ecoConfirmAction");
  action.addEventListener("click", async () => {
    action.disabled = true;
    if (modal) modal.hide();
    await onConfirm();
    setTimeout(() => modalElement.remove(), 300);
  });
  modalElement.addEventListener("hidden.bs.modal", () => modalElement.remove(), { once: true });
  if (modal) modal.show();
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, character => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[character]);
}

function setInlineMessage(element, text, type = "danger") {
  if (!element) return;
  element.className = `alert alert-${type} mt-3 mb-0 py-2`;
  element.textContent = text;
}

document.addEventListener("DOMContentLoaded", async () => {
  const logoutMessage = sessionStorage.getItem("logoutMessage");
  if (logoutMessage) {
    sessionStorage.removeItem("logoutMessage");
    showTimedNotice(logoutMessage, "success");
  }

  const accountDeletedMessage = sessionStorage.getItem("accountDeletedMessage");
  if (accountDeletedMessage) {
    sessionStorage.removeItem("accountDeletedMessage");
    showTimedNotice(accountDeletedMessage, "success");
  }

  const user = await getCurrentUser();
  renderUserNav(user);

  /*
   * REQUIRED FLOW:
   * First page -> Calculate My Footprint -> Home
   * Home -> Start Tracking
   * Logged out -> Register -> Login -> Transport
   * Logged in -> Transport directly
   */
  if (location.pathname === "/tracking") {
    if (user.loggedIn) {
      location.replace("/transport");
    } else {
      goToRegisterForTracking();
    }
    return;
  }

  if (isProtectedPage() && !user.loggedIn) {
    goToRegisterForTracking();
    return;
  }

  const startTrackingButton = document.querySelector("[data-start-tracking]");

  if (startTrackingButton) {
    startTrackingButton.addEventListener("click", event => {
      event.preventDefault();

      if (user.loggedIn) {
        location.href = "/transport";
      } else {
        goToRegisterForTracking();
      }
    });
  }

  const registerForm = document.getElementById("registerForm");

  if (registerForm) {
    registerForm.addEventListener("submit", async event => {
      event.preventDefault();

      const form = new FormData(registerForm);
      const message = document.getElementById("message");

      if (form.get("password") !== form.get("confirmPassword")) {
        setInlineMessage(message, "Passwords do not match.", "danger");
        return;
      }

      try {
        await api("/register", {
          method: "POST",
          body: JSON.stringify({
            name: form.get("name"),
            email: form.get("email"),
            password: form.get("password")
          })
        });

        location.href = "/login?registered=1";
      } catch (error) {
        setInlineMessage(message, error.message, "danger");
      }
    });
  }

  const loginForm = document.getElementById("loginForm");

  if (loginForm) {
    const message = document.getElementById("message");

    if (new URLSearchParams(location.search).has("registered")) {
      showTimedNotice("Registration successful. Please login.", "success");
    }

    loginForm.addEventListener("submit", async event => {
      event.preventDefault();

      const form = new FormData(loginForm);

      try {
        const target =
          localStorage.getItem("afterLogin") || "/transport";

        await api("/login", {
          method: "POST",
          body: JSON.stringify({
            email: form.get("email"),
            password: form.get("password")
          })
        });

        localStorage.removeItem("afterLogin");
        location.href = target;
      } catch (error) {
        setInlineMessage(message, error.message, "danger");
      }
    });
  }

  const forgotPasswordForm = document.getElementById("forgotPasswordForm");

  if (forgotPasswordForm) {
    const message = document.getElementById("message");

    forgotPasswordForm.addEventListener("submit", async event => {
      event.preventDefault();
      const form = new FormData(forgotPasswordForm);
      const password = form.get("password");
      const confirmPassword = form.get("confirmPassword");

      if (password !== confirmPassword) {
        setInlineMessage(message, "Passwords do not match.", "danger");
        return;
      }

      try {
        await api("/reset-password", {
          method: "POST",
          body: JSON.stringify({
            email: form.get("email"),
            password,
            confirmPassword
          })
        });
        setInlineMessage(message, "Password updated successfully. Redirecting to login...", "success");
        forgotPasswordForm.reset();
        setTimeout(() => { location.href = "/login"; }, 1200);
      } catch (error) {
        setInlineMessage(message, error.message, "danger");
      }
    });
  }

  const stepForm = document.getElementById("stepForm");

  if (stepForm) {
    const stepNumber = Number(stepForm.dataset.step);

    stepForm.addEventListener("submit", async event => {
      event.preventDefault();

      const formData = new FormData(stepForm);
      const data = Object.fromEntries(formData.entries());

      const draft = JSON.parse(
        localStorage.getItem("ecoDraft") || "{}"
      );

      Object.assign(draft, data);
      localStorage.setItem("ecoDraft", JSON.stringify(draft));

      if (stepNumber < 4) {
        const nextPages = {
          1: "/home-energy",
          2: "/food",
          3: "/shopping"
        };

        location.href = nextPages[stepNumber];
        return;
      }

      try {
        await api("/calculate", {
          method: "POST",
          body: JSON.stringify(draft)
        });

        localStorage.removeItem("ecoDraft");
        location.href = "/dashboard";
      } catch (error) {
        document.getElementById("message").textContent = error.message;
      }
    });
  }

  if (location.pathname === "/dashboard") {
    loadDashboard();
  }
});

async function loadDashboard() {
  try {
    const data = await api("/dashboard");

    if (!data.calculation) {
      const emptyMessage = document.getElementById("message");
      if (emptyMessage) {
        emptyMessage.textContent = "No saved calculation yet. Complete the tracker to see your dashboard.";
        emptyMessage.classList.remove("d-none");
      }
      return;
    }

    const calculation = data.calculation;

    document.getElementById("total").textContent =
      Number(calculation.total).toFixed(2);

    document.getElementById("you").textContent =
      Number(calculation.total).toFixed(2);

    document.getElementById("savedAt").textContent =
      "Saved " + new Date(calculation.savedAt).toLocaleString();

    document.getElementById("level").textContent =
      calculation.total < 3
        ? "Low footprint"
        : calculation.total < 6
          ? "Moderate footprint"
          : "High footprint";

    const breakdown = calculation.breakdown;
    const maximum = Math.max(...Object.values(breakdown), 1);

    document.getElementById("breakdown").innerHTML =
      Object.entries(breakdown)
        .map(([category, value]) => `
          <div class="bar">
            <div class="bar-top">
              <span>${category}</span>
              <span>${Number(value).toFixed(2)} t</span>
            </div>
            <div class="bar-track">
              <span style="width:${(Number(value) / maximum) * 100}%"></span>
            </div>
          </div>
        `)
        .join("");

    const insights = document.getElementById("insights");

    if (insights && data.insights) {
      const icons = ["💡", "🌱", "📌", "✨"];
      insights.innerHTML = data.insights
        .map((item, index) => `
          <article class="insight-card">
            <div class="insight-icon">${icons[index % icons.length]}</div>
            <h3>Personalized insight</h3>
            <p>${escapeHtml(item)}</p>
          </article>
        `)
        .join("");
    }

    const dashboardMessage = document.getElementById("message");
    if (dashboardMessage) dashboardMessage.classList.add("d-none");
  } catch (error) {
    const dashboardMessage = document.getElementById("message");
    if (dashboardMessage) {
      dashboardMessage.textContent = error.message || "Unable to load dashboard data.";
      dashboardMessage.classList.remove("d-none");
      dashboardMessage.classList.remove("alert-info");
      dashboardMessage.classList.add("alert-danger");
    }
  }
}
