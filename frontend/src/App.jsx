import { useEffect, useMemo, useRef, useState } from "react";
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";

const TOKEN_KEY = "astropi_token";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const ESTADOS_TICKET = ["ABIERTA", "EN_PROCESO", "PARADA", "RESUELTA", "CERRADA"];
const ESTADOS_GESTION = ["ABIERTA", "EN_PROCESO", "CERRADA"];
const DEFAULT_TICKET_FILTERS = {
  estado: "",
  grupoId: "",
  fechaDesde: "",
  fechaHasta: ""
};
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

function formatTicketDate(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

function getStatusColor(estado) {
  if (estado === "ABIERTA") {
    return "status-dot open";
  }

  if (estado === "EN_PROCESO") {
    return "status-dot in-progress";
  }

  if (estado === "CERRADA") {
    return "status-dot closed";
  }

  return "status-dot neutral";
}

function TrashIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="icon-svg">
      <path
        d="M4 7h16M9 7V5h6v2m-9 0 1 12h10l1-12"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function EditIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="icon-svg">
      <path
        d="M4 20h4l10-10-4-4L4 16v4Zm9-13 4 4"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function PasswordIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="icon-svg">
      <path
        d="M7 8 3 12l4 4M17 8l4 4-4 4M14 5l-4 14"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="icon-svg">
      <path
        d="M12 5v14M5 12h14"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function parseUserRequestDescription(descripcion) {
  const lines = (descripcion || "").split("\n");
  const data = {
    username: "",
    nombre: "",
    apellidos: "",
    email: "",
    dni: ""
  };

  lines.forEach((line) => {
    const [label, ...rest] = line.split(":");
    const value = rest.join(":").trim();
    const normalizedLabel = (label || "").trim().toLowerCase();

    if (normalizedLabel === "username") {
      data.username = value;
    } else if (normalizedLabel === "nombre") {
      data.nombre = value;
    } else if (normalizedLabel === "apellidos") {
      data.apellidos = value;
    } else if (normalizedLabel === "email") {
      data.email = value;
    } else if (normalizedLabel === "dni") {
      data.dni = value;
    }
  });

  return data;
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

function LoginPage({
  onLogin,
  onRegister,
  onForgotPassword,
  loading,
  registerLoading,
  forgotPasswordLoading,
  error,
  registerMessage,
  forgotPasswordMessage
}) {
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
  const [forgotPasswordForm, setForgotPasswordForm] = useState({
    username: "",
    email: ""
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

  async function handleForgotPasswordSubmit(event) {
    event.preventDefault();
    await onForgotPassword(forgotPasswordForm);
  }

  return (
    <main className="login-shell">
      <section className="login-hero">
        <div className="login-copy">
          <img className="brand-logo brand-logo-login" src="/astropi-logo.png" alt="AstroPI" />
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
            <button
              className={mode === "forgot" ? "nav-button active" : "nav-button"}
              type="button"
              onClick={() => setMode("forgot")}
            >
              He olvidado mi contrasena
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
          ) : mode === "register" ? (
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
          ) : (
            <form className="stack-form" onSubmit={handleForgotPasswordSubmit}>
              <label>
                <span>Username</span>
                <input
                  type="text"
                  value={forgotPasswordForm.username}
                  onChange={(event) =>
                    setForgotPasswordForm((current) => ({ ...current, username: event.target.value }))
                  }
                  required
                />
              </label>
              <label>
                <span>Email de contacto</span>
                <input
                  type="email"
                  value={forgotPasswordForm.email}
                  onChange={(event) =>
                    setForgotPasswordForm((current) => ({ ...current, email: event.target.value }))
                  }
                />
              </label>
              {forgotPasswordMessage ? <p className="form-success">{forgotPasswordMessage}</p> : null}
              {error ? <p className="form-error">{error}</p> : null}
              <button className="primary-button" type="submit" disabled={forgotPasswordLoading}>
                {forgotPasswordLoading ? "Enviando..." : "Solicitar recuperacion"}
              </button>
            </form>
          )}
        </div>
      </section>
      <footer className="app-footer login-footer">
        <p>Copyright © 2026 Daniel Montero. Todos los derechos reservados.</p>
      </footer>
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
        <div className="app-brand">
          <img className="brand-logo brand-logo-header" src="/astropi-logo.png" alt="AstroPI" />
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
      <footer className="app-footer">
        <p>Copyright © 2026 Daniel Montero. Todos los derechos reservados.</p>
      </footer>
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
  onUpdateGroup,
  onDeleteGroup,
  onCreateUser,
  onDeleteUser,
  onUpdateUser,
  onChangeUserPassword,
  onAssignRolePermission,
  onRemoveRolePermission,
  onUpdateUserGroup,
  onUpdateUserActive,
  onCreatePermission,
  onUpdatePermission,
  onDeletePermission
}) {
  const [groupName, setGroupName] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [editingGroupId, setEditingGroupId] = useState(null);
  const [editingGroupName, setEditingGroupName] = useState("");
  const [editingUserId, setEditingUserId] = useState(null);
  const [editingUserForm, setEditingUserForm] = useState(null);
  const [expandedUserId, setExpandedUserId] = useState(null);
  const [passwordForm, setPasswordForm] = useState({ userId: null, password: "" });
  const [rolePermissionSelection, setRolePermissionSelection] = useState({});
  const [userSearch, setUserSearch] = useState("");
  const [userPage, setUserPage] = useState(0);
  const [showPermissionModal, setShowPermissionModal] = useState(false);
  const [editingPermissionId, setEditingPermissionId] = useState(null);
  const [permissionForm, setPermissionForm] = useState({
    nombre: "",
    opciones: []
  });
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
  const permissionOptions = [
    "Ver tickets de grupo",
    "Gestionar tickets",
    "Cambiar estado de tickets",
    "Asignar tickets",
    "Gestionar comentarios",
    "Crear usuarios",
    "Resetear passwords"
  ];
  const filteredUsers = usuarios.filter((usuario) => {
    const term = userSearch.trim().toLowerCase();

    if (!term) {
      return true;
    }

    return (
      usuario.username.toLowerCase().includes(term) ||
      (usuario.email || "").toLowerCase().includes(term)
    );
  });
  const pagedUsers = filteredUsers.slice(userPage * 10, userPage * 10 + 10);
  const totalUserPages = Math.max(1, Math.ceil(filteredUsers.length / 10));

  useEffect(() => {
    setUserForm((current) => ({
      ...current,
      rolId: current.rolId || roles[0]?.id || "",
      grupoId: current.grupoId || grupos[0]?.id || ""
    }));
  }, [roles, grupos]);

  useEffect(() => {
    setUserPage(0);
  }, [userSearch]);

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

  async function handleUpdateGroup(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onUpdateGroup(editingGroupId, editingGroupName);
      setEditingGroupId(null);
      setEditingGroupName("");
      setMessage("Grupo actualizado correctamente");
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

  async function handleUpdateUser(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onUpdateUser(editingUserId, editingUserForm);
      await onUpdateUserGroup(editingUserId, Number(editingUserForm.grupoId));
      await onUpdateUserActive(editingUserId, editingUserForm.activo);
      setEditingUserId(null);
      setEditingUserForm(null);
      setMessage("Usuario actualizado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleChangePassword(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onChangeUserPassword(passwordForm.userId, passwordForm.password);
      setPasswordForm({ userId: null, password: "" });
      setMessage("Password actualizada correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleAssignRolePermission(roleId) {
    const permisoId = rolePermissionSelection[roleId];

    if (!permisoId) {
      return;
    }

    setMessage("");
    setError("");

    try {
      await onAssignRolePermission(roleId, Number(permisoId));
      setRolePermissionSelection((current) => ({ ...current, [roleId]: "" }));
      setMessage("Permiso asignado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleCreatePermission(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      const descripcion = permissionForm.opciones.length
        ? `Opciones: ${permissionForm.opciones.join(", ")}`
        : null;
      if (editingPermissionId) {
        await onUpdatePermission(editingPermissionId, permissionForm.nombre, descripcion);
      } else {
        await onCreatePermission(permissionForm.nombre, descripcion);
      }
      setPermissionForm({ nombre: "", opciones: [] });
      setEditingPermissionId(null);
      setShowPermissionModal(false);
      setMessage(editingPermissionId ? "Permiso actualizado correctamente" : "Permiso creado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleRemoveRolePermission(roleId, permisoId) {
    setMessage("");
    setError("");

    try {
      await onRemoveRolePermission(roleId, permisoId);
      setMessage("Permiso quitado correctamente");
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
        <form className="admin-group-form" onSubmit={handleCreateGroup}>
          <label className="admin-group-input">
            <span>Nuevo grupo</span>
            <input value={groupName} onChange={(event) => setGroupName(event.target.value)} required />
          </label>
          <button className="primary-button compact-button" type="submit">Crear grupo</button>
        </form>
        <div className="ticket-list">
          {grupos.map((grupo) => (
            <article className="admin-row admin-row-compact" key={`grupo-${grupo.id}`}>
              <div className="ticket-main admin-row-main">
                {editingGroupId === grupo.id ? (
                  <form className="admin-inline-form" onSubmit={handleUpdateGroup}>
                    <input
                      value={editingGroupName}
                      onChange={(event) => setEditingGroupName(event.target.value)}
                      required
                    />
                    <div className="comment-actions">
                      <button className="secondary-button compact-button" type="submit">Guardar</button>
                      <button
                        className="secondary-button compact-button"
                        type="button"
                        onClick={() => {
                          setEditingGroupId(null);
                          setEditingGroupName("");
                        }}
                      >
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : (
                  <>
                    <strong>{grupo.nombre}</strong>
                    <p>Edicion de nombre disponible. Los permisos se gestionan por rol.</p>
                  </>
                )}
              </div>
              <div className="admin-actions-row">
                <button
                  className="secondary-button icon-button"
                  type="button"
                  title="Editar grupo"
                  onClick={() => {
                    setEditingGroupId(grupo.id);
                    setEditingGroupName(grupo.nombre);
                  }}
                >
                  <EditIcon />
                </button>
                <button
                  className="secondary-button icon-button"
                  type="button"
                  title="Borrar grupo"
                  onClick={() => handleDeleteGroup(grupo.id)}
                >
                  <TrashIcon />
                </button>
              </div>
            </article>
          ))}
        </div>
        <div className="ticket-list">
          {roles.map((rol) => (
            <article className="admin-row" key={`rol-${rol.id}`}>
              <div className="ticket-main admin-row-main">
                <strong>{rol.nombre}</strong>
                <div className="admin-permission-list">
                  {rol.permisos?.length ? (
                    rol.permisos.map((permisoNombre) => {
                      const permiso = permisos.find((item) => item.nombre === permisoNombre);

                      return (
                        <span className="admin-permission-pill" key={`${rol.id}-${permisoNombre}`}>
                          {permisoNombre}
                          {permiso ? (
                            <button
                              className="icon-button small-icon-button"
                              type="button"
                              title="Quitar permiso"
                              onClick={() => handleRemoveRolePermission(rol.id, permiso.id)}
                            >
                              <TrashIcon />
                            </button>
                          ) : null}
                        </span>
                      );
                    })
                  ) : (
                    <p>Sin permisos asignados</p>
                  )}
                </div>
                <div className="admin-role-permission-form">
                  <label className="admin-group-input">
                    <span>Añadir permiso</span>
                    <select
                      value={rolePermissionSelection[rol.id] || ""}
                      onChange={(event) => setRolePermissionSelection((current) => ({
                        ...current,
                        [rol.id]: event.target.value
                      }))}
                    >
                      <option value="">Selecciona un permiso</option>
                      {permisos
                        .filter((permiso) => !(rol.permisos || []).includes(permiso.nombre))
                        .map((permiso) => (
                          <option key={`${rol.id}-${permiso.id}`} value={permiso.id}>{permiso.nombre}</option>
                        ))}
                    </select>
                  </label>
                  <button
                    className="secondary-button compact-button content-button"
                    type="button"
                    onClick={() => handleAssignRolePermission(rol.id)}
                  >
                    Añadir
                  </button>
                </div>
              </div>
            </article>
          ))}
          {permisos.length ? (
            <article className="admin-row">
              <div className="ticket-main admin-row-main">
                <strong>Permisos disponibles</strong>
                <div className="admin-permission-list">
                  {permisos.map((permiso) => (
                    <span className="admin-permission-pill" key={`permiso-${permiso.id}`}>
                      {permiso.nombre}
                      <button
                        className="icon-button small-icon-button"
                        type="button"
                        title="Editar permiso"
                        onClick={() => {
                          setEditingPermissionId(permiso.id);
                          setPermissionForm({
                            nombre: permiso.nombre,
                            opciones: (permiso.descripcion || "")
                              .replace("Opciones:", "")
                              .split(",")
                              .map((item) => item.trim())
                              .filter(Boolean)
                          });
                          setShowPermissionModal(true);
                        }}
                      >
                        <EditIcon />
                      </button>
                      <button
                        className="icon-button small-icon-button"
                        type="button"
                        title="Borrar permiso"
                        onClick={() => onDeletePermission(permiso.id)}
                      >
                        <TrashIcon />
                      </button>
                    </span>
                  ))}
                </div>
              </div>
              <div className="admin-actions-row">
                <button
                  className="secondary-button icon-button"
                  type="button"
                  title="Crear permiso"
                  onClick={() => {
                    setEditingPermissionId(null);
                    setPermissionForm({ nombre: "", opciones: [] });
                    setShowPermissionModal(true);
                  }}
                >
                  <PlusIcon />
                </button>
              </div>
            </article>
          ) : null}
        </div>
      </section>

      <section className="list-panel admin-users-panel">
        <div className="section-heading">
          <p className="eyebrow">Administracion</p>
          <h2>Crear Usuario</h2>
        </div>
        <form className="stack-form admin-user-form" onSubmit={handleCreateUser}>
          <div className="admin-form-grid">
            <label><span>Username</span><input value={userForm.username} onChange={(event) => setUserForm((current) => ({ ...current, username: event.target.value }))} required /></label>
            <label><span>Nombre</span><input value={userForm.nombre} onChange={(event) => setUserForm((current) => ({ ...current, nombre: event.target.value }))} required /></label>
            <label><span>Apellidos</span><input value={userForm.apellidos} onChange={(event) => setUserForm((current) => ({ ...current, apellidos: event.target.value }))} required /></label>
            <label><span>Email</span><input type="email" value={userForm.email} onChange={(event) => setUserForm((current) => ({ ...current, email: event.target.value }))} /></label>
            <label><span>DNI</span><input value={userForm.dni} onChange={(event) => setUserForm((current) => ({ ...current, dni: event.target.value }))} required /></label>
            <label><span>Password</span><input type="password" value={userForm.password} onChange={(event) => setUserForm((current) => ({ ...current, password: event.target.value }))} required /></label>
          </div>
          <div className="admin-select-row">
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
          </div>
          <button className="primary-button admin-create-button" type="submit">Crear</button>
        </form>

        {message ? <p className="form-success">{message}</p> : null}
        {error ? <p className="form-error">{error}</p> : null}

        <div className="section-heading admin-subheading">
          <h2>Usuarios</h2>
        </div>
        <label className="admin-search-field">
          <span>Buscar usuario</span>
          <input
            value={userSearch}
            onChange={(event) => setUserSearch(event.target.value)}
            placeholder="Username o email"
          />
        </label>
        <div className="ticket-list">
          {pagedUsers.map((usuario) => (
            <article className="ticket-row" key={`usuario-${usuario.id}`}>
              <div className="ticket-main">
                {editingUserId === usuario.id ? (
                  <form className="stack-form" onSubmit={handleUpdateUser}>
                    <label><span>Username</span><input value={editingUserForm.username} onChange={(event) => setEditingUserForm((current) => ({ ...current, username: event.target.value }))} required /></label>
                    <label><span>Nombre</span><input value={editingUserForm.nombre} onChange={(event) => setEditingUserForm((current) => ({ ...current, nombre: event.target.value }))} required /></label>
                    <label><span>Apellidos</span><input value={editingUserForm.apellidos} onChange={(event) => setEditingUserForm((current) => ({ ...current, apellidos: event.target.value }))} required /></label>
                    <label><span>Email</span><input type="email" value={editingUserForm.email || ""} onChange={(event) => setEditingUserForm((current) => ({ ...current, email: event.target.value }))} /></label>
                    <label><span>DNI</span><input value={editingUserForm.dni} onChange={(event) => setEditingUserForm((current) => ({ ...current, dni: event.target.value }))} required /></label>
                    <label>
                      <span>Grupo</span>
                      <select value={editingUserForm.grupoId} onChange={(event) => setEditingUserForm((current) => ({ ...current, grupoId: event.target.value }))} required>
                        {grupos.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.nombre}</option>)}
                      </select>
                    </label>
                    <label>
                      <span>Activo</span>
                      <select value={String(editingUserForm.activo)} onChange={(event) => setEditingUserForm((current) => ({ ...current, activo: event.target.value === "true" }))}>
                        <option value="true">Activo</option>
                        <option value="false">Inactivo</option>
                      </select>
                    </label>
                    <div className="comment-actions">
                      <button className="secondary-button" type="submit">Guardar</button>
                      <button className="secondary-button" type="button" onClick={() => { setEditingUserId(null); setEditingUserForm(null); }}>
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : (
                  <>
                    <button
                      className="admin-user-toggle"
                      type="button"
                      onClick={() => setExpandedUserId((current) => current === usuario.id ? null : usuario.id)}
                    >
                      <strong>{usuario.username}</strong>
                    </button>
                    {expandedUserId === usuario.id ? (
                      <>
                        <p>{usuario.nombre} {usuario.apellidos}</p>
                        <p>{usuario.rol} | {usuario.grupo}</p>
                        <p>{usuario.email || "Sin email"}</p>
                        <p>{usuario.activo ? "Activo" : "Inactivo"}</p>
                        <p>Permisos: {usuario.permisos?.length ? usuario.permisos.join(", ") : "Sin permisos"}</p>
                      </>
                    ) : null}
                  </>
                )}
              </div>
              <div className="admin-user-side">
                <div className="admin-actions-row admin-actions-row-right">
                  <button
                    className="secondary-button icon-button"
                    type="button"
                    title="Editar usuario"
                    onClick={() => {
                      setEditingUserId(usuario.id);
                      setEditingUserForm({
                        username: usuario.username,
                        nombre: usuario.nombre,
                        apellidos: usuario.apellidos,
                        email: usuario.email || "",
                        dni: usuario.dni,
                        grupoId: grupos.find((grupo) => grupo.nombre === usuario.grupo)?.id || "",
                        activo: usuario.activo
                      });
                    }}
                  >
                    <EditIcon />
                  </button>
                  <button
                    className="secondary-button icon-button"
                    type="button"
                    title="Cambiar password"
                    onClick={() => setPasswordForm({ userId: usuario.id, password: "" })}
                  >
                    <PasswordIcon />
                  </button>
                  <button
                    className="secondary-button icon-button"
                    type="button"
                    title="Borrar usuario"
                    onClick={() => handleDeleteUser(usuario.id)}
                  >
                    <TrashIcon />
                  </button>
                </div>
                {passwordForm.userId === usuario.id ? (
                  <form className="stack-form admin-password-form" onSubmit={handleChangePassword}>
                    <input
                      type="password"
                      value={passwordForm.password}
                      onChange={(event) => setPasswordForm({ userId: usuario.id, password: event.target.value })}
                      placeholder="Nueva password"
                      required
                    />
                    <div className="comment-actions">
                      <button className="secondary-button compact-button" type="submit">Guardar</button>
                      <button
                        className="secondary-button compact-button"
                        type="button"
                        onClick={() => setPasswordForm({ userId: null, password: "" })}
                      >
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : null}
              </div>
            </article>
          ))}
        </div>
        {filteredUsers.length > 10 ? (
          <div className="pagination-strip">
            <button className="secondary-button" type="button" onClick={() => setUserPage((current) => current - 1)} disabled={userPage === 0}>
              Anterior
            </button>
            <span>Pagina {userPage + 1} de {totalUserPages}</span>
            <button className="secondary-button" type="button" onClick={() => setUserPage((current) => current + 1)} disabled={userPage + 1 >= totalUserPages}>
              Siguiente
            </button>
          </div>
        ) : null}
        <button className="secondary-button content-button" type="button" onClick={onRefresh}>Recargar admin</button>
      </section>
      {showPermissionModal ? (
        <div className="modal-overlay">
          <section className="draggable-modal admin-permission-modal">
            <header className="modal-header">
              <strong>{editingPermissionId ? "Editar permiso" : "Crear permiso"}</strong>
              <button className="secondary-button" type="button" onClick={() => setShowPermissionModal(false)}>
                Cerrar
              </button>
            </header>
            <form className="stack-form" onSubmit={handleCreatePermission}>
              <label>
                <span>Nombre de permiso</span>
                <input
                  value={permissionForm.nombre}
                  onChange={(event) => setPermissionForm((current) => ({ ...current, nombre: event.target.value }))}
                  required
                />
              </label>
              <div className="permission-options">
                <span>Personalizar permiso</span>
                {permissionOptions.map((option) => (
                  <label className="permission-check" key={option}>
                    <input
                      type="checkbox"
                      checked={permissionForm.opciones.includes(option)}
                      onChange={(event) => setPermissionForm((current) => ({
                        ...current,
                        opciones: event.target.checked
                          ? [...current.opciones, option]
                          : current.opciones.filter((item) => item !== option)
                      }))}
                    />
                    <span>{option}</span>
                  </label>
                ))}
              </div>
              <button className="primary-button content-button" type="submit">
                {editingPermissionId ? "Guardar permiso" : "Crear permiso"}
              </button>
            </form>
          </section>
        </div>
      ) : null}
    </section>
  );
}

function CreateUserFromTicketModal({
  isOpen,
  initialData,
  roles,
  groups,
  onClose,
  onCreateUser
}) {
  const modalRef = useRef(null);
  const [position, setPosition] = useState({ x: 120, y: 120 });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    username: "",
    nombre: "",
    apellidos: "",
    email: "",
    dni: "",
    password: "",
    rolId: "",
    grupoId: "",
    activo: true
  });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setForm({
      username: initialData?.username || "",
      nombre: initialData?.nombre || "",
      apellidos: initialData?.apellidos || "",
      email: initialData?.email || "",
      dni: initialData?.dni || "",
      password: "",
      rolId: roles.find((rol) => rol.nombre === "USER")?.id || roles[0]?.id || "",
      grupoId: groups.find((grupo) => grupo.nombre === "Administradores")?.id || groups[0]?.id || "",
      activo: true
    });
    setMessage("");
    setError("");
    setPosition({ x: 120, y: 120 });
  }, [isOpen]);

  if (!isOpen) {
    return null;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await onCreateUser({
        ...form,
        rolId: Number(form.rolId),
        grupoId: Number(form.grupoId)
      });
      setMessage("Usuario creado correctamente");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function startDragging(event) {
    event.preventDefault();

    const startX = event.clientX;
    const startY = event.clientY;
    const initialX = position.x;
    const initialY = position.y;

    function handleMouseMove(moveEvent) {
      const nextPosition = {
        x: initialX + (moveEvent.clientX - startX),
        y: initialY + (moveEvent.clientY - startY)
      };

      setPosition(nextPosition);

      if (modalRef.current) {
        modalRef.current.style.left = `${nextPosition.x}px`;
        modalRef.current.style.top = `${nextPosition.y}px`;
      }
    }

    function handleMouseUp() {
      document.removeEventListener("mousemove", handleMouseMove);
      document.removeEventListener("mouseup", handleMouseUp);
    }

    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
  }

  return (
    <div className="modal-overlay">
      <section
        ref={modalRef}
        className="draggable-modal"
        style={{ left: `${position.x}px`, top: `${position.y}px` }}
      >
        <header className="modal-header">
          <div
            className="modal-drag-handle"
            onMouseDown={startDragging}
          >
            <strong>Crear usuario desde ticket</strong>
          </div>
          <button className="secondary-button" type="button" onClick={onClose}>
            Cerrar
          </button>
        </header>

        <form className="stack-form" onSubmit={handleSubmit}>
          <label><span>Username</span><input value={form.username} onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))} required /></label>
          <label><span>Nombre</span><input value={form.nombre} onChange={(event) => setForm((current) => ({ ...current, nombre: event.target.value }))} required /></label>
          <label><span>Apellidos</span><input value={form.apellidos} onChange={(event) => setForm((current) => ({ ...current, apellidos: event.target.value }))} required /></label>
          <label><span>Email</span><input type="email" value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} /></label>
          <label><span>DNI</span><input value={form.dni} onChange={(event) => setForm((current) => ({ ...current, dni: event.target.value }))} required /></label>
          <label><span>Password inicial</span><input type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} required /></label>
          <label>
            <span>Rol</span>
            <select value={form.rolId} onChange={(event) => setForm((current) => ({ ...current, rolId: event.target.value }))} required>
              {roles.map((rol) => (
                <option key={rol.id} value={rol.id}>{rol.nombre}</option>
              ))}
            </select>
          </label>
          <label>
            <span>Grupo</span>
            <select value={form.grupoId} onChange={(event) => setForm((current) => ({ ...current, grupoId: event.target.value }))} required>
              {groups.map((grupo) => (
                <option key={grupo.id} value={grupo.id}>{grupo.nombre}</option>
              ))}
            </select>
          </label>
          {message ? <p className="form-success">{message}</p> : null}
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit">Crear usuario</button>
        </form>
      </section>
    </div>
  );
}

function Dashboard({ incidencias, peticiones }) {
  function buildStatusCounts(items) {
    return {
      abiertas: items.filter((item) => item.estado === "ABIERTA").length,
      enProceso: items.filter((item) => item.estado === "EN_PROCESO").length,
      cerradas: items.filter((item) => item.estado === "CERRADA").length
    };
  }

  const cards = [
    {
      title: "Incidencias",
      value: incidencias.length,
      subtitle: "Incidencias cargadas en esta sesion",
      counts: buildStatusCounts(incidencias),
      image:
        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80"
    },
    {
      title: "Peticiones",
      value: peticiones.length,
      subtitle: "Peticiones cargadas en esta sesion",
      counts: buildStatusCounts(peticiones),
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
            <div className="metric-status-row">
              <span><span className="status-dot open"></span>{card.counts.abiertas}</span>
              <span><span className="status-dot in-progress"></span>{card.counts.enProceso}</span>
              <span><span className="status-dot closed"></span>{card.counts.cerradas}</span>
            </div>
          </div>
        </article>
      ))}
    </section>
  );
}

