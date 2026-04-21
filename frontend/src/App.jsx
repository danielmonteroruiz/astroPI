import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";

const TOKEN_KEY = "astropi_token";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const ESTADOS_TICKET = ["ABIERTA", "EN_PROCESO", "PARADA", "RESUELTA", "CERRADA"];
const CATALOGOS_TICKETS = {
  incidencias: {
    Autenticacion: ["Error de login", "Recuperacion de acceso", "Bloqueo de cuenta"],
    Infraestructura: ["Red", "Servidor", "VPN"],
    Hardware: ["Portatil", "Monitor", "Perifericos"],
    Software: ["Instalacion fallida", "Error de aplicacion", "Actualizacion"]
  },
  peticiones: {
    Accesos: ["Alta de permisos", "Cambio de permisos", "Baja de permisos"],
    Software: ["Nueva licencia", "Instalacion de aplicacion", "Actualizacion programada"],
    Hardware: ["Solicitud de portatil", "Solicitud de monitor", "Solicitud de perifericos"],
    Cuentas: ["Alta de usuario", "Cambio de datos", "Restablecimiento de password"]
  }
};

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

function ProtectedRoute({ user, children }) {
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

function LoginPage({ onLogin, loading, error }) {
  const [form, setForm] = useState({ username: "superadmin", password: "admin123" });

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await onLogin(form);
  }

  return (
    <main className="login-shell">
      <section className="login-hero">
        <div className="login-copy">
          <p className="eyebrow">AstroPI</p>
          <h1>Gestiona incidencias y peticiones desde una interfaz real.</h1>
          <p className="login-text">
            Frontend React conectado con Spring Boot, JWT y PostgreSQL.
          </p>
        </div>
        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            <span>Username</span>
            <input
              type="text"
              value={form.username}
              onChange={(event) => updateField("username", event.target.value)}
              required
            />
          </label>
          <label>
            <span>Password</span>
            <input
              type="password"
              value={form.password}
              onChange={(event) => updateField("password", event.target.value)}
              required
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={loading}>
            {loading ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </section>
    </main>
  );
}

function AppLayout({ user, onLogout, children }) {
  const location = useLocation();
  const links = [
    { to: "/dashboard", label: "Resumen" },
    { to: "/incidencias", label: "Incidencias" },
    { to: "/peticiones", label: "Peticiones" }
  ];

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">AstroPI</p>
          <h1>{user.nombre || user.username}</h1>
          <p className="header-meta">
            {user.rol} | {user.grupo || "Sin grupo"}
          </p>
        </div>
        <nav className="main-nav">
          {links.map((link) => (
            <Link
              key={link.to}
              className={location.pathname === link.to ? "nav-button active" : "nav-button"}
              to={link.to}
            >
              {link.label}
            </Link>
          ))}
        </nav>
        <button className="secondary-button" onClick={onLogout}>
          Salir
        </button>
      </header>
      {children}
    </main>
  );
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

  return (
    <section className="dashboard-grid">
      {cards.map((card) => (
        <article className="metric-tile" key={card.title}>
          <img src={card.image} alt={card.title} />
          <div className="metric-copy">
            <p>{card.title}</p>
            <strong>{card.value}</strong>
            <span>{card.subtitle}</span>
          </div>
        </article>
      ))}
    </section>
  );
}

