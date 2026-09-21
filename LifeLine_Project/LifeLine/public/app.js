const API = "/api";

function getUser() {
    try {
        return JSON.parse(localStorage.getItem("lifeLineUser"));
    } catch {
        return null;
    }
}

function setUser(user) {
    localStorage.setItem("lifeLineUser", JSON.stringify(user));
}

function clearUser() {
    localStorage.removeItem("lifeLineUser");
}

function showToast(message, type = "success", duration = 3000, redirect = null) {
    const existing = document.querySelector(".toast");
    if (existing) {
        existing.remove();
    }

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    const icon = type === "success" ? "✓" : "!";
    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-message">${escapeHtml(message)}</span>
    `;

    document.body.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add("show");
    });

    setTimeout(() => {
        toast.classList.remove("show");

        setTimeout(() => {
            toast.remove();

            if (redirect) {
                window.location.href = redirect;
            }
        }, 250);
    }, duration);
}

function showSavedToastIfAny() {
    const saved = sessionStorage.getItem("lifeLineToast");

    if (!saved) {
        return;
    }

    sessionStorage.removeItem("lifeLineToast");

    try {
        const data = JSON.parse(saved);
        showToast(data.message, data.type || "success");
    } catch {
        showToast(saved);
    }
}

function saveToast(message, type = "success") {
    sessionStorage.setItem(
        "lifeLineToast",
        JSON.stringify({ message, type })
    );
}

function requireLogin() {
    const user = getUser();

    if (!user) {
        window.location.href = "login.html";
        return null;
    }

    return user;
}

function renderAccountName() {
    const user = getUser();
    const nameElements = document.querySelectorAll("[data-user-name]");
    const emailElements = document.querySelectorAll("[data-user-email]");

    nameElements.forEach(element => {
        element.textContent = user ? user.name : "Account";
    });

    emailElements.forEach(element => {
        element.textContent = user ? user.email : "";
    });
}

function showConfirmModal({ title, message, confirmText, danger = false }) {
    return new Promise(resolve => {
        const existing = document.querySelector(".confirm-modal");
        if (existing) {
            existing.remove();
        }

        const overlay = document.createElement("div");
        overlay.className = "confirm-modal";
        overlay.innerHTML = `
            <div class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirmTitle">
                <span class="eyebrow">Confirmation</span>
                <h2 id="confirmTitle">${escapeHtml(title)}</h2>
                <p>${escapeHtml(message)}</p>
                <div class="confirm-actions">
                    <button type="button" class="btn btn-secondary" data-confirm-cancel>Cancel</button>
                    <button type="button" class="btn ${danger ? "btn-danger" : "btn-primary"}" data-confirm-ok>${escapeHtml(confirmText)}</button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const close = result => {
            overlay.remove();
            resolve(result);
        };

        overlay.querySelector("[data-confirm-cancel]").addEventListener("click", () => close(false));
        overlay.querySelector("[data-confirm-ok]").addEventListener("click", () => close(true));
        overlay.addEventListener("click", event => {
            if (event.target === overlay) {
                close(false);
            }
        });
    });
}

function bindLogout() {
    document.querySelectorAll("[data-logout]").forEach(button => {
        button.addEventListener("click", async () => {
            const confirmed = await showConfirmModal({
                title: "Are you sure you want to logout?",
                message: "You will be signed out of your LifeLine account.",
                confirmText: "Logout"
            });

            if (!confirmed) {
                return;
            }

            clearUser();
            showToast("Logged out successfully", "success", 2500, "login.html");
        });
    });
}