function TicketDetail({
  ticket,
  currentUsername,
  currentUserRole,
  assignables,
  onAssign,
  onChangeStatus,
  onAddComment,
  onUpdateComment,
  onDeleteComment,
  onCreateUserFromTicket
}) {
  const userRequestData = useMemo(
    () => parseUserRequestDescription(ticket.descripcion),
    [ticket.descripcion]
  );
  const [assignedUsername, setAssignedUsername] = useState(ticket.usuarioAsignado || "");
  const [nextStatus, setNextStatus] = useState(ticket.estado);
  const [comment, setComment] = useState("");
  const [actionError, setActionError] = useState("");
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingContent, setEditingContent] = useState("");
  const [showCreateUserModal, setShowCreateUserModal] = useState(false);

  useEffect(() => {
    setAssignedUsername(ticket.usuarioAsignado || "");
  }, [ticket.usuarioAsignado]);

  useEffect(() => {
    setNextStatus(ticket.estado);
  }, [ticket.estado]);

  async function handleAssign() {
    setActionError("");

    try {
      await onAssign(ticket.id, assignedUsername);
    } catch (requestError) {
      setActionError(requestError.message);
    }
  }

  async function handleStatusUpdate() {
    setActionError("");

    try {
      await onChangeStatus(ticket.id, nextStatus);
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
    <>
    <article className="ticket-detail ticket-detail-full">
      <div className="ticket-main">
        <strong>{ticket.codigoTicket}</strong>
        <p className="detail-label"><strong>Asunto:</strong> {ticket.titulo}</p>
        <p className="detail-label"><strong>Descripcion:</strong> {ticket.descripcion}</p>

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
            <button className="secondary-button content-button" type="submit">
              Anadir comentario
            </button>
          </form>
        </section>
      </div>

      <div className="ticket-meta">
        <div className="ticket-meta-box">
          <span><span className={getStatusColor(ticket.estado)}></span> Estado: {ticket.estado}</span>
          <span>Grupo: {ticket.grupo}</span>
          <span>Servicio: {ticket.servicio}</span>
          <span>Categoria: {ticket.categoria}</span>
          <span>Asignado: {ticket.usuarioAsignado || "Sin asignar"}</span>
        </div>
        <span>Creador: {ticket.usuario || "-"}</span>
        {currentUserRole === "SUPER_ADMIN" && ticket.categoria === "Alta de usuario" ? (
          <button className="secondary-button" type="button" onClick={() => setShowCreateUserModal(true)}>
            Crear usuario desde ticket
          </button>
        ) : null}
        <label>
          <span>Nuevo estado</span>
          <select value={nextStatus} onChange={(event) => setNextStatus(event.target.value)}>
            {ESTADOS_GESTION.map((estado) => (
              <option key={estado} value={estado}>
                {estado}
              </option>
            ))}
          </select>
        </label>
        <button className="secondary-button" type="button" onClick={handleStatusUpdate}>
          Confirmar cambio
        </button>
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
        {actionError ? <p className="form-error">{actionError}</p> : null}
      </div>
    </article>
    <CreateUserFromTicketModal
      isOpen={showCreateUserModal}
      initialData={userRequestData}
      roles={onCreateUserFromTicket.roles}
      groups={onCreateUserFromTicket.groups}
      onClose={() => setShowCreateUserModal(false)}
      onCreateUser={async (payload) => {
        await onCreateUserFromTicket.create(payload);
        setShowCreateUserModal(false);
      }}
    />
    </>
  );
}

