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

function AdminRoute({ user, children }) {
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.rol !== "SUPER_ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

function LoginPage({ onLogin, onRegister, loading, registerLoading, error, registerMessage }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ username: "superadmin", password: "admin123" });
  const [registerForm, setRegisterForm] = useState({
    username: "",
    nombre: "",
    apellidos: "",
    email: "",
    dni: "",
    password: ""
  });

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function updateRegisterField(field, value) {
    setRegisterForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await onLogin(form);
  }

  async function handleRegisterSubmit(event) {
    event.preventDefault();
    await onRegister(registerForm);
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
        <div className="login-form">
          <div className="mode-switch">
            <button
              className={mode === "login" ? "nav-button active" : "nav-button"}
              type="button"
              onClick={() => setMode("login")}
            >
              Iniciar sesion
            </button>
            <button
              className={mode === "register" ? "nav-button active" : "nav-button"}
              type="button"
              onClick={() => setMode("register")}
            >
              Solicitar alta
            </button>
          </div>

          {mode === "login" ? (
            <form className="stack-form" onSubmit={handleSubmit}>
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
          ) : (
            <form className="stack-form" onSubmit={handleRegisterSubmit}>
              <label>
                <span>Username</span>
                <input
                  type="text"
                  value={registerForm.username}
                  onChange={(event) => updateRegisterField("username", event.target.value)}
                  required
                />
              </label>
              <label>
                <span>Nombre</span>
                <input
                  type="text"
                  value={registerForm.nombre}
                  onChange={(event) => updateRegisterField("nombre", event.target.value)}
                  required
                />
              </label>
              <label>
                <span>Apellidos</span>
                <input
                  type="text"
                  value={registerForm.apellidos}
                  onChange={(event) => updateRegisterField("apellidos", event.target.value)}
                  required
                />
              </label>
              <label>
                <span>Email</span>
                <input
                  type="email"
                  value={registerForm.email}
                  onChange={(event) => updateRegisterField("email", event.target.value)}
                />
              </label>
              <label>
                <span>DNI</span>
                <input
                  type="text"
                  value={registerForm.dni}
                  onChange={(event) => updateRegisterField("dni", event.target.value)}
                  required
                />
              </label>
              <label>
                <span>Password propuesta</span>
                <input
                  type="password"
                  value={registerForm.password}
                  onChange={(event) => updateRegisterField("password", event.target.value)}
                  required
                />
              </label>
              {registerMessage ? <p className="form-success">{registerMessage}</p> : null}
              {error ? <p className="form-error">{error}</p> : null}
              <button className="primary-button" type="submit" disabled={registerLoading}>
                {registerLoading ? "Enviando..." : "Enviar solicitud"}
              </button>
            </form>
          )}
        </div>
      </section>
    </main>
  );
}