async function apiRequest(url, options = {}) {
    const response = await fetch(API + url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    let data;

    try {
        data = await response.json();
    } catch {
        data = {
            success: false,
            message: "Server returned an invalid response."
        };
    }

    if (!response.ok || data.success === false) {
        throw new Error(data.message || "Something went wrong.");
    }

    return data;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function formatDate(dateValue) {
    if (!dateValue) {
        return "Not available";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

document.addEventListener("DOMContentLoaded", () => {
    showSavedToastIfAny();
    renderAccountName();
    bindLogout();

    const page = document.body.dataset.page;

    if (page === "register") {
        initRegister();
    }

    if (page === "login") {
        initLogin();
    }

    if (page === "recovery") {
        initRecovery();
    }

    if (page === "dashboard") {
        initDashboard();
    }

    if (page === "create") {
        initCreateCard();
    }

    if (page === "card") {
        initCardPage();
    }

    if (page === "profile") {
        initProfile();
    }

    initDeleteAccountActions();

    document.querySelectorAll("[data-year]").forEach(element => {
        element.textContent = new Date().getFullYear();
    });
});

function initRegister() {
    const form = document.querySelector("#registerForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const name = form.name.value.trim();
        const email = form.email.value.trim();
        const password = form.password.value;
        const confirmPassword = form.confirmPassword.value;

        if (password !== confirmPassword) {
            showToast("Passwords do not match.", "error");
            return;
        }

        const button = form.querySelector("button[type='submit']");
        button.disabled = true;
        button.textContent = "Creating account...";

        try {
            await apiRequest("/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    name,
                    email,
                    password
                })
            });

            showToast("Registration successful", "success", 2500, "login.html");
        } catch (error) {
            showToast(error.message, "error");
            button.disabled = false;
            button.textContent = "Create Account";
        }
    });
}

function initLogin() {
    const form = document.querySelector("#loginForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const email = form.email.value.trim();
        const password = form.password.value;

        const button = form.querySelector("button[type='submit']");
        button.disabled = true;
        button.textContent = "Signing in...";

        try {
            const result = await apiRequest("/auth/login", {
                method: "POST",
                body: JSON.stringify({ email, password })
            });

            setUser(result.user);
            showToast("Login successful", "success", 2500, "dashboard.html");
        } catch (error) {
            showToast(error.message, "error");
            button.disabled = false;
            button.textContent = "Login";
        }
    });
}

function initRecovery() {
    const form = document.querySelector("#recoveryForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const email = form.email.value.trim();
        const newPassword = form.newPassword.value;
        const confirmPassword = form.confirmPassword.value;
        const button = form.querySelector("button[type='submit']");

        if (newPassword !== confirmPassword) {
            showToast("New Password and Confirm Password must match.", "error");
            return;
        }

        button.disabled = true;
        button.textContent = "Changing...";

        try {
            await apiRequest("/auth/change-password", {
                method: "POST",
                body: JSON.stringify({ email, newPassword, confirmPassword })
            });

            showToast("Password changed successfully", "success", 2500, "login.html");
        } catch (error) {
            showToast(error.message, "error");
            button.disabled = false;
            button.textContent = "Change Password";
        }
    });
}