function TicketsPage({
  title,
  endpoint,
  currentUsername,
  currentUserRole,
  groups,
  tickets,
  pageInfo,
  assignables,
  onRefresh,
  onChangeStatus,
  onAssign,
  onAddComment,
  onUpdateComment,
  onDeleteComment,
  onCreateUserFromTicket
}) {
  const singularTitle = endpoint === "incidencias" ? "incidencia" : "peticion";
  const catalogo = CATALOGOS_TICKETS[endpoint];
  const servicios = useMemo(() => Object.keys(catalogo), [catalogo]);

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
  const [filters, setFilters] = useState(DEFAULT_TICKET_FILTERS);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [selectedTicketId, setSelectedTicketId] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [sortOrder, setSortOrder] = useState("desc");

  const selectedTicket =
    tickets.find((ticket) => ticket.id === selectedTicketId) || null;
  const visibleTickets = [...tickets].sort((left, right) => {
    const leftValue = new Date(left.fechaCreacion).getTime();
    const rightValue = new Date(right.fechaCreacion).getTime();
    return sortOrder === "asc" ? leftValue - rightValue : rightValue - leftValue;
  });

  useEffect(() => {
    setForm((current) => ({
      ...current,
      grupoId: current.grupoId || groups[0]?.id || ""
    }));
  }, [groups]);

  useEffect(() => {
    setForm(initialForm);
  }, [initialForm]);

  useEffect(() => {
    if (!tickets.length) {
      setSelectedTicketId(null);
    }
  }, [selectedTicketId, tickets]);

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
      await onRefresh(filters, currentPage);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleFilterChange(field, value) {
    const nextFilters = { ...filters, [field]: value };
    setFilters(nextFilters);
    setCurrentPage(0);
    await onRefresh(nextFilters, 0);
  }

  async function handleResetFilters() {
    setFilters(DEFAULT_TICKET_FILTERS);
    setCurrentPage(0);
    await onRefresh(DEFAULT_TICKET_FILTERS, 0);
  }

  async function handlePageChange(nextPage) {
    setCurrentPage(nextPage);
    await onRefresh(filters, nextPage);
  }

  return (
    <section className="content-grid">
      {selectedTicket ? (
        <section className="list-panel ticket-detail-screen">
          <div className="section-heading section-heading-inline">
            <div>
              <p className="eyebrow">Detalle</p>
              <h2>{selectedTicket.codigoTicket}</h2>
            </div>
            <button className="secondary-button" type="button" onClick={() => setSelectedTicketId(null)}>
              Volver al listado
            </button>
          </div>

          <TicketDetail
            ticket={selectedTicket}
            currentUsername={currentUsername}
            currentUserRole={currentUserRole}
            assignables={assignables}
            onAssign={onAssign}
            onChangeStatus={onChangeStatus}
            onAddComment={onAddComment}
            onUpdateComment={onUpdateComment}
            onDeleteComment={onDeleteComment}
            onCreateUserFromTicket={onCreateUserFromTicket}
          />
        </section>
      ) : (
        <>
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

          <section className="list-panel ticket-list-screen">
            <div className="section-heading section-heading-inline">
              <div>
                <p className="eyebrow">Listado</p>
                <h2>{title}</h2>
              </div>
              <div className="filter-strip">
                <label className="filter-field">
                  <span>Estado</span>
                  <select
                    value={filters.estado}
                    onChange={(event) => handleFilterChange("estado", event.target.value)}
                  >
                    <option value="">Mostrar todos</option>
                    {ESTADOS_TICKET.map((estado) => (
                      <option key={estado} value={estado}>
                        {estado}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="filter-field">
                  <span>Grupo</span>
                  <select
                    value={filters.grupoId || ""}
                    onChange={(event) => handleFilterChange("grupoId", event.target.value)}
                  >
                    <option value="">Todos los grupos</option>
                    {groups.map((group) => (
                      <option key={group.id} value={group.id}>
                        {group.nombre}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="filter-field">
                  <span>Desde</span>
                  <input
                    type="date"
                    value={filters.fechaDesde}
                    onChange={(event) => handleFilterChange("fechaDesde", event.target.value)}
                  />
                </label>
                <label className="filter-field">
                  <span>Hasta</span>
                  <input
                    type="date"
                    value={filters.fechaHasta}
                    onChange={(event) => handleFilterChange("fechaHasta", event.target.value)}
                  />
                </label>
                {endpoint === "peticiones" ? (
                  <label className="filter-field">
                    <span>Fecha</span>
                    <select value={sortOrder} onChange={(event) => setSortOrder(event.target.value)}>
                      <option value="desc">Mas recientes</option>
                      <option value="asc">Mas antiguas</option>
                    </select>
                  </label>
                ) : null}
                <button className="secondary-button aligned-filter-button" type="button" onClick={handleResetFilters}>
                  Recargar
                </button>
              </div>
            </div>

            <div className="ticket-summary-list">
              {tickets.length === 0 ? (
                <p className="empty-state">No hay resultados para este filtro.</p>
              ) : (
                visibleTickets.map((ticket) => (
                  <button
                    key={`${endpoint}-${ticket.id}`}
                    className="ticket-summary"
                    type="button"
                    onClick={() => setSelectedTicketId(ticket.id)}
                  >
                    <span className={getStatusColor(ticket.estado)}></span>
                    <span>{ticket.codigoTicket}</span>
                    <span>|</span>
                    <span><strong>Asunto:</strong> {ticket.titulo}</span>
                    <span>|</span>
                    <span><strong>Estado:</strong> {ticket.estado}</span>
                    <span>|</span>
                    <span><strong>Creado por:</strong> {ticket.usuario || "-"}</span>
                    <span>|</span>
                    <span><strong>Grupo:</strong> {ticket.grupo || "-"}</span>
                    <span>|</span>
                    <span><strong>Fecha:</strong> {formatTicketDate(ticket.fechaCreacion)}</span>
                  </button>
                ))
              )}
            </div>
            {pageInfo.totalPages > 1 ? (
              <div className="pagination-strip">
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                >
                  Anterior
                </button>
                <span>Pagina {currentPage + 1} de {pageInfo.totalPages}</span>
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage + 1 >= pageInfo.totalPages}
                >
                  Siguiente
                </button>
              </div>
            ) : null}
          </section>
        </>
      )}
    </section>
  );
}

export default function App() {
  const navigate = useNavigate();
  const [authLoading, setAuthLoading] = useState(true);
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [forgotPasswordLoading, setForgotPasswordLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [registerMessage, setRegisterMessage] = useState("");
  const [forgotPasswordMessage, setForgotPasswordMessage] = useState("");
  const [user, setUser] = useState(null);
  const [groups, setGroups] = useState([]);
  const [incidencias, setIncidencias] = useState([]);
  const [peticiones, setPeticiones] = useState([]);
  const [incidenciasPageInfo, setIncidenciasPageInfo] = useState({ page: 0, totalPages: 0, totalElements: 0, size: 10 });
  const [peticionesPageInfo, setPeticionesPageInfo] = useState({ page: 0, totalPages: 0, totalElements: 0, size: 10 });
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

  async function loadIncidencias(filters = DEFAULT_TICKET_FILTERS, page = 0) {
    const params = new URLSearchParams({ page: String(page), size: "10" });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }
    if (filters.grupoId) {
      params.set("grupoId", filters.grupoId);
    }
    if (filters.fechaDesde) {
      params.set("fechaDesde", filters.fechaDesde);
    }
    if (filters.fechaHasta) {
      params.set("fechaHasta", filters.fechaHasta);
    }

    const data = await apiRequest(`/incidencias?${params.toString()}`);
    setIncidencias(data.content || []);
    setIncidenciasPageInfo({
      page: data.page || 0,
      totalPages: data.totalPages || 0,
      totalElements: data.totalElements || 0,
      size: data.size || 10
    });
  }

  async function loadIncidenciaAssignables() {
    const data = await apiRequest("/incidencias/asignables");
    setIncidenciaAssignables(data);
  }

  async function loadPeticiones(filters = DEFAULT_TICKET_FILTERS, page = 0) {
    const params = new URLSearchParams({ page: String(page), size: "10" });

    if (filters.estado) {
      params.set("estado", filters.estado);
    }
    if (filters.grupoId) {
      params.set("grupoId", filters.grupoId);
    }
    if (filters.fechaDesde) {
      params.set("fechaDesde", filters.fechaDesde);
    }
    if (filters.fechaHasta) {
      params.set("fechaHasta", filters.fechaHasta);
    }

    const data = await apiRequest(`/peticiones?${params.toString()}`);
    setPeticiones(data.content || []);
    setPeticionesPageInfo({
      page: data.page || 0,
      totalPages: data.totalPages || 0,
      totalElements: data.totalElements || 0,
      size: data.size || 10
    });
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
    setForgotPasswordMessage("");

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

  async function handleForgotPassword(request) {
    setForgotPasswordLoading(true);
    setLoginError("");
    setRegisterMessage("");
    setForgotPasswordMessage("");

    try {
      const data = await apiRequest("/auth/forgot-password", {
        method: "POST",
        body: JSON.stringify(request)
      });
      setForgotPasswordMessage(data.mensaje);
    } catch (requestError) {
      setLoginError(requestError.message);
    } finally {
      setForgotPasswordLoading(false);
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

  async function updateAdminGroup(groupId, nombre) {
    await apiRequest(`/admin/grupos/${groupId}`, {
      method: "PUT",
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

  async function updateAdminUser(userId, payload) {
    await apiRequest(`/admin/usuarios/${userId}`, {
      method: "PUT",
      body: JSON.stringify({
        username: payload.username,
        nombre: payload.nombre,
        apellidos: payload.apellidos,
        email: payload.email,
        dni: payload.dni
      })
    });
  }

  async function updateAdminUserGroup(userId, grupoId) {
    await apiRequest(`/admin/usuarios/${userId}/grupo`, {
      method: "PUT",
      body: JSON.stringify({ grupoId })
    });
  }

  async function updateAdminUserActive(userId, activo) {
    await apiRequest(`/admin/usuarios/${userId}/activo`, {
      method: "PUT",
      body: JSON.stringify({ activo })
    });
    await loadAdminData();
  }

  async function changeAdminUserPassword(userId, password) {
    await apiRequest(`/admin/usuarios/${userId}/password`, {
      method: "PUT",
      body: JSON.stringify({ password })
    });
    await loadAdminData();
  }

  async function assignAdminRolePermission(roleId, permisoId) {
    await apiRequest(`/admin/roles/${roleId}/permisos`, {
      method: "PUT",
      body: JSON.stringify({ permisoId })
    });
    await loadAdminData();
  }

  async function removeAdminRolePermission(roleId, permisoId) {
    await apiRequest(`/admin/roles/${roleId}/permisos/${permisoId}`, {
      method: "DELETE"
    });
    await loadAdminData();
  }

  async function createAdminPermission(nombre, descripcion) {
    await apiRequest("/admin/permisos", {
      method: "POST",
      body: JSON.stringify({ nombre, descripcion })
    });
    await loadAdminData();
  }

  async function updateAdminPermission(permisoId, nombre, descripcion) {
    await apiRequest(`/admin/permisos/${permisoId}`, {
      method: "PUT",
      body: JSON.stringify({ nombre, descripcion })
    });
    await loadAdminData();
  }

  async function deleteAdminPermission(permisoId) {
    await apiRequest(`/admin/permisos/${permisoId}`, {
      method: "DELETE"
    });
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
            onForgotPassword={handleForgotPassword}
            loading={loginLoading}
            registerLoading={registerLoading}
            forgotPasswordLoading={forgotPasswordLoading}
            error={loginError}
            registerMessage={registerMessage}
            forgotPasswordMessage={forgotPasswordMessage}
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
                onUpdateGroup={updateAdminGroup}
                onDeleteGroup={deleteAdminGroup}
                onCreateUser={createAdminUser}
                onDeleteUser={deleteAdminUser}
                onUpdateUser={updateAdminUser}
                onChangeUserPassword={changeAdminUserPassword}
                onAssignRolePermission={assignAdminRolePermission}
                onRemoveRolePermission={removeAdminRolePermission}
                onUpdateUserGroup={updateAdminUserGroup}
                onUpdateUserActive={updateAdminUserActive}
                onCreatePermission={createAdminPermission}
                onUpdatePermission={updateAdminPermission}
                onDeletePermission={deleteAdminPermission}
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
                currentUserRole={user?.rol || ""}
                groups={groups}
                tickets={incidencias}
                pageInfo={incidenciasPageInfo}
                assignables={incidenciaAssignables}
                onRefresh={loadIncidencias}
                onChangeStatus={(id, estado) => updateTicketStatus("incidencias", id, estado)}
                onAssign={(id, usernameAsignado) => assignTicket("incidencias", id, usernameAsignado)}
                onAddComment={(id, contenido) => addTicketComment("incidencias", id, contenido)}
                onUpdateComment={(id, commentId, contenido) =>
                  updateTicketComment("incidencias", id, commentId, contenido)
                }
                onDeleteComment={(id, commentId) => deleteTicketComment("incidencias", id, commentId)}
                onCreateUserFromTicket={{
                  create: createAdminUser,
                  roles: adminRoles,
                  groups: adminGroups
                }}
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
                currentUserRole={user?.rol || ""}
                groups={groups}
                tickets={peticiones}
                pageInfo={peticionesPageInfo}
                assignables={peticionAssignables}
                onRefresh={loadPeticiones}
                onChangeStatus={(id, estado) => updateTicketStatus("peticiones", id, estado)}
                onAssign={(id, usernameAsignado) => assignTicket("peticiones", id, usernameAsignado)}
                onAddComment={(id, contenido) => addTicketComment("peticiones", id, contenido)}
                onUpdateComment={(id, commentId, contenido) =>
                  updateTicketComment("peticiones", id, commentId, contenido)
                }
                onDeleteComment={(id, commentId) => deleteTicketComment("peticiones", id, commentId)}
                onCreateUserFromTicket={{
                  create: createAdminUser,
                  roles: adminRoles,
                  groups: adminGroups
                }}
              />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}