function AppLayout({ user, onLogout, children }) {
  const location = useLocation();
  const adminLinks = user?.rol === "SUPER_ADMIN" ? [{ to: "/admin", label: "Admin" }] : [];
  const links = [
    { to: "/dashboard", label: "Resumen" },
    { to: "/incidencias", label: "Incidencias" },
    { to: "/peticiones", label: "Peticiones" },
    ...adminLinks
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

function AdminPage({
  usuarios,
  grupos,
  roles,
  permisos,
  onRefresh,
  onCreateGroup,
  onDeleteGroup,
  onCreateUser,
  onDeleteUser
}) {
  const [groupName, setGroupName] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [userForm, setUserForm] = useState({
    username: "",
    nombre: "",
    apellidos: "",
    email: "",
    dni: "",
    password: "",
    rolId: roles[0]?.id || "",
    grupoId: grupos[0]?.id || "",
    activo: true
  });

  useEffect(() => {
    setUserForm((current) => ({
      ...current,
      rolId: current.rolId || roles[0]?.id || "",
      grupoId: current.grupoId || grupos[0]?.id || ""
    }));
  }, [roles, grupos]);

  async function handleCreateGroup(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onCreateGroup(groupName);
      setGroupName("");
      setMessage("Grupo creado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleCreateUser(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onCreateUser({
        ...userForm,
        rolId: Number(userForm.rolId),
        grupoId: Number(userForm.grupoId)
      });
      setUserForm((current) => ({
        ...current,
        username: "",
        nombre: "",
        apellidos: "",
        email: "",
        dni: "",
        password: ""
      }));
      setMessage("Usuario creado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleDeleteGroup(groupId) {
    setMessage("");
    setError("");

    try {
      await onDeleteGroup(groupId);
      setMessage("Grupo borrado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleDeleteUser(userId) {
    setMessage("");
    setError("");

    try {
      await onDeleteUser(userId);
      setMessage("Usuario borrado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <section className="admin-grid">
      <section className="list-panel">
        <div className="section-heading">
          <p className="eyebrow">Administracion</p>
          <h2>Grupos y permisos</h2>
        </div>
        <form className="stack-form" onSubmit={handleCreateGroup}>
          <label>
            <span>Nuevo grupo</span>
            <input value={groupName} onChange={(event) => setGroupName(event.target.value)} required />
          </label>
          <button className="primary-button" type="submit">Crear grupo</button>
        </form>
        <div className="ticket-list">
          {grupos.map((grupo) => (
            <article className="ticket-row" key={`grupo-${grupo.id}`}>
              <div className="ticket-main">
                <strong>{grupo.nombre}</strong>
              </div>
              <div className="ticket-meta">
                <button className="secondary-button" type="button" onClick={() => handleDeleteGroup(grupo.id)}>
                  Borrar grupo
                </button>
              </div>
            </article>
          ))}
        </div>
        <div className="ticket-list">
          {roles.map((rol) => (
            <article className="ticket-row" key={`rol-${rol.id}`}>
              <div className="ticket-main">
                <strong>{rol.nombre}</strong>
                <p>Permisos: {rol.permisos?.length ? rol.permisos.join(", ") : "Sin permisos"}</p>
              </div>
            </article>
          ))}
          {permisos.length ? (
            <article className="ticket-row">
              <div className="ticket-main">
                <strong>Permisos disponibles</strong>
                <p>{permisos.map((permiso) => permiso.nombre).join(", ")}</p>
              </div>
            </article>
          ) : null}
        </div>
      </section>

      <section className="list-panel">
        <div className="section-heading">
          <p className="eyebrow">Administracion</p>
          <h2>Usuarios</h2>
        </div>
        <form className="stack-form" onSubmit={handleCreateUser}>
          <label><span>Username</span><input value={userForm.username} onChange={(event) => setUserForm((current) => ({ ...current, username: event.target.value }))} required /></label>
          <label><span>Nombre</span><input value={userForm.nombre} onChange={(event) => setUserForm((current) => ({ ...current, nombre: event.target.value }))} required /></label>
          <label><span>Apellidos</span><input value={userForm.apellidos} onChange={(event) => setUserForm((current) => ({ ...current, apellidos: event.target.value }))} required /></label>
          <label><span>Email</span><input type="email" value={userForm.email} onChange={(event) => setUserForm((current) => ({ ...current, email: event.target.value }))} /></label>
          <label><span>DNI</span><input value={userForm.dni} onChange={(event) => setUserForm((current) => ({ ...current, dni: event.target.value }))} required /></label>
          <label><span>Password</span><input type="password" value={userForm.password} onChange={(event) => setUserForm((current) => ({ ...current, password: event.target.value }))} required /></label>
          <label>
            <span>Rol</span>
            <select value={userForm.rolId} onChange={(event) => setUserForm((current) => ({ ...current, rolId: event.target.value }))} required>
              {roles.map((rol) => <option key={rol.id} value={rol.id}>{rol.nombre}</option>)}
            </select>
          </label>
          <label>
            <span>Grupo</span>
            <select value={userForm.grupoId} onChange={(event) => setUserForm((current) => ({ ...current, grupoId: event.target.value }))} required>
              {grupos.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.nombre}</option>)}
            </select>
          </label>
          <label>
            <span>Activo</span>
            <select value={String(userForm.activo)} onChange={(event) => setUserForm((current) => ({ ...current, activo: event.target.value === "true" }))}>
              <option value="true">Activo</option>
              <option value="false">Inactivo</option>
            </select>
          </label>
          <button className="primary-button" type="submit">Crear usuario</button>
        </form>

        {message ? <p className="form-success">{message}</p> : null}
        {error ? <p className="form-error">{error}</p> : null}

        <div className="ticket-list">
          {usuarios.map((usuario) => (
            <article className="ticket-row" key={`usuario-${usuario.id}`}>
              <div className="ticket-main">
                <strong>{usuario.username}</strong>
                <p>{usuario.nombre} {usuario.apellidos}</p>
                <p>{usuario.rol} | {usuario.grupo}</p>
                <p>Permisos: {usuario.permisos?.length ? usuario.permisos.join(", ") : "Sin permisos"}</p>
              </div>
              <div className="ticket-meta">
                <button className="secondary-button" type="button" onClick={() => handleDeleteUser(usuario.id)}>
                  Borrar usuario
                </button>
              </div>
            </article>
          ))}
        </div>
        <button className="secondary-button" type="button" onClick={onRefresh}>Recargar admin</button>
      </section>
    </section>
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

function TicketCard({
  ticket,
  currentUsername,
  assignables,
  onAssign,
  onChangeStatus,
  onAddComment,
  onUpdateComment,
  onDeleteComment
}) {
  const [assignedUsername, setAssignedUsername] = useState(ticket.usuarioAsignado || "");
  const [comment, setComment] = useState("");
  const [actionError, setActionError] = useState("");
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingContent, setEditingContent] = useState("");

  useEffect(() => {
    setAssignedUsername(ticket.usuarioAsignado || "");
  }, [ticket.usuarioAsignado]);

  async function handleAssign() {
    setActionError("");

    try {
      await onAssign(ticket.id, assignedUsername);
    } catch (requestError) {
      setActionError(requestError.message);
    }
  }

  async function handleCommentSubmit(event) {
    event.preventDefault();
    setActionError("");

    try {
      await onAddComment(ticket.id, comment);
      setComment("");
    } catch (requestError) {
      setActionError(requestError.message);
    }
  }

  async function handleUpdateComment(event) {
    event.preventDefault();
    setActionError("");

    try {
      await onUpdateComment(ticket.id, editingCommentId, editingContent);
      setEditingCommentId(null);
      setEditingContent("");
    } catch (requestError) {
      setActionError(requestError.message);
    }
  }

  async function handleDeleteComment(commentId) {
    setActionError("");

    try {
      await onDeleteComment(ticket.id, commentId);
    } catch (requestError) {
      setActionError(requestError.message);
    }
  }

  return (
    <article className="ticket-row">
      <div className="ticket-main">
        <strong>{ticket.codigoTicket}</strong>
        <h3>{ticket.titulo}</h3>
        <p>{ticket.descripcion}</p>
        <div className="ticket-badges">
          <span>{ticket.estado}</span>
          <span>{ticket.grupo}</span>
          <span>{ticket.servicio}</span>
        </div>

        <section className="ticket-comments">
          <h4>Comentarios</h4>
          {ticket.comentarios?.length ? (
            <div className="comment-list">
              {ticket.comentarios.map((comentario) => (
                <article className="comment-item" key={comentario.id}>
                  <p className="comment-meta">
                    <strong>{comentario.autor}</strong> | {new Date(comentario.fechaCreacion).toLocaleString()}
                  </p>
                  {editingCommentId === comentario.id ? (
                    <form className="comment-form" onSubmit={handleUpdateComment}>
                      <textarea
                        value={editingContent}
                        onChange={(event) => setEditingContent(event.target.value)}
                        required
                      />
                      <div className="comment-actions">
                        <button className="secondary-button" type="submit">
                          Guardar
                        </button>
                        <button
                          className="secondary-button"
                          type="button"
                          onClick={() => {
                            setEditingCommentId(null);
                            setEditingContent("");
                          }}
                        >
                          Cancelar
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <p>{comentario.contenido}</p>
                      {comentario.autor === currentUsername ? (
                        <div className="comment-actions">
                          <button
                            className="secondary-button"
                            type="button"
                            onClick={() => {
                              setEditingCommentId(comentario.id);
                              setEditingContent(comentario.contenido);
                            }}
                          >
                            Editar
                          </button>
                          <button
                            className="secondary-button"
                            type="button"
                            onClick={() => handleDeleteComment(comentario.id)}
                          >
                            Borrar
                          </button>
                        </div>
                      ) : null}
                    </>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <p className="empty-inline">Sin comentarios todavia.</p>
          )}

          <form className="comment-form" onSubmit={handleCommentSubmit}>
            <textarea
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              placeholder="Escribe una actualizacion del ticket"
              required
            />
            <button className="secondary-button" type="submit">
              Anadir comentario
            </button>
          </form>
        </section>
      </div>

      <div className="ticket-meta">
        <span>Creador: {ticket.usuario || "-"}</span>
        <span>Asignado: {ticket.usuarioAsignado || "Sin asignar"}</span>
        <select value={assignedUsername} onChange={(event) => setAssignedUsername(event.target.value)}>
          <option value="">Sin asignar</option>
          {assignables.map((assignable) => (
            <option key={assignable.id} value={assignable.username}>
              {assignable.username}
            </option>
          ))}
        </select>
        <button className="secondary-button" type="button" onClick={handleAssign}>
          Guardar asignacion
        </button>
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
        {actionError ? <p className="form-error">{actionError}</p> : null}
      </div>
    </article>
  );
}

function TicketsPage({
  title,
  endpoint,
  currentUsername,
  groups,
  tickets,
  assignables,
  onRefresh,
  onChangeStatus,
  onAssign,
  onAddComment,
  onUpdateComment,
  onDeleteComment
}) {
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
    const nextFilters = { ...filters, estado };
    setFilters(nextFilters);
    await onRefresh(nextFilters);
  }

  async function handleGroupFilterChange(grupoId) {
    const nextFilters = { ...filters, grupoId };
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
            <select
              value={filters.grupoId || ""}
              onChange={(event) => handleGroupFilterChange(event.target.value)}
            >
              <option value="">Todos los grupos</option>
              {groups.map((group) => (
                <option key={group.id} value={group.id}>
                  {group.nombre}
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
              <TicketCard
                key={`${endpoint}-${ticket.id}`}
                ticket={ticket}
                currentUsername={currentUsername}
                assignables={assignables}
                onAssign={onAssign}
                onChangeStatus={onChangeStatus}
                onAddComment={onAddComment}
                onUpdateComment={onUpdateComment}
                onDeleteComment={onDeleteComment}
              />
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
  const [registerLoading, setRegisterLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [registerMessage, setRegisterMessage] = useState("");
  const [user, setUser] = useState(null);
  const [groups, setGroups] = useState([]);
  const [incidencias, setIncidencias] = useState([]);
  const [peticiones, setPeticiones] = useState([]);
  const [incidenciaAssignables, setIncidenciaAssignables] = useState([]);
  const [peticionAssignables, setPeticionAssignables] = useState([]);
  const [adminUsers, setAdminUsers] = useState([]);
  const [adminRoles, setAdminRoles] = useState([]);
  const [adminPermisos, setAdminPermisos] = useState([]);
  const [adminGroups, setAdminGroups] = useState([]);

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
    if (filters.grupoId) {
      params.set("grupoId", filters.grupoId);
    }

    const data = await apiRequest(`/incidencias?${params.toString()}`);
    setIncidencias(data.content || []);
  }

  async function loadIncidenciaAssignables() {
    const data = await apiRequest("/incidencias/asignables");
    setIncidenciaAssignables(data);
  }

  async function loadPeticiones(filters = { estado: "ABIERTA" }) {
    const params = new URLSearchParams({ page: "0", size: "10" });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }
    if (filters.grupoId) {
      params.set("grupoId", filters.grupoId);
    }

    const data = await apiRequest(`/peticiones?${params.toString()}`);
    setPeticiones(data.content || []);
  }

  async function loadPeticionAssignables() {
    const data = await apiRequest("/peticiones/asignables");
    setPeticionAssignables(data);
  }

  async function loadAdminData() {
    const [usuarios, gruposAdmin, roles, permisos] = await Promise.all([
      apiRequest("/admin/usuarios"),
      apiRequest("/admin/grupos"),
      apiRequest("/admin/roles"),
      apiRequest("/admin/permisos")
    ]);

    setAdminUsers(usuarios);
    setAdminGroups(gruposAdmin);
    setAdminRoles(roles);
    setAdminPermisos(permisos);
  }

  async function hydrateSession() {
    const token = localStorage.getItem(TOKEN_KEY);

    if (!token) {
      setAuthLoading(false);
      return;
    }

    try {
      const currentUser = await apiRequest("/auth/me");
      setUser(currentUser);

      await Promise.all([
        loadGroups(),
        loadIncidencias(),
        loadPeticiones(),
        loadIncidenciaAssignables(),
        loadPeticionAssignables()
      ]);

      if (currentUser.rol === "SUPER_ADMIN") {
        await loadAdminData();
      }
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
      const currentUser = await apiRequest("/auth/me");
      setUser(currentUser);

      await Promise.all([
        loadGroups(),
        loadIncidencias(),
        loadPeticiones(),
        loadIncidenciaAssignables(),
        loadPeticionAssignables()
      ]);

      if (currentUser.rol === "SUPER_ADMIN") {
        await loadAdminData();
      }
      navigate("/dashboard", { replace: true });
    } catch (requestError) {
      setLoginError(requestError.message);
    } finally {
      setLoginLoading(false);
      setAuthLoading(false);
    }
  }

  async function handleRegister(request) {
    setRegisterLoading(true);
    setLoginError("");
    setRegisterMessage("");

    try {
      const data = await apiRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify(request)
      });
      setRegisterMessage(data.mensaje);
    } catch (requestError) {
      setLoginError(requestError.message);
    } finally {
      setRegisterLoading(false);
    }
  }

  function handleLogout() {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
    setGroups([]);
    setIncidencias([]);
    setPeticiones([]);
    setIncidenciaAssignables([]);
    setPeticionAssignables([]);
    setAdminUsers([]);
    setAdminGroups([]);
    setAdminRoles([]);
    setAdminPermisos([]);
    navigate("/login", { replace: true });
  }

  async function createAdminGroup(nombre) {
    await apiRequest("/admin/grupos", {
      method: "POST",
      body: JSON.stringify({ nombre })
    });
    await Promise.all([loadGroups(), loadAdminData()]);
  }

  async function deleteAdminGroup(groupId) {
    await apiRequest(`/admin/grupos/${groupId}`, { method: "DELETE" });
    await Promise.all([loadGroups(), loadAdminData()]);
  }

  async function createAdminUser(payload) {
    await apiRequest("/admin/usuarios", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    await loadAdminData();
  }

  async function deleteAdminUser(userId) {
    await apiRequest(`/admin/usuarios/${userId}`, { method: "DELETE" });
    await loadAdminData();
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

  async function assignTicket(type, id, usernameAsignado) {
    await apiRequest(`/${type}/${id}/asignacion`, {
      method: "PUT",
      body: JSON.stringify({ usernameAsignado })
    });

    if (type === "incidencias") {
      await loadIncidencias();
      return;
    }

    await loadPeticiones();
  }

  async function addTicketComment(type, id, contenido) {
    await apiRequest(`/${type}/${id}/comentarios`, {
      method: "POST",
      body: JSON.stringify({ contenido })
    });

    if (type === "incidencias") {
      await loadIncidencias();
      return;
    }

    await loadPeticiones();
  }

  async function updateTicketComment(type, id, commentId, contenido) {
    await apiRequest(`/${type}/${id}/comentarios/${commentId}`, {
      method: "PUT",
      body: JSON.stringify({ contenido })
    });

    if (type === "incidencias") {
      await loadIncidencias();
      return;
    }

    await loadPeticiones();
  }

  async function deleteTicketComment(type, id, commentId) {
    await apiRequest(`/${type}/${id}/comentarios/${commentId}`, {
      method: "DELETE"
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
        element={
          <LoginPage
            onLogin={handleLogin}
            onRegister={handleRegister}
            loading={loginLoading}
            registerLoading={registerLoading}
            error={loginError}
            registerMessage={registerMessage}
          />
        }
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
        path="/admin"
        element={
          <AdminRoute user={user}>
            <AppLayout user={user} onLogout={handleLogout}>
              <AdminPage
                usuarios={adminUsers}
                grupos={adminGroups}
                roles={adminRoles}
                permisos={adminPermisos}
                onRefresh={loadAdminData}
                onCreateGroup={createAdminGroup}
                onDeleteGroup={deleteAdminGroup}
                onCreateUser={createAdminUser}
                onDeleteUser={deleteAdminUser}
              />
            </AppLayout>
          </AdminRoute>
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
                currentUsername={user?.username || ""}
                groups={groups}
                tickets={incidencias}
                assignables={incidenciaAssignables}
                onRefresh={loadIncidencias}
                onChangeStatus={(id, estado) => updateTicketStatus("incidencias", id, estado)}
                onAssign={(id, usernameAsignado) => assignTicket("incidencias", id, usernameAsignado)}
                onAddComment={(id, contenido) => addTicketComment("incidencias", id, contenido)}
                onUpdateComment={(id, commentId, contenido) =>
                  updateTicketComment("incidencias", id, commentId, contenido)
                }
                onDeleteComment={(id, commentId) => deleteTicketComment("incidencias", id, commentId)}
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
                currentUsername={user?.username || ""}
                groups={groups}
                tickets={peticiones}
                assignables={peticionAssignables}
                onRefresh={loadPeticiones}
                onChangeStatus={(id, estado) => updateTicketStatus("peticiones", id, estado)}
                onAssign={(id, usernameAsignado) => assignTicket("peticiones", id, usernameAsignado)}
                onAddComment={(id, contenido) => addTicketComment("peticiones", id, contenido)}
                onUpdateComment={(id, commentId, contenido) =>
                  updateTicketComment("peticiones", id, commentId, contenido)
                }
                onDeleteComment={(id, commentId) => deleteTicketComment("peticiones", id, commentId)}
              />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}