async function initDashboard() {
    const user = requireLogin();

    if (!user) {
        return;
    }

    document.querySelectorAll("[data-dashboard-name]").forEach(element => {
        element.textContent = user.name;
    });

    try {
        const result = await apiRequest(
            `/cards?userId=${encodeURIComponent(user.id)}`
        );

        const card = result.cards?.[0];

        const emptyState = document.querySelector("#emptyState");
        const cardSummary = document.querySelector("#cardSummary");

        if (card) {
            if (emptyState) {
                emptyState.hidden = true;
            }

            if (cardSummary) {
                cardSummary.hidden = false;

                cardSummary.innerHTML = `
                    <div class="summary-head">
                        <div>
                            <span class="eyebrow">Your Emergency Card</span>
                            <h2>${escapeHtml(card.name)}</h2>
                        </div>

                        <span class="status-pill">Active</span>
                    </div>

                    <div class="summary-grid">
                        <div>
                            <span>Blood Group</span>
                            <strong>${escapeHtml(card.bloodGroup || "Not added")}</strong>
                        </div>

                        <div>
                            <span>Emergency Contact</span>
                            <strong>${escapeHtml(card.emergencyName || "Not added")}</strong>
                        </div>

                        <div>
                            <span>Phone</span>
                            <strong>${escapeHtml(card.phone || "Not added")}</strong>
                        </div>

                        <div>
                            <span>Last Updated</span>
                            <strong>${escapeHtml(formatDate(card.updatedAt))}</strong>
                        </div>
                    </div>

                    <div class="button-row">
                        <a class="btn btn-primary" href="card.html">
                            View Emergency Card
                        </a>

                        <a class="btn btn-secondary" href="create.html">
                            Edit Card
                        </a>
                    </div>
                `;
            }
        } else {
            if (emptyState) {
                emptyState.hidden = false;
            }

            if (cardSummary) {
                cardSummary.hidden = true;
            }
        }
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function initCreateCard() {
    const user = requireLogin();

    if (!user) {
        return;
    }

    const form = document.querySelector("#cardForm");

    if (!form) {
        return;
    }

    let editingCard = null;

    try {
        const result = await apiRequest(
            `/cards?userId=${encodeURIComponent(user.id)}`
        );

        editingCard = result.cards?.[0] || null;

        if (editingCard) {
            fillCardForm(form, editingCard);
            document.querySelector("#formTitle").textContent = "Update Emergency Card";
            document.querySelector("#formSubtitle").textContent =
                "Keep your emergency information accurate and up to date.";
            document.querySelector("#submitCard").textContent = "Update Card";
        }
    } catch (error) {
        showToast(error.message, "error");
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const data = readCardForm(form);

        data.userId = user.id;

        const button = document.querySelector("#submitCard");
        button.disabled = true;
        button.textContent = editingCard ? "Updating..." : "Saving...";

        try {
            const result = await apiRequest("/cards", {
                method: editingCard ? "PUT" : "POST",
                body: JSON.stringify({
                    ...data,
                    ...(editingCard ? { id: editingCard.id } : {})
                })
            });

            editingCard = result.card;

            const message = editingCard && button.textContent === "Updating..."
                ? "Card updated successfully"
                : "Card added successfully";

            showToast(message, "success", 2500, "card.html");
        } catch (error) {
            showToast(error.message, "error");
            button.disabled = false;
            button.textContent = editingCard ? "Update Card" : "Save Emergency Card";
        }
    });
}

function readCardForm(form) {
    const data = {};

    [
        "name",
        "dobAge",
        "phone",
        "address",
        "emergencyName",
        "emergencyPhone",
        "relationship",
        "bloodGroup",
        "allergies",
        "medicalConditions",
        "currentMedications",
        "importantNotes",
        "doctorName",
        "doctorPhone"
    ].forEach(field => {
        data[field] = form[field]?.value.trim() || "";
    });

    return data;
}

function fillCardForm(form, card) {
    Object.keys(card).forEach(key => {
        if (form[key]) {
            form[key].value = card[key] || "";
        }
    });
}

async function initCardPage() {
    const user = requireLogin();

    if (!user) {
        return;
    }

    const container = document.querySelector("#cardView");

    try {
        const result = await apiRequest(
            `/cards?userId=${encodeURIComponent(user.id)}`
        );

        const card = result.cards?.[0];

        if (!card) {
            container.innerHTML = `
                <div class="empty-panel">
                    <div class="empty-icon">🆘</div>
                    <h2>No emergency card yet</h2>
                    <p>Create your LifeLine card so important information is ready when needed.</p>
                    <a class="btn btn-primary" href="create.html">Create Emergency Card</a>
                </div>
            `;
            return;
        }

        renderEmergencyCard(container, card);

        document.querySelector("#deleteCard")?.addEventListener("click", async () => {
            const confirmed = await showConfirmModal({
                title: "Delete Emergency Card?",
                message: "Your emergency card will be permanently deleted. This action cannot be undone.",
                confirmText: "Delete Card",
                danger: true
            });

            if (!confirmed) {
                return;
            }

            try {
                await apiRequest("/cards", {
                    method: "DELETE",
                    body: JSON.stringify({
                        id: card.id,
                        userId: user.id
                    })
                });

                showToast("Card deleted successfully", "success", 2500, "dashboard.html");
            } catch (error) {
                showToast(error.message, "error");
            }
        });
    } catch (error) {
        showToast(error.message, "error");
    }
}

function renderEmergencyCard(container, card) {
    container.innerHTML = `
        <article class="emergency-card">
            <div class="emergency-card-top">
                <div>
                    <span class="eyebrow">LIFELINE EMERGENCY CARD</span>
                    <h1>${escapeHtml(card.name)}</h1>
                    <p class="muted">
                        Last updated ${escapeHtml(formatDate(card.updatedAt))}
                    </p>
                </div>

                <div class="emergency-badge">EMERGENCY</div>
            </div>

            <div class="emergency-highlights">
                <div class="highlight highlight-blood">
                    <span>🩸</span>
                    <small>Blood Group</small>
                    <strong>${escapeHtml(card.bloodGroup || "Not added")}</strong>
                </div>

                <div class="highlight highlight-alert">
                    <span>⚠️</span>
                    <small>Allergies</small>
                    <strong>${escapeHtml(card.allergies || "None added")}</strong>
                </div>

                <div class="highlight highlight-medical">
                    <span>💊</span>
                    <small>Medications</small>
                    <strong>${escapeHtml(card.currentMedications || "None added")}</strong>
                </div>
            </div>

            <div class="emergency-section">
                <h3>Personal Information</h3>

                <div class="detail-grid">
                    <div>
                        <span>Date of Birth / Age</span>
                        <strong>${escapeHtml(card.dobAge || "Not added")}</strong>
                    </div>

                    <div>
                        <span>Phone Number</span>
                        <strong>
                            <a href="tel:${escapeHtml(card.phone)}">
                                ${escapeHtml(card.phone || "Not added")}
                            </a>
                        </strong>
                    </div>

                    <div class="detail-wide">
                        <span>Home Address</span>
                        <strong>${escapeHtml(card.address || "Not added")}</strong>
                    </div>
                </div>
            </div>

            <div class="emergency-section emergency-contact-box">
                <h3>🚨 Emergency Contact</h3>

                <div class="contact-main">
                    <div>
                        <span>Contact Name</span>
                        <strong>${escapeHtml(card.emergencyName || "Not added")}</strong>
                    </div>

                    <div>
                        <span>Relationship</span>
                        <strong>${escapeHtml(card.relationship || "Not added")}</strong>
                    </div>

                    <div>
                        <span>Phone</span>
                        <strong>
                            <a href="tel:${escapeHtml(card.emergencyPhone)}">
                                ${escapeHtml(card.emergencyPhone || "Not added")}
                            </a>
                        </strong>
                    </div>
                </div>

                ${
                    card.emergencyPhone
                        ? `<a class="btn btn-emergency" href="tel:${escapeHtml(card.emergencyPhone)}">📞 Call Emergency Contact</a>`
                        : ""
                }
            </div>

            <div class="emergency-section">
                <h3>Medical Information</h3>

                <div class="detail-grid">
                    <div class="detail-wide">
                        <span>Medical Conditions</span>
                        <strong>${escapeHtml(card.medicalConditions || "None added")}</strong>
                    </div>

                    <div class="detail-wide">
                        <span>Important Notes</span>
                        <strong>${escapeHtml(card.importantNotes || "None added")}</strong>
                    </div>
                </div>
            </div>

            ${
                card.doctorName || card.doctorPhone
                    ? `
                    <div class="emergency-section">
                        <h3>Primary Doctor</h3>

                        <div class="detail-grid">
                            <div>
                                <span>Doctor Name</span>
                                <strong>${escapeHtml(card.doctorName || "Not added")}</strong>
                            </div>

                            <div>
                                <span>Doctor / Clinic Phone</span>
                                <strong>
                                    <a href="tel:${escapeHtml(card.doctorPhone)}">
                                        ${escapeHtml(card.doctorPhone || "Not added")}
                                    </a>
                                </strong>
                            </div>
                        </div>
                    </div>
                    `
                    : ""
            }
        </article>
    `;
}

function initDeleteAccountActions() {
    document.querySelectorAll("[data-delete-account]").forEach(button => {
        button.addEventListener("click", async () => {
            const user = getUser();
            if (!user) {
                window.location.href = "login.html";
                return;
            }

            const confirmed = await showConfirmModal({
                title: "Are you sure?",
                message: "Your account and emergency card will be permanently deleted. This action cannot be undone.",
                confirmText: "Delete Account",
                danger: true
            });

            if (!confirmed) {
                return;
            }

            try {
                await apiRequest("/auth/delete", {
                    method: "POST",
                    body: JSON.stringify({ userId: user.id })
                });

                clearUser();
                showToast("Account deleted successfully", "success", 2500, "index.html");
            } catch (error) {
                showToast(error.message, "error");
            }
        });
    });
}

async function initProfile() {
    const user = requireLogin();

    if (!user) {
        return;
    }

    document.querySelector("#profileName").textContent = user.name;
    document.querySelector("#profileEmail").textContent = user.email;
    document.querySelector("#profileId").textContent = user.id;
}

