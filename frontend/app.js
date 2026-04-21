import React, { useEffect, useMemo, useState } from "https://esm.sh/react@18.3.1";
import { createRoot } from "https://esm.sh/react-dom@18.3.1/client";
import htm from "https://esm.sh/htm@3.1.1";

const html = htm.bind(React.createElement);
const TOKEN_KEY = "astropi_token";
const API_BASE_URL = "http://localhost:8080";

async function apiRequest(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = {
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {})
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === "object" && payload !== null
        ? payload.mensaje || payload.error || "Error en la peticion"
        : "Error en la peticion";
    throw new Error(message);
  }

  return payload;
}

function LoginScreen({ onLogin, error, loading }) {
  const [form, setForm] = useState({ username: "superadmin", password: "admin123" });

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onLogin(form);
  }

  return html`
    <main className="login-shell">
      <section className="login-hero">
        <div className="login-copy">
          <p className="eyebrow">AstroPI</p>
          <h1>Accede y empieza a trabajar con tickets reales.</h1>
          <p className="login-text">
            Incidencias, peticiones y administracion sobre la API Spring Boot ya conectada.
          </p>
        </div>
        <form className="login-form" onSubmit=${handleSubmit}>
          <label>
            <span>Username</span>
            <input
              type="text"
              value=${form.username}
              onChange=${(event) => updateField("username", event.target.value)}
              required
            />
          </label>
          <label>
            <span>Password</span>
            <input
              type="password"
              value=${form.password}
              onChange=${(event) => updateField("password", event.target.value)}
              required
            />
          </label>
          ${error ? html`<p className="form-error">${error}</p>` : null}
          <button className="primary-button" type="submit" disabled=${loading}>
            ${loading ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </section>
    </main>
  `;
}

function Header({ user, activeView, onChangeView, onLogout }) {
  const items = [
    { id: "dashboard", label: "Resumen" },
    { id: "incidencias", label: "Incidencias" },
    { id: "peticiones", label: "Peticiones" }
  ];

  return html`
    <header className="app-header">
      <div>
        <p className="eyebrow">AstroPI</p>
        <h1>${user.nombre || user.username}</h1>
        <p className="header-meta">
          ${user.rol} · ${user.grupo || "Sin grupo"}
        </p>
      </div>
      <nav className="main-nav">
        ${items.map(
          (item) => html`
            <button
              className=${activeView === item.id ? "nav-button active" : "nav-button"}
              onClick=${() => onChangeView(item.id)}
            >
              ${item.label}
            </button>
          `
        )}
      </nav>
      <button className="secondary-button" onClick=${onLogout}>Salir</button>
    </header>
  `;
}

function Dashboard({ incidencias, peticiones }) {
  const cards = [
    {
      title: "Incidencias",
      value: incidencias.length,
      subtitle: "tickets cargados en esta sesion",
      image:
        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80"
    },
    {
      title: "Peticiones",
      value: peticiones.length,
      subtitle: "peticiones visibles ahora mismo",
      image:
        "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=900&q=80"
    }
  ];

  return html`
    <section className="dashboard-grid">
      ${cards.map(
        (card) => html`
          <article className="metric-tile">
            <img src=${card.image} alt=${card.title} />
            <div className="metric-copy">
              <p>${card.title}</p>
              <strong>${card.value}</strong>
              <span>${card.subtitle}</span>
            </div>
          </article>
        `
      )}
    </section>
  `;
}