function TicketsPage({ title, endpoint, groups, tickets, onRefresh, onChangeStatus }) {
  const singularTitle = endpoint === "incidencias" ? "incidencia" : "peticion";
  const catalogo = CATALOGOS_TICKETS[endpoint];
  const servicios = Object.keys(catalogo);

  const initialForm = useMemo(
    () => ({
      titulo: "",
      descripcion: "",
      servicio: servicios[0],
      categoria: catalogo[servicios[0]][0],
      grupoId: groups[0]?.id || ""
    }),
    [catalogo, groups, servicios]
  );

  const [form, setForm] = useState(initialForm);
  const [filters, setFilters] = useState({ estado: "ABIERTA" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setForm((current) => ({
      ...current,
      grupoId: current.grupoId || groups[0]?.id || ""
    }));
  }, [groups]);

  useEffect(() => {
    setForm(initialForm);
  }, [initialForm]);

  function updateField(field, value) {
    setForm((current) => {
      if (field === "servicio") {
        const categorias = catalogo[value] || [];
        return {
          ...current,
          servicio: value,
          categoria: categorias[0] || ""
        };
      }

      return { ...current, [field]: value };
    });
  }

  async function handleCreate(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await apiRequest(`/${endpoint}`, {
        method: "POST",
        body: JSON.stringify({
          ...form,
          grupoId: Number(form.grupoId)
        })
      });

      setForm({
        ...initialForm,
        grupoId: groups[0]?.id || ""
      });
      setMessage(`${singularTitle} creada correctamente`);
      await onRefresh(filters);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleFilterChange(estado) {
    const nextFilters = { estado };
    setFilters(nextFilters);
    await onRefresh(nextFilters);
  }

  return (
    <section className="content-grid">
      <form className="editor-panel" onSubmit={handleCreate}>
        <div className="section-heading">
          <p className="eyebrow">{title}</p>
          <h2>Crear {singularTitle}</h2>
        </div>
        <label>
          <span>Titulo</span>
          <input
            type="text"
            value={form.titulo}
            onChange={(event) => updateField("titulo", event.target.value)}
            required
          />
        </label>
        <label>
          <span>Descripcion</span>
          <textarea
            value={form.descripcion}
            onChange={(event) => updateField("descripcion", event.target.value)}
            required
          />
        </label>
        <div className="field-row">
          <label>
            <span>Servicio</span>
            <select
              value={form.servicio}
              onChange={(event) => updateField("servicio", event.target.value)}
              required
            >
              {servicios.map((servicio) => (
                <option key={servicio} value={servicio}>
                  {servicio}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>Categoria</span>
            <select
              value={form.categoria}
              onChange={(event) => updateField("categoria", event.target.value)}
              required
            >
              {(catalogo[form.servicio] || []).map((categoria) => (
                <option key={categoria} value={categoria}>
                  {categoria}
                </option>
              ))}
            </select>
          </label>
        </div>
        <label>
          <span>Grupo</span>
          <select
            value={form.grupoId}
            onChange={(event) => updateField("grupoId", event.target.value)}
            required
          >
            <option value="" disabled>
              Selecciona un grupo
            </option>
            {groups.map((group) => (
              <option key={group.id} value={group.id}>
                {group.nombre}
              </option>
            ))}
          </select>
        </label>
        {message ? <p className="form-success">{message}</p> : null}
        {error ? <p className="form-error">{error}</p> : null}
        <button className="primary-button" type="submit">
          Guardar
        </button>
      </form>

      <section className="list-panel">
        <div className="section-heading section-heading-inline">
          <div>
            <p className="eyebrow">Listado</p>
            <h2>{title}</h2>
          </div>
          <div className="filter-strip">
            <select
              value={filters.estado}
              onChange={(event) => handleFilterChange(event.target.value)}
            >
              {ESTADOS_TICKET.map((estado) => (
                <option key={estado} value={estado}>
                  {estado}
                </option>
              ))}
            </select>
            <button className="secondary-button" type="button" onClick={() => onRefresh(filters)}>
              Recargar
            </button>
          </div>
        </div>

        <div className="ticket-list">
          {tickets.length === 0 ? (
            <p className="empty-state">No hay resultados para este filtro.</p>
          ) : (
            tickets.map((ticket) => (
              <article className="ticket-row" key={`${endpoint}-${ticket.id}`}>
                <div>
                  <strong>{ticket.codigoTicket}</strong>
                  <h3>{ticket.titulo}</h3>
                  <p>{ticket.descripcion}</p>
                </div>
                <div className="ticket-meta">
                  <span>{ticket.estado}</span>
                  <span>{ticket.grupo}</span>
                  <span>{ticket.servicio}</span>
                  {ticket.estado !== "CERRADA" ? (
                    <>
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => onChangeStatus(ticket.id, "RESUELTA")}
                      >
                        Marcar RESUELTA
                      </button>
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => onChangeStatus(ticket.id, "CERRADA")}
                      >
                        Marcar CERRADA
                      </button>
                    </>
                  ) : null}
                </div>
              </article>
            ))
          )}
        </div>
      </section>
    </section>
  );
}

export default function App() {
  const navigate = useNavigate();
  const [authLoading, setAuthLoading] = useState(true);
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [user, setUser] = useState(null);
  const [groups, setGroups] = useState([]);
  const [incidencias, setIncidencias] = useState([]);
  const [peticiones, setPeticiones] = useState([]);

  async function loadGroups() {
    const data = await apiRequest("/grupos");
    setGroups(data);
  }

  async function loadMe() {
    const current = await apiRequest("/auth/me");
    setUser(current);
  }

  async function loadIncidencias(filters = { estado: "ABIERTA" }) {
    const params = new URLSearchParams({ page: "0", size: "10" });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }

    const data = await apiRequest(`/incidencias?${params.toString()}`);
    setIncidencias(data.content || []);
  }

  async function loadPeticiones(filters = { estado: "ABIERTA" }) {
    const params = new URLSearchParams({ page: "0", size: "10" });

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
      navigate("/dashboard", { replace: true });
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
    setGroups([]);
    setIncidencias([]);
    setPeticiones([]);
    navigate("/login", { replace: true });
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
    return (
      <main className="loading-screen">
        <p>Cargando AstroPI...</p>
      </main>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage onLogin={handleLogin} loading={loginLoading} error={loginError} />}
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute user={user}>
            <AppLayout user={user} onLogout={handleLogout}>
              <Dashboard incidencias={incidencias} peticiones={peticiones} />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/incidencias"
        element={
          <ProtectedRoute user={user}>
            <AppLayout user={user} onLogout={handleLogout}>
              <TicketsPage
                title="Incidencias"
                endpoint="incidencias"
                groups={groups}
                tickets={incidencias}
                onRefresh={loadIncidencias}
                onChangeStatus={(id, estado) => updateTicketStatus("incidencias", id, estado)}
              />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/peticiones"
        element={
          <ProtectedRoute user={user}>
            <AppLayout user={user} onLogout={handleLogout}>
              <TicketsPage
                title="Peticiones"
                endpoint="peticiones"
                groups={groups}
                tickets={peticiones}
                onRefresh={loadPeticiones}
                onChangeStatus={(id, estado) => updateTicketStatus("peticiones", id, estado)}
              />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}
