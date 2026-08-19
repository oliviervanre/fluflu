const STORAGE_KEY = "fluflu.entries.v1";
const CONTEXTS = [
  { key: "lying", label: "Position allongée" },
  { key: "activity", label: "Activité physique" },
  { key: "stress", label: "Stress inhabituel" },
  { key: "medication", label: "Médicament récent" },
  { key: "alcohol", label: "Alcool consommé" }
];

let entries = loadEntries();
let selectedDate = dayKey(new Date());
let analysisWindow = 3;

const $ = (selector, parent = document) => parent.querySelector(selector);
const $$ = (selector, parent = document) => [...parent.querySelectorAll(selector)];

function loadEntries() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || []; }
  catch { return []; }
}

function saveEntries() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
  renderAll();
}

function dayKey(date) {
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function toLocalInputValue(date = new Date()) {
  const d = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return d.toISOString().slice(0, 16);
}

function formatTime(value) {
  return new Intl.DateTimeFormat("fr-FR", { hour: "2-digit", minute: "2-digit" }).format(new Date(value));
}

function formatDay(value, options = {}) {
  return new Intl.DateTimeFormat("fr-FR", options).format(new Date(`${value}T12:00:00`));
}

function escapeHtml(text = "") {
  const node = document.createElement("div");
  node.textContent = text;
  return node.innerHTML;
}

function renderContextQuestions() {
  $("#contextQuestions").innerHTML = CONTEXTS.map(({ key, label }) => `
    <div class="context-row">
      <p>${label}</p>
      <div class="tri-state" role="group" aria-label="${label}">
        ${[["yes", "Oui"], ["no", "Non"], ["unknown", "?"]].map(([value, text]) => `
          <label><input type="radio" name="context-${key}" value="${value}" ${value === "unknown" ? "checked" : ""}><span>${text}</span></label>
        `).join("")}
      </div>
    </div>
  `).join("");
}

function renderJournal() {
  const today = dayKey(new Date());
  const dateEntries = entries.filter(entry => dayKey(entry.at) === selectedDate).sort((a, b) => new Date(a.at) - new Date(b.at));
  const offset = Math.round((new Date(`${selectedDate}T12:00:00`) - new Date(`${today}T12:00:00`)) / 86400000);
  $("#journalTitle").textContent = offset === 0 ? "Aujourd'hui" : offset === -1 ? "Hier" : formatDay(selectedDate, { weekday: "long" });
  $("#selectedDateLabel").textContent = formatDay(selectedDate, { day: "numeric", month: "long", year: "numeric" });
  $("#nextDay").disabled = selectedDate >= today;
  $("#entryCount").textContent = `${dateEntries.length} saisie${dateEntries.length > 1 ? "s" : ""}`;

  const timeline = $("#timeline");
  if (!dateEntries.length) {
    timeline.innerHTML = $("#emptyTimelineTemplate").innerHTML;
    return;
  }

  timeline.innerHTML = dateEntries.map(entry => {
    if (entry.type === "food") {
      const details = [entry.mealType !== "Non précisé" ? entry.mealType : "", entry.quantity, entry.note].filter(Boolean).map(escapeHtml).join(" · ");
      return `<article class="timeline-item">
        <time class="timeline-time">${formatTime(entry.at)}</time>
        <div class="entry-card food">
          <p class="entry-type">Aliment</p><h4>${escapeHtml(entry.name)}</h4>
          ${details ? `<p>${details}</p>` : ""}
          <button class="delete-entry" data-delete="${entry.id}" aria-label="Supprimer cette saisie">×</button>
        </div>
      </article>`;
    }
    const dots = [1,2,3,4].map(i => `<i class="${i <= entry.intensity ? "on" : ""}"></i>`).join("");
    const knownContexts = CONTEXTS.filter(c => entry.context?.[c.key] !== "unknown");
    const contextTags = knownContexts.map(c => `<span class="context-tag ${entry.context[c.key] === "yes" ? "yes" : ""}">${c.label} : ${entry.context[c.key] === "yes" ? "oui" : "non"}</span>`).join("");
    return `<article class="timeline-item">
      <time class="timeline-time">${formatTime(entry.at)}</time>
      <div class="entry-card symptom">
        <p class="entry-type">Reflux <span class="intensity-dots">${dots}</span></p><h4>Intensité ${entry.intensity}/4</h4>
        ${entry.note ? `<p>${escapeHtml(entry.note)}</p>` : ""}
        ${contextTags ? `<div class="context-tags">${contextTags}</div>` : `<p>Contexte non renseigné</p>`}
        <button class="delete-entry" data-delete="${entry.id}" aria-label="Supprimer cette saisie">×</button>
      </div>
    </article>`;
  }).join("");
}

function computeAnalysis(hours) {
  const foods = entries.filter(e => e.type === "food");
  const symptoms = entries.filter(e => e.type === "symptom");
  const grouped = new Map();

  foods.forEach(food => {
    const key = food.name.trim().toLocaleLowerCase("fr-FR");
    if (!grouped.has(key)) grouped.set(key, { name: food.name.trim(), exposures: 0, linked: 0, intensities: [] });
    const item = grouped.get(key);
    item.exposures++;
    const start = new Date(food.at).getTime();
    const linked = symptoms.filter(symptom => {
      const delay = new Date(symptom.at).getTime() - start;
      return delay >= 0 && delay <= hours * 3600000;
    });
    if (linked.length) {
      item.linked++;
      item.intensities.push(Math.max(...linked.map(s => s.intensity)));
    }
  });

  return [...grouped.values()].map(item => ({
    ...item,
    rate: Math.round(item.linked / item.exposures * 100),
    average: item.intensities.length ? item.intensities.reduce((a, b) => a + b, 0) / item.intensities.length : 0
  })).sort((a, b) => b.rate - a.rate || b.exposures - a.exposures || b.average - a.average);
}

function renderAnalysis() {
  const results = computeAnalysis(analysisWindow);
  const symptoms = entries.filter(e => e.type === "symptom");
  const contextAnswers = symptoms.flatMap(s => Object.values(s.context || {}));
  const known = contextAnswers.filter(v => v && v !== "unknown").length;
  const quality = contextAnswers.length ? Math.round(known / contextAnswers.length * 100) : 0;
  $("#qualityCard").innerHTML = `<strong>Qualité du contexte : ${quality}% renseigné</strong>${symptoms.length} reflux saisi${symptoms.length > 1 ? "s" : ""}. Les réponses « non renseigné » ne sont jamais interprétées comme une absence du facteur.`;

  const list = $("#analysisList");
  if (!results.length) {
    list.innerHTML = `<div class="insufficient">Aucune consommation enregistrée. Les premières associations apparaîtront après la saisie d'aliments et de reflux.</div>`;
    return;
  }
  list.innerHTML = results.map(item => `
    <article class="analysis-card">
      <div class="analysis-top"><h3>${escapeHtml(item.name)}</h3><span class="analysis-rate">${item.rate}%</span></div>
      <div class="progress"><span style="width:${item.rate}%"></span></div>
      <div class="analysis-meta"><span>${item.linked}/${item.exposures} consommation${item.exposures > 1 ? "s" : ""} suivie${item.linked > 1 ? "s" : ""} d'un reflux</span><span>${item.average ? `int. moy. ${item.average.toFixed(1)}/4` : "aucun reflux"}</span></div>
    </article>
  `).join("");
}

function renderHistory() {
  const days = [...new Set(entries.map(entry => dayKey(entry.at)))].sort().reverse();
  const list = $("#historyList");
  if (!days.length) {
    list.innerHTML = `<div class="insufficient">L'historique se constituera au fil des saisies.</div>`;
    return;
  }
  list.innerHTML = days.map(day => {
    const dayEntries = entries.filter(entry => dayKey(entry.at) === day);
    const foods = dayEntries.filter(entry => entry.type === "food").length;
    const symptoms = dayEntries.filter(entry => entry.type === "symptom");
    const max = symptoms.length ? Math.max(...symptoms.map(s => s.intensity)) : 0;
    return `<button class="history-day" data-day="${day}">
      <h3>${formatDay(day, { weekday: "long", day: "numeric", month: "long" })}</h3>
      <strong>${symptoms.length ? `${symptoms.length} reflux · max ${max}/4` : "Aucun reflux"}</strong>
      <p>${foods} aliment${foods > 1 ? "s" : ""} ou boisson${foods > 1 ? "s" : ""} consigné${foods > 1 ? "s" : ""}</p>
    </button>`;
  }).join("");
}

function renderSuggestions() {
  const frequent = [...new Set(entries.filter(e => e.type === "food").map(e => e.name))].slice(-5).reverse();
  const defaults = ["Café", "Tomate", "Chocolat", "Agrumes", "Plat épicé"];
  const suggestions = frequent.length ? frequent : defaults;
  $("#foodSuggestions").innerHTML = suggestions.map(name => `<button type="button" data-food-suggestion>${escapeHtml(name)}</button>`).join("");
}

function renderAll() {
  renderJournal();
  renderAnalysis();
  renderHistory();
  renderSuggestions();
}

function openDialog(id) {
  const dialog = document.getElementById(id);
  const now = new Date();
  if (id === "foodDialog") $("#foodDateTime").value = toLocalInputValue(now);
  if (id === "symptomDialog") {
    $("#symptomDateTime").value = toLocalInputValue(now);
    $("#symptomForm").reset();
    $("#symptomDateTime").value = toLocalInputValue(now);
  }
  dialog.showModal();
}

document.addEventListener("click", event => {
  const open = event.target.closest("[data-open-dialog]");
  if (open) openDialog(open.dataset.openDialog);

  const suggestion = event.target.closest("[data-food-suggestion]");
  if (suggestion) $("#foodName").value = suggestion.textContent;

  const close = event.target.closest("[data-close-dialog]");
  if (close) close.closest("dialog").close();

  const remove = event.target.closest("[data-delete]");
  if (remove && confirm("Supprimer cette saisie ?")) {
    entries = entries.filter(entry => entry.id !== remove.dataset.delete);
    saveEntries();
  }

  const navigation = event.target.closest("[data-view]");
  if (navigation) {
    $$(".bottom-nav button").forEach(button => button.classList.toggle("active", button === navigation));
    $$(".view").forEach(view => view.classList.toggle("active", view.id === navigation.dataset.view));
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  const windowButton = event.target.closest("[data-window]");
  if (windowButton) {
    analysisWindow = Number(windowButton.dataset.window);
    $$("[data-window]").forEach(button => button.classList.toggle("active", button === windowButton));
    renderAnalysis();
  }

  const day = event.target.closest("[data-day]");
  if (day) {
    selectedDate = day.dataset.day;
    $("[data-view='journalView']").click();
  }
});

$("#foodForm").addEventListener("submit", event => {
  event.preventDefault();
  if (!event.currentTarget.reportValidity()) return;
  entries.push({
    id: crypto.randomUUID(), type: "food", name: $("#foodName").value.trim(),
    at: new Date($("#foodDateTime").value).toISOString(), mealType: $("#mealType").value,
    quantity: $("#foodQuantity").value.trim(), note: $("#foodNote").value.trim()
  });
  selectedDate = dayKey($("#foodDateTime").value);
  event.currentTarget.reset();
  $("#foodDialog").close();
  saveEntries();
});

$("#symptomForm").addEventListener("submit", event => {
  event.preventDefault();
  if (!event.currentTarget.reportValidity()) return;
  const context = Object.fromEntries(CONTEXTS.map(({ key }) => [key, $(`input[name='context-${key}']:checked`).value]));
  entries.push({
    id: crypto.randomUUID(), type: "symptom", at: new Date($("#symptomDateTime").value).toISOString(),
    intensity: Number($("input[name='intensity']:checked").value), context,
    note: $("#symptomNote").value.trim()
  });
  selectedDate = dayKey($("#symptomDateTime").value);
  $("#symptomDialog").close();
  saveEntries();
});

$("#previousDay").addEventListener("click", () => {
  const date = new Date(`${selectedDate}T12:00:00`); date.setDate(date.getDate() - 1); selectedDate = dayKey(date); renderJournal();
});
$("#nextDay").addEventListener("click", () => {
  const date = new Date(`${selectedDate}T12:00:00`); date.setDate(date.getDate() + 1); selectedDate = dayKey(date); renderJournal();
});
$("#openSettings").addEventListener("click", () => $("#settingsDialog").showModal());

$("#loadDemo").addEventListener("click", () => {
  if (entries.length && !confirm("Remplacer les données actuelles par la démonstration ?")) return;
  const now = new Date();
  const at = (daysAgo, hour, minute = 0) => { const d = new Date(now); d.setDate(d.getDate() - daysAgo); d.setHours(hour, minute, 0, 0); return d.toISOString(); };
  entries = [
    { id: crypto.randomUUID(), type:"food", name:"Café", at:at(4,8,10), mealType:"Petit-déjeuner", quantity:"1 tasse", note:"" },
    { id: crypto.randomUUID(), type:"food", name:"Pain", at:at(4,8,12), mealType:"Petit-déjeuner", quantity:"2 tranches", note:"" },
    { id: crypto.randomUUID(), type:"symptom", at:at(4,10,5), intensity:2, context:{lying:"no",activity:"no",stress:"yes",medication:"unknown",alcohol:"no"}, note:"Brûlure légère" },
    { id: crypto.randomUUID(), type:"food", name:"Tomate", at:at(3,12,35), mealType:"Déjeuner", quantity:"1 portion", note:"En salade" },
    { id: crypto.randomUUID(), type:"symptom", at:at(3,14,10), intensity:3, context:{lying:"no",activity:"no",stress:"no",medication:"no",alcohol:"no"}, note:"" },
    { id: crypto.randomUUID(), type:"food", name:"Café", at:at(2,9,5), mealType:"Petit-déjeuner", quantity:"1 tasse", note:"" },
    { id: crypto.randomUUID(), type:"food", name:"Chocolat", at:at(2,16,20), mealType:"Collation", quantity:"3 carrés", note:"" },
    { id: crypto.randomUUID(), type:"symptom", at:at(2,18,0), intensity:2, context:{lying:"yes",activity:"no",stress:"unknown",medication:"no",alcohol:"no"}, note:"Après repos sur le canapé" },
    { id: crypto.randomUUID(), type:"food", name:"Tomate", at:at(1,19,40), mealType:"Dîner", quantity:"1 portion", note:"Sauce tomate" },
    { id: crypto.randomUUID(), type:"symptom", at:at(1,22,5), intensity:4, context:{lying:"yes",activity:"no",stress:"no",medication:"no",alcohol:"no"}, note:"Reflux important au coucher" },
    { id: crypto.randomUUID(), type:"food", name:"Banane", at:at(0,8,15), mealType:"Petit-déjeuner", quantity:"1", note:"" }
  ];
  selectedDate = dayKey(new Date());
  $("#settingsDialog").close(); saveEntries();
});

$("#clearData").addEventListener("click", () => {
  if (!confirm("Effacer définitivement toutes les saisies de ce navigateur ?")) return;
  entries = []; localStorage.removeItem(STORAGE_KEY); $("#settingsDialog").close(); renderAll();
});

renderContextQuestions();
renderAll();

if ("serviceWorker" in navigator) navigator.serviceWorker.register("./service-worker.js").catch(() => {});