function TicketsView({
  title,
  endpoint,
  estadoOptions,
  groups,
  tickets,
  onRefresh,
  onChangeStatus
}) {
  const emptyForm = useMemo(
    () => ({
      titulo: "",
      descripcion: "",
      servicio: "",
      categoria: "",
      grupoId: groups[0]?.id || 1
    }),
    [groups]
  );

  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({ estado: "ABIERTA" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setForm((current) => ({ ...current, grupoId: groups[0]?.id || current.grupoId || 1 }));
  }, [groups]);

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleCreate(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      await apiRequest(`/${endpoint}`, {
        method: "POST",
        body: JSON.stringify({
          ...form,
          grupoId: Number(form.grupoId)
        })
      });

      setMessage(`${title.slice(0, -1)} creada correctamente`);
      setForm({
        ...emptyForm,
        grupoId: groups[0]?.id || 1
      });
      await onRefresh(filters);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function applyFilters(nextFilters) {
    setFilters(nextFilters);
    await onRefresh(nextFilters);
  }

  return html`
    <section className="content-grid">
      <form className="editor-panel" onSubmit=${handleCreate}>
        <div className="section-heading">
          <p className="eyebrow">${title}</p>
          <h2>Crear ${title.slice(0, -1).toLowerCase()}</h2>
        </div>
        <label>
          <span>Titulo</span>
          <input
            type="text"
            value=${form.titulo}
            onChange=${(event) => updateForm("titulo", event.target.value)}
            required
          />
        </label>
        <label>
          <span>Descripcion</span>
          <textarea
            value=${form.descripcion}
            onChange=${(event) => updateForm("descripcion", event.target.value)}
            required
          ></textarea>
        </label>
        <div className="field-row">
          <label>
            <span>Servicio</span>
            <input
              type="text"
              value=${form.servicio}
              onChange=${(event) => updateForm("servicio", event.target.value)}
              required
            />
          </label>
          <label>
            <span>Categoria</span>
            <input
              type="text"
              value=${form.categoria}
              onChange=${(event) => updateForm("categoria", event.target.value)}
              required
            />
          </label>
        </div>
        <label>
          <span>Grupo</span>
          <select
            value=${String(form.grupoId)}
            onChange=${(event) => updateForm("grupoId", event.target.value)}
            required
          >
            ${groups.map(
              (group) => html`<option value=${group.id}>${group.nombre}</option>`
            )}
          </select>
        </label>
        ${message ? html`<p className="form-success">${message}</p>` : null}
        ${error ? html`<p className="form-error">${error}</p>` : null}
        <button className="primary-button" type="submit">Guardar</button>
      </form>

      <section className="list-panel">
        <div className="section-heading section-heading-inline">
          <div>
            <p className="eyebrow">Listado</p>
            <h2>${title}</h2>
          </div>
          <div className="filter-strip">
            <select
              value=${filters.estado}
              onChange=${(event) => applyFilters({ ...filters, estado: event.target.value })}
            >
              ${estadoOptions.map(
                (estado) => html`<option value=${estado}>${estado}</option>`
              )}
            </select>
            <button className="secondary-button" onClick=${() => onRefresh(filters)}>
              Recargar
            </button>
          </div>
        </div>

        <div className="ticket-list">
          ${tickets.length === 0
            ? html`<p className="empty-state">No hay resultados para este filtro.</p>`
            : tickets.map(
                (ticket) => html`
                  <article className="ticket-row">
                    <div>
                      <strong>${ticket.codigoTicket}</strong>
                      <h3>${ticket.titulo}</h3>
                      <p>${ticket.descripcion}</p>
                    </div>
                    <div className="ticket-meta">
                      <span>${ticket.estado}</span>
                      <span>${ticket.grupo}</span>
                      <span>${ticket.servicio}</span>
                      ${ticket.estado !== "CERRADA"
                        ? html`
                            <button
                              className="secondary-button"
                              onClick=${() => onChangeStatus(ticket.id, "RESUELTA")}
                            >
                              Marcar RESUELTA
                            </button>
                            <button
                              className="secondary-button"
                              onClick=${() => onChangeStatus(ticket.id, "CERRADA")}
                            >
                              Marcar CERRADA
                            </button>
                          `
                        : null}
                    </div>
                  </article>
                `
              )}
        </div>
      </section>
    </section>
  `;
}

function App() {
  const [authLoading, setAuthLoading] = useState(true);
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [user, setUser] = useState(null);
  const [activeView, setActiveView] = useState("dashboard");
  const [groups, setGroups] = useState([]);
  const [incidencias, setIncidencias] = useState([]);
  const [peticiones, setPeticiones] = useState([]);

  async function loadGroups() {
    const data = await apiRequest("/grupos");
    setGroups(data);
  }

  async function loadMe() {
    const current = await apiRequest("/auth/me");
    setUser({
      username: current.username,
      nombre: current.username,
      rol: current.role,
      grupo: ""
    });
  }

  async function loadIncidencias(filters = { estado: "ABIERTA" }) {
    const params = new URLSearchParams({
      page: "0",
      size: "10"
    });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }

    const data = await apiRequest(`/incidencias?${params.toString()}`);
    setIncidencias(data.content || []);
  }

  async function loadPeticiones(filters = { estado: "ABIERTA" }) {
    const params = new URLSearchParams({
      page: "0",
      size: "10"
    });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }

    const data = await apiRequest(`/peticiones?${params.toString()}`);
    setPeticiones(data.content || []);
  }

  async function hydrateSession() {
    const token = localStorage.getItem(TOKEN_KEY);

    if (!token) {
      setAuthLoading(false);
      return;
    }

    try {
      await Promise.all([loadMe(), loadGroups(), loadIncidencias(), loadPeticiones()]);
    } catch (_error) {
      localStorage.removeItem(TOKEN_KEY);
      setUser(null);
    } finally {
      setAuthLoading(false);
    }
  }

  useEffect(() => {
    hydrateSession();
  }, []);

  async function handleLogin(credentials) {
    setLoginLoading(true);
    setLoginError("");

    try {
      const data = await apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify(credentials)
      });

      localStorage.setItem(TOKEN_KEY, data.token);
      await Promise.all([loadMe(), loadGroups(), loadIncidencias(), loadPeticiones()]);
    } catch (requestError) {
      setLoginError(requestError.message);
    } finally {
      setLoginLoading(false);
      setAuthLoading(false);
    }
  }

  function handleLogout() {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
    setIncidencias([]);
    setPeticiones([]);
    setGroups([]);
    setActiveView("dashboard");
  }

  async function updateTicketStatus(type, id, estado) {
    await apiRequest(`/${type}/${id}/estado`, {
      method: "PUT",
      body: JSON.stringify({ estado })
    });

    if (type === "incidencias") {
      await loadIncidencias();
      return;
    }

    await loadPeticiones();
  }

  if (authLoading && !user) {
    return html`<main className="loading-screen"><p>Cargando AstroPI...</p></main>`;
  }

  if (!user) {
    return html`
      <${LoginScreen}
        onLogin=${handleLogin}
        error=${loginError}
        loading=${loginLoading}
      />
    `;
  }

  return html`
    <main className="app-shell">
      <${Header}
        user=${user}
        activeView=${activeView}
        onChangeView=${setActiveView}
        onLogout=${handleLogout}
      />

      ${activeView === "dashboard"
        ? html`<${Dashboard} incidencias=${incidencias} peticiones=${peticiones} />`
        : null}

      ${activeView === "incidencias"
        ? html`
            <${TicketsView}
              title="Incidencias"
              endpoint="incidencias"
              estadoOptions=${["ABIERTA", "EN_PROCESO", "PARADA", "RESUELTA", "CERRADA"]}
              groups=${groups}
              tickets=${incidencias}
              onRefresh=${loadIncidencias}
              onChangeStatus=${(id, estado) => updateTicketStatus("incidencias", id, estado)}
            />
          `
        : null}

      ${activeView === "peticiones"
        ? html`
            <${TicketsView}
              title="Peticiones"
              endpoint="peticiones"
              estadoOptions=${["ABIERTA", "EN_PROCESO", "PARADA", "RESUELTA", "CERRADA"]}
              groups=${groups}
              tickets=${peticiones}
              onRefresh=${loadPeticiones}
              onChangeStatus=${(id, estado) => updateTicketStatus("peticiones", id, estado)}
            />
          `
        : null}
    </main>
  `;
}

createRoot(document.getElementById("root")).render(html`<${App} />`);
