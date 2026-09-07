(() => {
  "use strict";

  const STORE_KEY = "cyberquiz_pwa_state_v1";
  const LETTERS = ["A", "B", "C", "D"];
  const DIFFICULTY_LABELS = { EASY: "Facile", MEDIUM: "Moyen", HARD: "Difficile" };
  const DIFFICULTY_XP = { EASY: 10, MEDIUM: 15, HARD: 20 };

  const app = document.getElementById("app");
  const homeButton = document.getElementById("homeButton");
  const statsButton = document.getElementById("statsButton");
  const navButtons = [...document.querySelectorAll(".bottom-nav button")];

  let questions = [];
  let state = loadState();
  let route = "home";
  let config = {
    difficulty: "ALL",
    count: 10,
    categories: new Set()
  };
  let session = null;

  function defaultState() {
    return {
      answered: 0,
      correct: 0,
      streak: 0,
      bestStreak: 0,
      xp: 0,
      categoryStats: {},
      history: []
    };
  }

  function loadState() {
    try {
      const parsed = JSON.parse(localStorage.getItem(STORE_KEY) || "null");
      return { ...defaultState(), ...(parsed || {}) };
    } catch (_) {
      return defaultState();
    }
  }

  function saveState() {
    localStorage.setItem(STORE_KEY, JSON.stringify(state));
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function shuffle(items) {
    const arr = [...items];
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr;
  }

  function accuracy(correct = state.correct, answered = state.answered) {
    return answered ? Math.round((correct / answered) * 100) : 0;
  }

  function level() {
    return Math.floor(state.xp / 100) + 1;
  }

  function setRoute(next) {
    route = next;
    navButtons.forEach(btn => btn.classList.toggle("active", btn.dataset.nav === next));
    window.scrollTo({ top: 0, behavior: "instant" });
    render();
  }

  function toast(message) {
    const old = document.querySelector(".toast");
    old?.remove();
    const el = document.createElement("div");
    el.className = "toast";
    el.textContent = message;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2800);
  }

  function isStandalone() {
    return window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone === true;
  }

  function isIOS() {
    return /iphone|ipad|ipod/i.test(navigator.userAgent);
  }

  function installCard() {
    if (isStandalone()) return "";
    const message = isIOS()
      ? "Sur iPhone : ouvre cette page dans Safari, touche Partager, puis « Sur l’écran d’accueil »."
      : "Tu peux installer CyberQuiz depuis le menu de ton navigateur pour l’utiliser comme une application.";
    return `
      <div class="install-card">
        <strong>Installer CyberQuiz gratuitement</strong><br>
        ${escapeHtml(message)}
        <button type="button" class="secondary-btn" id="installHelpBtn">Voir comment faire</button>
      </div>
    `;
  }

  function homeTemplate() {
    const acc = accuracy();
    const historyCount = state.history.length;
    return `
      <section class="screen">
        <div class="hero">
          <div class="eyebrow">CYBERSÉCURITÉ · PWA</div>
          <div class="hero-logo" aria-hidden="true"></div>
          <h1>Teste tes connaissances Cyber.</h1>
          <p>Questions, progression et historique directement sur ton iPhone. Tes données restent enregistrées sur l’appareil.</p>
          <div style="height:14px"></div>
          <button type="button" class="primary-btn" id="startQuizBtn">▶ Commencer un quiz</button>
        </div>

        ${installCard()}

        <div class="stats-row">
          <div class="stat"><strong>${level()}</strong><span>NIVEAU</span></div>
          <div class="stat"><strong>${acc}%</strong><span>RÉUSSITE</span></div>
          <div class="stat"><strong>${state.xp}</strong><span>XP</span></div>
        </div>

        <div class="dashboard grid">
          <article class="card clickable" data-card="stats">
            <div class="icon">⌁</div><h3>Mes statistiques</h3><p>Suis ta progression Cyber.</p>
          </article>
          <article class="card clickable" data-card="quiz">
            <div class="icon">◎</div><h3>Catégories</h3><p>Choisis les thèmes à travailler.</p>
          </article>
          <article class="card clickable" data-card="history">
            <div class="icon">◷</div><h3>Historique</h3><p>${historyCount ? `${historyCount} quiz enregistrés` : "Tes derniers quiz apparaîtront ici."}</p>
          </article>
          <article class="card clickable" data-card="reset">
            <div class="icon" style="color:var(--red)">⌫</div><h3>Effacer les données</h3><p>Réinitialise uniquement la PWA.</p>
          </article>
        </div>

        <div class="section-title">
          <div><div class="section-label">BANQUE CYBER</div><h2>${questions.length} questions</h2></div>
        </div>
        <div class="card">
          <p>La PWA utilise automatiquement la banque Cyber du projet Android lors du déploiement GitHub Pages.</p>
        </div>
      </section>
    `;
  }

  function quizConfigTemplate() {
    const categories = [...new Set(questions.map(q => q.category))].sort((a, b) => a.localeCompare(b, "fr"));
    if (!config.categories.size) categories.forEach(c => config.categories.add(c));
    const available = filteredQuestions().length;
    const countOptions = [10, 20, 50].filter(n => n <= questions.length);

    return `
      <section class="screen">
        <div class="section-title">
          <div><div class="section-label">NOUVEAU QUIZ</div><h2>Configure ton entraînement</h2><p>${available} question${available > 1 ? "s" : ""} disponible${available > 1 ? "s" : ""}</p></div>
        </div>

        <fieldset class="fieldset">
          <legend>DIFFICULTÉ</legend>
          <div class="segmented" id="difficultySelector">
            ${[
              ["ALL", "Aléatoire"],
              ["EASY", "Facile"],
              ["MEDIUM", "Moyen"],
              ["HARD", "Difficile"]
            ].map(([value, label]) => `
              <button type="button" data-value="${value}" class="${config.difficulty === value ? "selected" : ""}">${label}</button>
            `).join("")}
          </div>
        </fieldset>

        <fieldset class="fieldset">
          <legend>NOMBRE DE QUESTIONS</legend>
          <div class="segmented" id="countSelector">
            ${countOptions.map(n => `<button type="button" data-value="${n}" class="${config.count === n ? "selected" : ""}">${n}</button>`).join("")}
            <button type="button" data-value="ALL" class="${config.count === "ALL" ? "selected" : ""}">Toutes</button>
          </div>
        </fieldset>

        <fieldset class="fieldset">
          <legend>CATÉGORIES</legend>
          <div class="chip-grid" id="categorySelector">
            <button type="button" class="chip ${config.categories.size === categories.length ? "selected" : ""}" data-category="__ALL__">Toutes</button>
            ${categories.map(category => `
              <button type="button" class="chip ${config.categories.has(category) ? "selected" : ""}" data-category="${escapeHtml(category)}">${escapeHtml(category)}</button>
            `).join("")}
          </div>
        </fieldset>

        <button type="button" class="primary-btn" id="launchQuizBtn" ${available ? "" : "disabled"}>Lancer le quiz</button>
      </section>
    `;
  }

  function filteredQuestions() {
    return questions.filter(q =>
      (config.difficulty === "ALL" || q.difficulty === config.difficulty) &&
      config.categories.has(q.category)
    );
  }

  function startQuiz() {
    const pool = shuffle(filteredQuestions());
    if (!pool.length) {
      toast("Aucune question ne correspond à cette sélection.");
      return;
    }
    const wanted = config.count === "ALL" ? pool.length : Math.min(Number(config.count), pool.length);
    session = {
      questions: pool.slice(0, wanted),
      index: 0,
      correct: 0,
      locked: false,
      selectedIndex: null,
      answered: []
    };
    route = "quiz-playing";
    navButtons.forEach(btn => btn.classList.toggle("active", btn.dataset.nav === "quiz"));
    render();
  }

  function quizTemplate() {
    if (!session) {
      route = "quiz";
      return quizConfigTemplate();
    }
    const q = session.questions[session.index];
    const done = session.index;
    const total = session.questions.length;
    const progress = Math.round((done / total) * 100);
    const selected = session.selectedIndex;

    return `
      <section class="screen">
        <div class="quiz-top">
          <div>
            <div class="section-label">QUESTION ${session.index + 1} / ${total}</div>
            <div class="progress-wrap"><div class="progress-bar" style="width:${progress}%"></div></div>
          </div>
          <button type="button" class="icon-button" id="quitQuizBtn" aria-label="Quitter">×</button>
        </div>

        <div class="quiz-meta">
          <span class="badge">${escapeHtml(q.category)}</span>
          <span class="badge ${q.difficulty.toLowerCase()}">${DIFFICULTY_LABELS[q.difficulty] || q.difficulty}</span>
          <span class="badge">✓ ${session.correct}</span>
        </div>

        <article class="question-card">
          <h2>${escapeHtml(q.question)}</h2>
          <div class="answers">
            ${q.answers.map((answer, idx) => {
              let cls = "answer";
              if (session.locked && idx === q.correctIndex) cls += " correct";
              if (session.locked && idx === selected && idx !== q.correctIndex) cls += " wrong";
              return `<button type="button" class="${cls}" data-answer="${idx}" ${session.locked ? "disabled" : ""}><span class="letter">${LETTERS[idx]}</span>${escapeHtml(answer)}</button>`;
            }).join("")}
          </div>

          ${session.locked ? `
            <div class="explanation">
              <strong>${selected === q.correctIndex ? "Bonne réponse" : "À retenir"}</strong>
              ${escapeHtml(q.explanation)}
            </div>
            <div style="height:12px"></div>
            <button type="button" class="primary-btn" id="nextQuestionBtn">${session.index + 1 === total ? "Voir mon résultat" : "Question suivante"}</button>
          ` : ""}
        </article>
      </section>
    `;
  }

  function answerCurrent(index) {
    if (!session || session.locked) return;
    const q = session.questions[session.index];
    const correct = index === q.correctIndex;

    session.locked = true;
    session.selectedIndex = index;
    session.answered.push({ id: q.id, correct, category: q.category, difficulty: q.difficulty });

    state.answered += 1;
    if (correct) {
      session.correct += 1;
      state.correct += 1;
      state.streak += 1;
      state.bestStreak = Math.max(state.bestStreak, state.streak);
      state.xp += DIFFICULTY_XP[q.difficulty] || 10;
    } else {
      state.streak = 0;
    }

    const cat = state.categoryStats[q.category] || { answered: 0, correct: 0 };
    cat.answered += 1;
    if (correct) cat.correct += 1;
    state.categoryStats[q.category] = cat;
    saveState();
    render();
  }

  function nextQuestion() {
    if (!session) return;
    if (session.index + 1 >= session.questions.length) {
      finishQuiz();
      return;
    }
    session.index += 1;
    session.locked = false;
    session.selectedIndex = null;
    render();
  }

  function finishQuiz() {
    const total = session.questions.length;
    const correct = session.correct;
    const score = accuracy(correct, total);
    const entry = {
      id: Date.now(),
      date: new Date().toISOString(),
      total,
      correct,
      score,
      difficulty: config.difficulty,
      categories: [...config.categories]
    };
    state.history.unshift(entry);
    state.history = state.history.slice(0, 25);
    saveState();
    session = { ...session, finished: true, result: entry };
    route = "result";
    render();
  }

  function resultTemplate() {
    const result = session?.result;
    if (!result) {
      setRoute("home");
      return "";
    }
    const message = result.score === 100 ? "Parfait !" :
      result.score >= 85 ? "Très solide." :
      result.score >= 65 ? "Bon résultat." :
      result.score >= 51 ? "Quiz validé." :
      "Encore quelques notions à renforcer.";

    return `
      <section class="screen">
        <div class="hero" style="text-align:center">
          <div class="eyebrow">QUIZ TERMINÉ</div>
          <div class="result-ring" style="--score:${result.score}%">
            <div><strong>${result.score}%</strong><small>${result.correct}/${result.total}</small></div>
          </div>
          <h1>${message}</h1>
          <p>Ta progression a été enregistrée sur cet appareil.</p>
          <div style="height:14px"></div>
          <button type="button" class="primary-btn" id="replayBtn">Rejouer</button>
          <div style="height:9px"></div>
          <button type="button" class="secondary-btn" id="anotherQuizBtn">Faire un autre quiz</button>
        </div>
      </section>
    `;
  }

  function statsTemplate() {
    const categories = Object.entries(state.categoryStats)
      .sort((a, b) => b[1].answered - a[1].answered);
    return `
      <section class="screen">
        <div class="section-title">
          <div><div class="section-label">PROGRESSION</div><h2>Mes statistiques</h2></div>
        </div>

        <div class="stats-row">
          <div class="stat"><strong>${accuracy()}%</strong><span>RÉUSSITE</span></div>
          <div class="stat"><strong>${state.bestStreak}</strong><span>MEILLEURE SÉRIE</span></div>
          <div class="stat"><strong>${state.xp}</strong><span>XP</span></div>
        </div>

        <div class="card">
          <div class="section-label">NIVEAU ${level()}</div>
          <h3 style="margin:7px 0 5px">${state.answered} réponses données</h3>
          <p>${state.correct} bonnes réponses · série actuelle ${state.streak}</p>
          <div class="progress-wrap" style="margin-top:12px">
            <div class="progress-bar" style="width:${state.xp % 100}%"></div>
          </div>
          <p style="margin-top:6px">${state.xp % 100} / 100 XP vers le niveau suivant</p>
        </div>

        <div class="section-title"><div><div class="section-label">PAR THÈME</div><h2>Catégories</h2></div></div>
        ${categories.length ? `
          <div class="list">
            ${categories.map(([name, s]) => `
              <div class="list-row">
                <div class="grow"><strong>${escapeHtml(name)}</strong><small>${s.correct}/${s.answered} bonnes réponses</small></div>
                <div class="value">${accuracy(s.correct, s.answered)}%</div>
              </div>
            `).join("")}
          </div>
        ` : `<div class="empty">Fais un premier quiz pour commencer ta progression.</div>`}
      </section>
    `;
  }

  function historyTemplate() {
    return `
      <section class="screen">
        <div class="section-title">
          <div><div class="section-label">25 DERNIERS MAXIMUM</div><h2>Historique</h2></div>
        </div>
        ${state.history.length ? `
          <div class="list">
            ${state.history.map(item => `
              <div class="list-row">
                <div class="grow">
                  <strong>${new Date(item.date).toLocaleDateString("fr-FR", { day: "2-digit", month: "2-digit", year: "numeric" })}</strong>
                  <small>${item.correct}/${item.total} bonnes réponses</small>
                </div>
                <div class="value">${item.score}%</div>
              </div>
            `).join("")}
          </div>
        ` : `<div class="empty">Aucun quiz terminé pour le moment.</div>`}
      </section>
    `;
  }

  function render() {
    if (route === "home") app.innerHTML = homeTemplate();
    else if (route === "quiz") app.innerHTML = quizConfigTemplate();
    else if (route === "quiz-playing") app.innerHTML = quizTemplate();
    else if (route === "result") app.innerHTML = resultTemplate();
    else if (route === "stats") app.innerHTML = statsTemplate();
    else if (route === "history") app.innerHTML = historyTemplate();
    bindScreenEvents();
  }

  function bindScreenEvents() {
    document.getElementById("startQuizBtn")?.addEventListener("click", () => setRoute("quiz"));
    document.getElementById("installHelpBtn")?.addEventListener("click", () => {
      alert(isIOS()
        ? "Sur iPhone : 1) ouvre CyberQuiz dans Safari, 2) touche le bouton Partager, 3) choisis « Sur l’écran d’accueil », 4) touche Ajouter."
        : "Ouvre le menu de ton navigateur puis choisis l’option d’installation ou « Ajouter à l’écran d’accueil »."
      );
    });

    document.querySelectorAll("[data-card]").forEach(card => {
      card.addEventListener("click", () => {
        const target = card.dataset.card;
        if (target === "reset") {
          if (confirm("Effacer toutes les données enregistrées par la PWA CyberQuiz sur cet appareil ?")) {
            state = defaultState();
            localStorage.removeItem(STORE_KEY);
            toast("Données PWA effacées.");
            render();
          }
        } else {
          setRoute(target);
        }
      });
    });

    document.querySelectorAll("#difficultySelector button").forEach(btn => {
      btn.addEventListener("click", () => {
        config.difficulty = btn.dataset.value;
        render();
      });
    });

    document.querySelectorAll("#countSelector button").forEach(btn => {
      btn.addEventListener("click", () => {
        config.count = btn.dataset.value === "ALL" ? "ALL" : Number(btn.dataset.value);
        render();
      });
    });

    document.querySelectorAll("#categorySelector button").forEach(btn => {
      btn.addEventListener("click", () => {
        const category = btn.dataset.category;
        const allCategories = [...new Set(questions.map(q => q.category))];
        if (category === "__ALL__") {
          if (config.categories.size === allCategories.length) config.categories.clear();
          else config.categories = new Set(allCategories);
        } else {
          if (config.categories.has(category)) config.categories.delete(category);
          else config.categories.add(category);
        }
        render();
      });
    });

    document.getElementById("launchQuizBtn")?.addEventListener("click", startQuiz);
    document.querySelectorAll("[data-answer]").forEach(btn => btn.addEventListener("click", () => answerCurrent(Number(btn.dataset.answer))));
    document.getElementById("nextQuestionBtn")?.addEventListener("click", nextQuestion);
    document.getElementById("quitQuizBtn")?.addEventListener("click", () => {
      if (confirm("Quitter ce quiz ? Les réponses déjà données restent dans tes statistiques.")) {
        session = null;
        setRoute("home");
      }
    });
    document.getElementById("replayBtn")?.addEventListener("click", () => {
      session = null;
      startQuiz();
    });
    document.getElementById("anotherQuizBtn")?.addEventListener("click", () => {
      session = null;
      setRoute("quiz");
    });
  }

  homeButton.addEventListener("click", () => {
    session = null;
    setRoute("home");
  });
  statsButton.addEventListener("click", () => setRoute("stats"));
  navButtons.forEach(btn => btn.addEventListener("click", () => {
    const target = btn.dataset.nav;
    if (target === "quiz") {
      session = null;
      setRoute("quiz");
    } else {
      setRoute(target);
    }
  }));

  async function init() {
    try {
      const response = await fetch("./questions.json", { cache: "no-store" });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      questions = await response.json();
      if (!Array.isArray(questions) || !questions.length) throw new Error("Banque de questions vide");
      config.categories = new Set(questions.map(q => q.category));
      render();
    } catch (error) {
      app.innerHTML = `
        <section class="screen">
          <div class="hero">
            <div class="eyebrow">ERREUR DE CHARGEMENT</div>
            <h1>Impossible de charger les questions.</h1>
            <p>${escapeHtml(error.message || error)}</p>
            <div style="height:14px"></div>
            <button class="primary-btn" type="button" onclick="location.reload()">Réessayer</button>
          </div>
        </section>
      `;
    }

    if ("serviceWorker" in navigator) {
      window.addEventListener("load", () => {
        navigator.serviceWorker.register("./sw.js").catch(() => {});
      });
    }
  }

  init();
})();
