const state = {
  user: JSON.parse(localStorage.getItem("piedrazulUser") || "null"),
  especialistas: [],
  citas: [],
  portalSlots: [],
  selectedPortalSlot: null,
  settings: JSON.parse(localStorage.getItem("piedrazulSettings") || "null") || {
    intervaloMinutos: 30,
    semanasHabilitadas: 8,
    horaInicio: "08:00",
    horaFin: "12:00"
  },
  currentRoute: "inicio"
};

const GENERAL_SPECIALTY = "CONSULTA_GENERAL";
const GENERAL_SPECIALTIES = new Set(["CONSULTA_GENERAL", "MEDICINA_GENERAL"]);
const SPECIALTY_ORDER = [GENERAL_SPECIALTY, "TERAPIA_NEURAL", "QUIROPRAXIA", "FISIOTERAPIA"];
const BLOCKING_APPOINTMENT_STATES = new Set(["AGENDADA", "PENDIENTE", "REAGENDADA"]);
const ATTENDANCE_STATES = new Set(["ASISTIDA", "NO_ASISTIDA"]);
const manualPatientAutocomplete = { timer: null, requestId: 0 };

const SPECIALTY_LABELS = {
  CONSULTA_GENERAL: "Consulta General",
  TERAPIA_NEURAL: "Terapia Neural",
  QUIROPRAXIA: "Quiropraxia",
  FISIOTERAPIA: "Fisioterapia",
  MEDICINA_GENERAL: "Consulta General",
  PSICOLOGIA: "Psicologia",
};

const DAY_LABELS = {
  MONDAY: "Lunes",
  TUESDAY: "Martes",
  WEDNESDAY: "Miércoles",
  THURSDAY: "Jueves",
  FRIDAY: "Viernes",
  SATURDAY: "Sábado",
  SUNDAY: "Domingo"
};

const DAY_OPTIONS = [
  ["MONDAY", "Lunes"],
  ["TUESDAY", "Martes"],
  ["WEDNESDAY", "Miércoles"],
  ["THURSDAY", "Jueves"],
  ["FRIDAY", "Viernes"],
  ["SATURDAY", "Sábado"],
  ["SUNDAY", "Domingo"]
];

const $ = selector => document.querySelector(selector);
const viewRoot = $("#viewRoot");
const alertRoot = $("#alertRoot");
const modalRoot = $("#modalRoot");
const ROUTE_PERMISSIONS = {
  inicio: ["AGENDADOR", "PACIENTE"],

  "agendar-cita": ["AGENDADOR"],
  "consultar-citas": ["AGENDADOR"],
  "registrar-medico": ["AGENDADOR"],
  "dias-atencion": ["AGENDADOR"],
  "citas-medico": ["AGENDADOR"],
  "intervalo-citas": ["AGENDADOR"],
  "ventana-agendamiento": ["AGENDADOR"],

  "portal-agendar": ["PACIENTE"],
  "mis-citas": ["PACIENTE"],

  login: ["PUBLIC"],
  registro: ["PUBLIC"]
};

function getCurrentRole() {
  return String(state.user?.rolUsuario || "").toUpperCase();
}

function isPublicRoute(route) {
  return ["login", "registro"].includes(route);
}

function canAccessRoute(route) {
  if (isPublicRoute(route)) return true;

  const role = getCurrentRole();

  if (!role) return false;

  const allowedRoles = ROUTE_PERMISSIONS[route];

  if (!allowedRoles) return false;

  return allowedRoles.includes(role);
}

function getDefaultRouteByRole() {
  const role = getCurrentRole();

  if (role === "PACIENTE") {
    return "portal-agendar";
  }

  if (role === "AGENDADOR") {
    return "inicio";
  }

  return "login";
}
function updateMenuByRole() {
  const role = getCurrentRole();

  document.querySelectorAll("[data-roles]").forEach(element => {
    const allowedRoles = element.dataset.roles
      .split(",")
      .map(item => item.trim().toUpperCase());

    const canSee = role && allowedRoles.includes(role);

    element.classList.toggle("hidden-by-role", !canSee);
  });

  document.querySelectorAll("[data-public='true']").forEach(element => {
    element.classList.toggle("hidden-by-role", Boolean(state.user));
  });

  const logoutButton = $("#logoutButton");
  if (logoutButton) {
    logoutButton.classList.toggle("hidden-by-role", !state.user);
  }
}

function saveSettings() {
  localStorage.setItem("piedrazulSettings", JSON.stringify(state.settings));
}

function getUserDisplayName() {
  if (!state.user) return "Sin sesión";

  const nombreCompleto = [
    state.user.nombreUsuario || state.user.nombre,
    state.user.apellidoUsuario || state.user.apellido
  ]
    .filter(Boolean)
    .join(" ")
    .trim();

  return nombreCompleto || `Usuario ${state.user.idUsuario}`;
}

function saveUser() {
  if (state.user) {
    localStorage.setItem("piedrazulUser", JSON.stringify(state.user));
  } else {
    localStorage.removeItem("piedrazulUser");
  }

  const label = state.user
    ? `${getUserDisplayName()} (${state.user.rolUsuario || "sin rol"})`
    : "Sin sesión";

  $("#sidebarUser").textContent = `Usuario: ${label}`;
}

function showAlert(message, type = "info") {
  const el = document.createElement("div");
  el.className = `alert ${type}`;
  el.textContent = message;
  alertRoot.appendChild(el);
  setTimeout(() => el.remove(), 4800);
}

function emptyState(message = "No hay información para mostrar") {
  return `<div class="empty-state"><strong>${message}</strong><p>Verifique los filtros o registre nuevos datos.</p></div>`;
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>'"]/g, ch => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[ch]));
}

function formatSpecialty(value) { return SPECIALTY_LABELS[value] || String(value || "Sin especialidad").replaceAll("_", " "); }
function fullName(nombre, apellido) {
  return [nombre, apellido].filter(Boolean).join(" ").trim();
}

function splitFullName(value) {
  const parts = String(value || "").trim().split(/\s+/).filter(Boolean);
  if (parts.length <= 1) {
    return { nombre: parts[0] || "", apellido: "" };
  }

  return { nombre: parts.slice(0, -1).join(" "), apellido: parts.at(-1) };
}

function getAppointmentPatientName(cita) {
  const nombre = fullName(cita?.pacienteNombre, cita?.pacienteApellido);
  return nombre || cita?.pacienteNombre || `Paciente ${cita?.pacienteId || "-"}`;
}

function formatDate(value) {
  if (!value) return "-";
  const [y, m, d] = String(value).split("-");
  return y && m && d ? `${Number(d)}/${Number(m)}/${y}` : value;
}
function formatTime(value) {
  if (!value) return "-";
  const [h, m] = String(value).split(":");
  const hour = Number(h);
  const suffix = hour >= 12 ? "PM" : "AM";
  const hour12 = hour % 12 || 12;
  return `${String(hour12).padStart(2, "0")}:${m || "00"} ${suffix}`;
}
function statusBadge(status) {
  const normalized = String(status || "AGENDADA").toUpperCase();
  const cls = normalized.includes("CANCEL") || normalized === "NO_ASISTIDA" ? "badge-danger" : normalized === "ASISTIDA" || normalized.includes("COMPLET") ? "badge-success" : normalized.includes("REAGEND") || normalized.includes("AGEND") ? "badge-info" : "badge-muted";
  const label = (normalized.charAt(0) + normalized.slice(1).toLowerCase()).replaceAll("_", " ");
  return `<span class="badge ${cls}">${label}</span>`;
}

function todayIso() {
  return dateToLocalIso(new Date());
}

function tomorrowIso() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return dateToLocalIso(date);
}

function dateToLocalIso(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function normalizePersonName(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .replace(/\s+/g, " ")
    .toLowerCase()
    .split(" ")
    .filter(Boolean)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function isValidPersonName(value) {
  return /^[A-Za-z ]{2,60}$/.test(normalizePersonName(value));
}

function normalizeFormName(form, fieldName) {
  const input = form.elements[fieldName];
  if (!input) return "";
  input.value = normalizePersonName(input.value);
  return input.value;
}

function isBlockingAppointment(cita) {
  return BLOCKING_APPOINTMENT_STATES.has(String(cita?.estado || "").toUpperCase());
}

function canCancelAppointment(cita) {
  const estado = String(cita?.estado || "").toUpperCase();
  return estado !== "CANCELADA" && estado !== "ASISTIDA" && estado !== "NO_ASISTIDA";
}

function hasBlockingAppointment(patientId) {
  return state.citas.some(cita => Number(cita.pacienteId) === Number(patientId) && isBlockingAppointment(cita));
}

function getCurrentPatientId() {
  return Number(state.user?.idUsuario || state.user?.id || 0);
}

function isGeneralSpecialty(value) {
  return GENERAL_SPECIALTIES.has(String(value || "").trim().toUpperCase());
}

function hasAnyAppointment(patientId) {
  return state.citas.some(cita => Number(cita.pacienteId) === Number(patientId));
}

function isPastAppointment(cita) {
  return Boolean(cita?.fecha) && String(cita.fecha) < todayIso();
}

function canMarkAttendance(cita) {
  const estado = String(cita?.estado || "").toUpperCase();
  return getCurrentRole() === "AGENDADOR" && isPastAppointment(cita) && estado !== "CANCELADA" && !ATTENDANCE_STATES.has(estado);
}

function canReagendarAppointment(cita) {
  return getCurrentRole() === "AGENDADOR" && String(cita?.estado || "").toUpperCase() === "ASISTIDA";
}

function attendanceActions(cita) {
  if (!canMarkAttendance(cita)) return "";
  return `
    <button class="btn btn-success btn-small" data-attendance="ASISTIDA" data-id="${escapeHtml(cita.id)}">Asistida</button>
    <button class="btn btn-small" data-attendance="NO_ASISTIDA" data-id="${escapeHtml(cita.id)}">No asistida</button>
  `;
}

function bindAttendanceActions(afterChange = null, root = document) {
  root.querySelectorAll("[data-attendance]").forEach(btn => btn.addEventListener("click", () => {
    updateAppointmentStatus(btn.dataset.id, btn.dataset.attendance, afterChange);
  }));
}

async function updateAppointmentStatus(id, estado, afterChange = null) {
  try {
    await api.cambiarEstadoCita(id, estado);
    showAlert("Estado de la cita actualizado correctamente.", "success");
    await loadCitas({ silent: true });
    if (afterChange) {
      afterChange();
    } else {
      route();
    }
  } catch (error) {
    showAlert(`No se pudo actualizar el estado: ${error.message}`, "error");
  }
}

function pageHeader(title, subtitle) {
  return `<div class="page-header"><h2>${title}</h2><p class="page-subtitle">${subtitle}</p></div>`;
}

async function loadEspecialistas({ silent = false } = {}) {
  try {
    const data = await api.getEspecialistas();
    state.especialistas = Array.isArray(data) ? data : [];
  } catch (error) {
    state.especialistas = [];
    if (!silent) showAlert(`No se pudo conectar con especialista-service: ${error.message}`, "error");
  }
}

async function loadCitas({ silent = false } = {}) {
  try {
    const data = await api.getCitas();
    state.citas = Array.isArray(data) ? data : [];
  } catch (error) {
    if (!silent) {
      state.citas = [];
    }
    if (!silent) showAlert(`No se pudo conectar con appointment-service: ${error.message}`, "error");
  }
}

function specialistOptions(selected = "", { generalOnly = false } = {}) {
  const especialistas = generalOnly
    ? state.especialistas.filter(e => isGeneralSpecialty(e.especialidad))
    : state.especialistas;
  return [`<option value="">Seleccione...</option>`, ...especialistas.map(e => `<option value="${escapeHtml(e.id)}" ${selected === e.id ? "selected" : ""}>${escapeHtml(e.nombre)} - ${formatSpecialty(e.especialidad)}</option>`)].join("");
}

function formatDays(dias = []) {
  if (!Array.isArray(dias) || !dias.length) {
    return "Sin días configurados";
  }
  return dias.map(dia => DAY_LABELS[String(dia).toUpperCase()] || String(dia)).join(", ");
}

function availabilityHtml(disponibilidad) {
  if (!disponibilidad) {
    return `<div class="warning-box"><strong>Disponibilidad no configurada.</strong><br />Configure días, franja horaria e intervalo antes de agendar citas con este especialista.</div>`;
  }

  return `
    <div class="detail-box"><span>Días de atención</span><strong>${formatDays(disponibilidad.diasAtencion)}</strong></div>
    <div class="detail-grid">
      <div class="detail-box"><span>Hora inicio</span><strong>${formatTime(disponibilidad.horaInicio)}</strong></div>
      <div class="detail-box"><span>Hora fin</span><strong>${formatTime(disponibilidad.horaFin)}</strong></div>
    </div>
    <div class="detail-grid">
      <div class="detail-box"><span>Intervalo</span><strong>${escapeHtml(disponibilidad.intervaloMinutos || "-")} minutos</strong></div>
      <div class="detail-box"><span>Ventana</span><strong>${escapeHtml(disponibilidad.semanasHabilitadas || "-")} semanas</strong></div>
    </div>
  `;
}

function uniqueSpecialtiesOptions(selected = "", { generalOnly = false } = {}) {
  const values = generalOnly
    ? [GENERAL_SPECIALTY]
    : SPECIALTY_ORDER;
  return [`<option value="">Seleccione...</option>`, ...values.map(v => `<option value="${escapeHtml(v)}" ${selected === v ? "selected" : ""}>${formatSpecialty(v)}</option>`)].join("");
}

function setActiveNav(route) {
  document.querySelectorAll(".nav-link").forEach(link => link.classList.toggle("active", link.dataset.route === route));
}

function showModal(html) {
  modalRoot.innerHTML = html;
  modalRoot.classList.remove("hidden");
  modalRoot.setAttribute("aria-hidden", "false");
  modalRoot.querySelectorAll("[data-modal-close]").forEach(btn => btn.addEventListener("click", closeModal));
}
function closeModal() {
  modalRoot.classList.add("hidden");
  modalRoot.setAttribute("aria-hidden", "true");
  modalRoot.innerHTML = "";
}

async function openEspecialistaDetailModal(especialistaId) {
  const especialista = state.especialistas.find(e => e.id === especialistaId);
  if (!especialista) {
    showAlert("Seleccione un especialista para ver el detalle.", "error");
    return;
  }

  showModal(`
    <div class="modal modal-scroll">
      <div class="modal-header"><h3>Detalle del especialista</h3><button class="modal-close" data-modal-close>×</button></div>
      <div class="modal-body"><div class="empty-state"><strong>Cargando disponibilidad...</strong></div></div>
      <div class="modal-footer"><button class="btn" data-modal-close>Cerrar</button></div>
    </div>
  `);

  const disponibilidad = await loadDisponibilidadForEspecialista(especialistaId);
  showModal(`
    <div class="modal">
      <div class="modal-header"><h3>Detalle del especialista</h3><button class="modal-close" data-modal-close>×</button></div>
      <div class="modal-body">
        <div class="detail-box"><span>Nombre</span><strong>${escapeHtml(especialista.nombre)}</strong></div>
        <div class="detail-box"><span>Especialidad</span><strong>${formatSpecialty(especialista.especialidad)}</strong></div>
        ${availabilityHtml(disponibilidad)}
      </div>
      <div class="modal-footer"><button class="btn" data-modal-close>Cerrar</button></div>
    </div>
  `);
}


function renderLogin() {
  viewRoot.innerHTML = `
    ${pageHeader("Iniciar sesión", "Ingrese con un usuario registrado")}
    <section class="panel auth-panel">
      <form id="loginForm">
        <div class="field"><label class="required">Documento</label><input name="id" type="number" required /></div>
        <div class="field"><label class="required">Contraseña</label><input name="passwordHash" type="password" required /></div>
        <div class="actions"><button class="btn btn-primary">Entrar</button><a class="btn" href="#/registro">Crear cuenta</a></div>
      </form>
    </section>
  `;
  $("#loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = { id: Number(form.get("id")), passwordHash: String(form.get("passwordHash")) };
    try {
      const response = await api.login(payload);
      if (!response.autenticado) {
        showAlert(response.mensaje || "Credenciales inválidas", "error");
        return;
      }
      state.user = {
        ...response,
        nombre: response.nombreUsuario || response.nombre || "",
        apellido: response.apellidoUsuario || response.apellido || ""
      };

      saveUser();

      await Promise.all([
        loadEspecialistas({ silent: true }),
        loadCitas({ silent: true })
      ]);

      location.hash = `#/${getDefaultRouteByRole()}`;
    } catch (error) {
      showAlert(`No se pudo iniciar sesión: ${error.message}`, "error");
    }
  });
}

function renderRegistro() {
  viewRoot.innerHTML = `
    ${pageHeader("Registro de paciente", "Cree una cuenta de paciente para usar el sistema")}
    <section class="panel auth-panel">
      <form id="registerForm">
        <div class="grid-2">
          <div class="field"><label class="required">Documento</label><input name="id" type="number" min="1" placeholder="Ej: 1054123456" required /><small class="hint">Ingrese solo números, sin puntos ni espacios.</small></div>
          <div class="field"><label class="required">Nombre</label><input name="nombre" placeholder="Ej: Maria Camila" minlength="2" maxlength="60" required /><small class="hint">Use solo letras.</small></div>
          <div class="field"><label class="required">Apellido</label><input name="apellido" placeholder="Ej: Perez Gomez" minlength="2" maxlength="60" required /><small class="hint">Use solo letras. Escriba ambos apellidos si aplica.</small></div>
          <div class="field"><label class="required">Contraseña</label><input name="passwordHash" type="password" minlength="6" placeholder="Mínimo 6 caracteres" required /></div>
          <div class="field"><label>Género</label><select name="genero"><option value="MASCULINO">Masculino</option><option value="FEMENINO">Femenino</option><option value="OTRO">Otro</option></select></div>
          <div class="field"><label class="required">Celular</label><input name="telefono" inputmode="numeric" pattern="[0-9]{10}" maxlength="10" placeholder="Ej: 3001234567" required /><small class="hint">Ingrese exactamente 10 números.</small></div>
          <div class="field"><label>Fecha nacimiento</label><input name="fechaNacimiento" type="date" value="1998-01-01" /></div>
        </div>
        <div class="field"><label>Correo electrónico</label><input name="correo" type="email" placeholder="Ej: paciente@correo.com" /></div>
        <div class="actions"><button class="btn btn-primary">Registrarme</button><a class="btn" href="#/login">Volver al login</a></div>
      </form>
    </section>
  `;
  ["nombre", "apellido"].forEach(field => {
    $("#registerForm").elements[field].addEventListener("blur", event => {
      event.target.value = normalizePersonName(event.target.value);
    });
  });
  $("#registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const formEl = event.currentTarget;
    const nombre = normalizeFormName(formEl, "nombre");
    const apellido = normalizeFormName(formEl, "apellido");
    if (!isValidPersonName(nombre) || !isValidPersonName(apellido)) {
      showAlert("Revise nombres y apellidos: use solo letras y espacios.", "error");
      return;
    }
    const f = new FormData(event.currentTarget);
    const payload = Object.fromEntries(f.entries());
    payload.id = Number(payload.id);
    payload.rol = "PACIENTE";
    try {
      const response = await api.register(payload);
      if (!response.autenticado) {
        showAlert(response.mensaje || "No se pudo registrar el usuario", "error");
        return;
      }
      showAlert("Usuario registrado correctamente. Ya puede iniciar sesión.", "success");
      location.hash = "#/login";
    } catch (error) {
      showAlert(`No se pudo registrar: ${error.message}`, "error");
    }
  });
}

function logout() {
  state.user = null;
  saveUser();
  state.especialistas = [];
  state.citas = [];
  location.hash = "#/login";
}

function renderInicio() {
  const role = getCurrentRole();

  if (role === "PACIENTE") {
    viewRoot.innerHTML = `
      ${pageHeader("Portal del Paciente", "Gestione sus citas médicas")}
      <section class="panel">
        <h4>Bienvenido</h4>
        <p class="muted">
          Sesión activa: <strong>Paciente</strong>.
          Desde este portal puede agendar una nueva cita y consultar sus citas registradas.
        </p>
      </section>

      <h3>Accesos rápidos</h3>
      <div class="quick-grid">
        ${quickCard("#/portal-agendar", "▣", "Agendar mi cita", "Seleccione especialista, fecha y horario")}
        ${quickCard("#/mis-citas", "♙", "Mis citas", "Consulte y cancele sus citas agendadas", "purple")}
      </div>
    `;

    return;
  }

  viewRoot.innerHTML = `
    ${pageHeader("Gestión de Citas Médicas", "Sistema Piedrazul - Módulo de administración de citas")}
    <section class="panel">
      <h4>Bienvenido</h4>
      <p class="muted">
        Sesión activa: <strong>${escapeHtml(state.user?.rolUsuario || "Usuario")}</strong>.
        Utilice el menú lateral o los accesos rápidos para gestionar las citas del centro médico.
      </p>
    </section>

    <h3>Accesos rápidos</h3>
    <div class="quick-grid">
      ${quickCard("#/agendar-cita", "▣", "Nueva cita", "Agendar una cita para un paciente")}
      ${quickCard("#/consultar-citas", "▤", "Consultar citas", "Buscar y ver citas agendadas", "green")}
      ${quickCard("#", "⇩", "Exportar citas", "Generar reporte por fecha y especialista", "orange", "export-card")}
      ${quickCard("#/consultar-citas", "◷", "Reagendar", "Modificar fecha y hora de una cita", "purple")}
      ${quickCard("#/registrar-medico", "♙", "Registrar médico", "Gestionar médicos y terapeutas")}
      ${quickCard("#/dias-atencion", "▣", "Disponibilidad", "Días, franja horaria, intervalo y ventana", "green")}
    </div>
  `;

  $("#export-card")?.addEventListener("click", event => {
    event.preventDefault();
    openExportCitasModal();
  });
}

function quickCard(href, icon, title, text, color = "", id = "") {
  return `<a ${id ? `id="${id}"` : ""} class="quick-card" href="${href}"><span class="icon-box ${color}">${icon}</span><span><strong>${title}</strong><p>${text}</p></span></a>`;
}

function renderRegistrarMedico() {
  if (getCurrentRole() !== "AGENDADOR") {
    showAlert("No tiene permisos para registrar médicos.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }
  viewRoot.innerHTML = `
    ${pageHeader("Registrar Médico/Terapeuta", "Gestione la información de los profesionales de la salud")}
    <section class="panel">
      <h3>Datos del profesional</h3>
      <form id="doctorForm">
        <div class="grid-2">
          <div class="field"><label class="required">Tipo de documento</label><select name="tipoDocumento" required><option value="">Seleccione...</option><option>CC</option><option>CE</option></select></div>
          <div class="field"><label class="required">Número de documento</label><input name="id" placeholder="Ej: 1234567890" required /></div>
          <div class="field"><label class="required">Nombres</label><input name="nombres" placeholder="Ej: Juan Carlos" minlength="2" maxlength="60" required /><small class="hint">Use solo letras y espacios.</small></div>
          <div class="field"><label class="required">Apellidos</label><input name="apellidos" placeholder="Ej: Gomez Perez" minlength="2" maxlength="60" required /><small class="hint">Use solo letras y espacios.</small></div>
          <div class="field"><label class="required">Especialidad</label><select name="especialidad" required>${uniqueSpecialtiesOptions()}</select></div>
        </div>
        <div class="actions"><button class="btn btn-primary">Guardar</button><button class="btn" type="reset">Limpiar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel">
      <h3>Listado de médicos/terapeutas registrados</h3>
      <div id="doctorsTable"></div>
    </section>
  `;
  renderDoctorsTable();
  ["nombres", "apellidos"].forEach(field => {
    $("#doctorForm").elements[field].addEventListener("blur", event => {
      event.target.value = normalizePersonName(event.target.value);
    });
  });
  $("#doctorForm").addEventListener("submit", handleDoctorSubmit);
}

function renderDoctorsTable() {
  const rows = state.especialistas.map(e => `<tr><td>CC ${escapeHtml(e.id)}</td><td>${escapeHtml(e.nombre)}</td><td>${formatSpecialty(e.especialidad)}</td><td>${statusBadge("Activo")}</td><td><button class="btn btn-primary btn-small" data-specialist-detail="${escapeHtml(e.id)}">Ver detalle</button></td></tr>`).join("");
  $("#doctorsTable").innerHTML = rows ? `<div class="table-wrap"><table><thead><tr><th>Documento</th><th>Nombre completo</th><th>Especialidad</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyState("No hay médicos registrados");
  document.querySelectorAll("[data-specialist-detail]").forEach(btn => {
    btn.addEventListener("click", () => openEspecialistaDetailModal(btn.dataset.specialistDetail));
  });
}

async function handleDoctorSubmit(event) {
  event.preventDefault();
  const formEl = event.currentTarget;
  const nombres = normalizeFormName(formEl, "nombres");
  const apellidos = normalizeFormName(formEl, "apellidos");
  if (!isValidPersonName(nombres) || !isValidPersonName(apellidos)) {
    showAlert("Revise nombres y apellidos: use solo letras y espacios.", "error");
    return;
  }
  const form = new FormData(event.currentTarget);
  const payload = {
    id: `${form.get("tipoDocumento") || "CC"}-${form.get("id")}`.replace(/^CC-/, ""),
    nombre: `${nombres} ${apellidos}`.trim(),
    especialidad: form.get("especialidad")
  };
  try {
    await api.createEspecialista(payload);
    showAlert("Especialista creado correctamente.", "success");
    await loadEspecialistas({ silent: true });
  } catch (error) {
    showAlert(`No se pudo guardar en especialista-service: ${error.message}`, "error");
  }
  renderRegistrarMedico();
}

async function loadDisponibilidadForEspecialista(especialistaId) {
  if (!especialistaId) return null;

  try {
    return await api.getDisponibilidad(especialistaId);
  } catch (error) {
    return null;
  }
}

function normalizeDiasAtencion(disponibilidad) {
  if (!disponibilidad || !Array.isArray(disponibilidad.diasAtencion)) {
    return [];
  }

  return disponibilidad.diasAtencion
    .map(dia => String(dia).toUpperCase())
    .filter(dia => DAY_LABELS[dia]);
}

function paintDiasAtencion(diasAtencion) {
  const selectedDays = new Set(diasAtencion);

  document.querySelectorAll("input[name='dias']").forEach(input => {
    input.checked = selectedDays.has(input.value);
  });

  const labels = diasAtencion.map(dia => DAY_LABELS[dia]).filter(Boolean);
  $("#daysPreview").textContent = labels.join(", ") || "Sin días configurados";
}

async function refreshDiasAtencionForm(especialistaId) {
  const disponibilidad = await loadDisponibilidadForEspecialista(especialistaId);
  const diasGuardados = normalizeDiasAtencion(disponibilidad);
  paintDiasAtencion(diasGuardados);

  if ($("#availabilityStart")) $("#availabilityStart").value = disponibilidad?.horaInicio || state.settings.horaInicio;
  if ($("#availabilityEnd")) $("#availabilityEnd").value = disponibilidad?.horaFin || state.settings.horaFin;
  if ($("#availabilityInterval")) $("#availabilityInterval").value = disponibilidad?.intervaloMinutos || state.settings.intervaloMinutos;
  if ($("#availabilityWeeks")) $("#availabilityWeeks").value = disponibilidad?.semanasHabilitadas || state.settings.semanasHabilitadas;
}

async function renderDiasAtencion() {
  const selectedEspecialistaId = state.especialistas[0]?.id || "";

  viewRoot.innerHTML = `
    ${pageHeader("Configurar Disponibilidad", "Defina especialista, días, franja horaria, intervalo y ventana de agendamiento")}
    <section class="panel">
      <form id="daysForm">
        <h3>Selección de profesional</h3>
        <div class="field">
          <label class="required">Médico/Terapeuta</label>
          <select name="especialistaId" id="daysEspecialista" required>
            ${specialistOptions(selectedEspecialistaId)}
          </select>
        </div>

        <h3>Días de atención</h3>

        <div class="field">
          <label class="required">Seleccione los días</label>
          <div class="check-grid">
            ${DAY_OPTIONS.map(([value, label]) => `
              <label class="check-card">
                <input type="checkbox" name="dias" value="${value}" />
                ${label}
              </label>
            `).join("")}
          </div>
        </div>

        <div class="current-box">
          <strong>Configuración actual:</strong><br />
          <span id="daysPreview">Cargando configuración...</span>
        </div>

        <h3>Horario diario</h3>
        <div class="grid-4">
          <div class="field"><label class="required">Hora inicio</label><input id="availabilityStart" name="horaInicio" type="time" value="${state.settings.horaInicio}" required /></div>
          <div class="field"><label class="required">Hora fin</label><input id="availabilityEnd" name="horaFin" type="time" value="${state.settings.horaFin}" required /></div>
          <div class="field"><label class="required">Intervalo (minutos)</label><input id="availabilityInterval" name="intervaloMinutos" type="number" min="10" step="5" value="${state.settings.intervaloMinutos}" required /></div>
          <div class="field"><label class="required">Semanas habilitadas</label><input id="availabilityWeeks" name="semanasHabilitadas" type="number" min="1" max="52" value="${state.settings.semanasHabilitadas}" required /></div>
        </div>
        <p class="hint">El sistema solo ofrecerá horarios dentro de esta franja y con el intervalo configurado.</p>

        <div class="actions">
          <button class="btn btn-primary">Guardar disponibilidad</button>
          <a class="btn" href="#/inicio">Volver</a>
        </div>
      </form>
    </section>
  `;

  const updatePreview = () => {
    const days = [...document.querySelectorAll("input[name='dias']:checked")]
      .map(input => DAY_LABELS[input.value]);

    $("#daysPreview").textContent = days.join(", ") || "Sin días seleccionados";
  };

  document.querySelectorAll("input[name='dias']").forEach(input => {
    input.addEventListener("change", updatePreview);
  });

  $("#daysEspecialista").addEventListener("change", event => {
    refreshDiasAtencionForm(event.target.value);
  });

  $("#daysForm").addEventListener("submit", handleDaysSubmit);

  await refreshDiasAtencionForm(selectedEspecialistaId);
}

async function handleDaysSubmit(event) {
  event.preventDefault();

  const form = new FormData(event.currentTarget);
  const especialistaId = form.get("especialistaId");
  const diasAtencion = form.getAll("dias");
  const horaInicio = String(form.get("horaInicio"));
  const horaFin = String(form.get("horaFin"));
  const intervaloMinutos = Number(form.get("intervaloMinutos"));
  const semanasHabilitadas = Number(form.get("semanasHabilitadas"));

  if (!diasAtencion.length) {
    showAlert("Debe seleccionar al menos un día de atención.", "error");
    return;
  }

  if (!horaInicio || !horaFin || horaInicio >= horaFin) {
    showAlert("La hora de inicio debe ser menor que la hora de fin.", "error");
    return;
  }

  if (intervaloMinutos < 10 || semanasHabilitadas < 1) {
    showAlert("Revise intervalo y semanas habilitadas.", "error");
    return;
  }

  const payload = {
    diasAtencion,
    horaInicio,
    horaFin,
    intervaloMinutos,
    semanasHabilitadas
  };

  state.settings = { ...state.settings, horaInicio, horaFin, intervaloMinutos, semanasHabilitadas };
  saveSettings();

  try {
    await api.saveDisponibilidad(especialistaId, payload);
    showAlert("Disponibilidad guardada correctamente.", "success");

    await refreshDiasAtencionForm(especialistaId);
  } catch (error) {
    showAlert(`No se pudo guardar en especialista-service: ${error.message}`, "error");
  }
}
function renderIntervaloCitas() {
  viewRoot.innerHTML = `
    ${pageHeader("Configurar Horario e Intervalo", "Defina la franja diaria y el tiempo entre cada cita del profesional")}
    <section class="panel">
      <form id="intervalForm">
        <h3>Configuración del horario diario</h3>
        <div class="field"><label class="required">Médico/Terapeuta</label><select name="especialistaId" required>${specialistOptions(state.especialistas[0]?.id)}</select></div>
        <div class="grid-3">
          <div class="field"><label class="required">Hora inicio</label><input name="horaInicio" type="time" value="${state.settings.horaInicio}" required /></div>
          <div class="field"><label class="required">Hora fin</label><input name="horaFin" type="time" value="${state.settings.horaFin}" required /></div>
          <div class="field"><label class="required">Intervalo entre citas (minutos)</label><input name="intervalo" type="number" min="10" step="5" value="${state.settings.intervaloMinutos}" required /></div>
        </div>
        <div class="current-box"><strong>Configuración actual:</strong><br />${state.settings.horaInicio} a ${state.settings.horaFin}, cada ${state.settings.intervaloMinutos} minutos</div>
        <div class="warning-box"><strong>Nota:</strong> Este intervalo determina el tiempo mínimo entre citas consecutivas. Por ejemplo, si configura 30 minutos y una cita es a las 09:00 AM, la siguiente cita disponible será a las 09:30 AM.</div>
        <div class="actions"><button class="btn btn-primary">Guardar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel"><h3>Horarios configurados</h3><div class="table-wrap"><table><thead><tr><th>Profesional</th><th>Franja</th><th>Intervalo</th></tr></thead><tbody>${state.especialistas.slice(0, 5).map(e => `<tr><td>${escapeHtml(e.nombre)} - ${formatSpecialty(e.especialidad)}</td><td>${state.settings.horaInicio} - ${state.settings.horaFin}</td><td>${state.settings.intervaloMinutos} minutos</td></tr>`).join("")}</tbody></table></div></section>
  `;
  $("#intervalForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const horaInicio = String(form.get("horaInicio"));
    const horaFin = String(form.get("horaFin"));
    if (!horaInicio || !horaFin || horaInicio >= horaFin) {
      showAlert("La hora de inicio debe ser menor que la hora de fin.", "error");
      return;
    }
    state.settings.horaInicio = horaInicio;
    state.settings.horaFin = horaFin;
    state.settings.intervaloMinutos = Number(form.get("intervalo"));
    saveSettings();
    try {
      await syncAvailability(form.get("especialistaId"));
      showAlert("Intervalo actualizado en el backend.", "success");
    } catch (error) {
      showAlert(`No se pudo actualizar el intervalo en especialista-service: ${error.message}`, "error");
    }
    renderIntervaloCitas();
  });
}

function renderVentanaAgendamiento() {
  viewRoot.innerHTML = `
    ${pageHeader("Configurar Ventana de Agendamiento", "Defina el rango de semanas futuras permitidas para agendar citas")}
    <section class="panel">
      <form id="windowForm">
        <h3>Configuración de ventana temporal</h3>
        <div class="field"><label class="required">Número de semanas para agendamiento</label><input name="semanas" type="number" min="1" max="52" value="${state.settings.semanasHabilitadas}" required /></div>
        <div class="info-box"><strong>Vista previa:</strong><br />Los pacientes podrán agendar citas desde hoy hasta ${state.settings.semanasHabilitadas} semanas en el futuro.</div>
        <div class="warning-box"><strong>¿Qué es la ventana de agendamiento?</strong><br /><br />Es el periodo máximo futuro en el que se permiten agendar citas. Si configura <strong>4 semanas</strong>, los pacientes solo podrán agendar citas en los próximos 28 días. Si configura <strong>8 semanas</strong>, el rango será de 56 días.</div>
        <div class="note"><strong>Configuración actual:</strong> ${state.settings.semanasHabilitadas} semanas</div>
        <div class="actions"><button class="btn btn-primary">Guardar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel"><h3>Información adicional</h3><ul><li>Esta configuración es global y aplica para todos los profesionales y especialidades.</li><li>El sistema bloqueará automáticamente fechas fuera del rango configurado.</li><li>Se recomienda configurar un valor entre 2 y 8 semanas para un equilibrio adecuado.</li><li>Los cambios aplican de inmediato en el módulo de agendamiento.</li></ul></section>
  `;
  $("#windowForm").addEventListener("submit", async event => {
    event.preventDefault();
    state.settings.semanasHabilitadas = Number(new FormData(event.currentTarget).get("semanas"));
    saveSettings();
    try {
      await Promise.all(state.especialistas.map(e => syncAvailability(e.id)));
      showAlert("Ventana de agendamiento actualizada en el backend.", "success");
    } catch (error) {
      showAlert(`No se pudo sincronizar la ventana con especialista-service: ${error.message}`, "error");
    }
    renderVentanaAgendamiento();
  });
}

async function syncAvailability(especialistaId) {
  if (!especialistaId) return;
  let actual = null;
  try { actual = await api.getDisponibilidad(especialistaId); } catch (_) { actual = null; }
  const payload = {
    diasAtencion: actual?.diasAtencion?.length ? actual.diasAtencion : ["MONDAY", "WEDNESDAY", "FRIDAY"],
    horaInicio: actual?.horaInicio || state.settings.horaInicio,
    horaFin: actual?.horaFin || state.settings.horaFin,
    intervaloMinutos: Number(state.settings.intervaloMinutos),
    semanasHabilitadas: Number(state.settings.semanasHabilitadas)
  };
  await api.saveDisponibilidad(especialistaId, payload);
}

function renderPortalAgendar() {
  if (getCurrentRole() !== "PACIENTE") {
    showAlert("Esta sección es exclusiva para pacientes.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }

  const patientId = getCurrentPatientId();
  if (hasBlockingAppointment(patientId)) {
    viewRoot.innerHTML = `
      ${pageHeader("Agendar Mi Cita", "Seleccione especialista, fecha y un horario disponible")}
      <section class="panel">
        <div class="warning-box"><strong>No puede agendar otra cita por ahora.</strong><br />Ya tiene una cita en estado Agendada, Pendiente o Reagendada. Primero debe cancelar o cerrar esa cita.</div>
        <div class="actions"><a class="btn btn-primary" href="#/mis-citas">Ver mis citas</a></div>
      </section>
    `;
    return;
  }

  const firstAppointment = !hasAnyAppointment(patientId);
  const minimumDate = tomorrowIso();

  viewRoot.innerHTML = `
    ${pageHeader("Agendar Mi Cita", "Seleccione especialista, fecha y un horario disponible")}
    <form id="portalAppointmentForm">
      <section class="panel">
        <h3>Seleccionar especialista</h3>
        <div class="grid-2">
          <div class="field"><label class="required">Médico/Terapeuta</label><select name="especialistaId" id="portalDoctor" required>${specialistOptions("", { generalOnly: firstAppointment })}</select>${firstAppointment ? `<small class="hint">Para la primera cita solo se muestran profesionales de medicina general.</small>` : ""}</div>
          <div class="field"><label class="required">Fecha</label><input name="fecha" id="portalDate" type="date" min="${minimumDate}" value="${minimumDate}" required /></div>
        </div>
        <div class="actions"><button type="button" class="btn" id="portalSpecialistDetail">Ver detalle del especialista</button></div>
      </section>
      <section class="panel" id="slotPanel"><div class="empty-state"><strong>Seleccione especialista y fecha para ver los horarios disponibles</strong></div></section>
      <input type="hidden" name="hora" id="portalSelectedTime" />
    </form>
  `;
  $("#portalDoctor").addEventListener("change", updateSlots);
  $("#portalDate").addEventListener("change", updateSlots);
  $("#portalSpecialistDetail").addEventListener("click", () => openEspecialistaDetailModal($("#portalDoctor").value));
  updateSlots();
  $("#portalAppointmentForm").addEventListener("submit", submitPortalAppointment);
}

function bindPortalSlotEvents() {
  const slotPanel = $("#slotPanel");
  slotPanel.onchange = event => {
    if (event.target.matches("[data-portal-slot]")) {
      const slot = state.portalSlots[Number(event.target.dataset.portalSlot)];
      showSlotSummary(slot);
    }
  };

  slotPanel.onclick = event => {
    const cancelButton = event.target.closest("#cancelSlotSelection");

    if (cancelButton) {
      event.preventDefault();
      updateSlots();
    }
  };
}

function getSelectedPortalSlot() {
  if (state.selectedPortalSlot) {
    return state.selectedPortalSlot;
  }

  const selectedInput = document.querySelector("[data-portal-slot]:checked");
  if (!selectedInput) {
    return null;
  }

  return state.portalSlots[Number(selectedInput.dataset.portalSlot)] || null;
}

async function updateSlots() {
  const doctorId = $("#portalDoctor").value;
  const date = $("#portalDate").value;
  state.portalSlots = [];
  state.selectedPortalSlot = null;
  $("#portalSelectedTime").value = "";
  if (!doctorId || !date) {
    $("#slotPanel").innerHTML = `<div class="empty-state"><strong>Seleccione especialista y fecha para ver los horarios disponibles</strong></div>`;
    return;
  }

  if (date <= todayIso()) {
    $("#slotPanel").innerHTML = `<div class="warning-box"><strong>Seleccione una fecha posterior a hoy.</strong><br />No se pueden agendar citas para el mismo dia.</div>`;
    return;
  }

  const doctor = state.especialistas.find(e => e.id === doctorId);
  let horarios = [];
  try {
    const result = await api.getHorarios(doctorId, date);
    horarios = Array.isArray(result) ? result : [];
  } catch (error) {
    $("#slotPanel").innerHTML = emptyState("No se pudieron consultar horarios disponibles");
    showAlert(`No se pudieron consultar horarios: ${error.message}`, "error");
    return;
  }
  const slots = horarios.map(hora => ({ hora: String(hora).slice(0, 5), fecha: date, doctor })).filter(slot => slot.doctor);
  state.portalSlots = slots;
  $("#slotPanel").innerHTML = `
    <h3>Horarios disponibles</h3>
    <p class="muted">Estos horarios están disponibles según la disponibilidad del profesional, días festivos y horarios de atención.</p>
    <div class="slot-list" id="slotList">
      ${slots.map((slot, idx) => `<label class="slot-option"><input type="radio" name="slot" value="${idx}" data-portal-slot="${idx}" /><span><strong>${formatDate(slot.fecha)}</strong>${formatTime(slot.hora)} - ${escapeHtml(slot.doctor.nombre)}</span></label>`).join("") || emptyState("No hay horarios disponibles")}
    </div>
    <div id="slotSummary">
      ${slots.length ? `<div class="actions"><button type="submit" class="btn btn-primary" id="confirmPortalAppointment">Agendar cita</button></div>` : ""}
    </div>
  `;
  bindPortalSlotEvents();
}

function showSlotSummary(slot) {
  if (!slot) return;
  state.selectedPortalSlot = slot;
  $("#portalSelectedTime").value = slot.hora;
  document.querySelectorAll(".slot-option").forEach(label => label.classList.toggle("selected", label.querySelector("input").checked));
  $("#slotSummary").innerHTML = `
    <section class="panel" style="margin-top:18px;">
      <h3>Resumen de su cita</h3>
      <table class="summary-table"><tbody>
        <tr><td>Especialidad:</td><td>${formatSpecialty(slot.doctor.especialidad)}</td></tr>
        <tr><td>Profesional:</td><td>${escapeHtml(slot.doctor.nombre)}</td></tr>
        <tr><td>Fecha:</td><td>${formatDate(slot.fecha)}</td></tr>
        <tr><td>Hora:</td><td>${formatTime(slot.hora)}</td></tr>
      </tbody></table>
      <div class="actions"><button type="submit" class="btn btn-primary" id="confirmPortalAppointment">Agendar cita</button><button type="button" class="btn" id="cancelSlotSelection">Cancelar selección</button></div>
    </section>
  `;
}

async function submitPortalAppointment(event) {
  event.preventDefault();

  if (!$("#portalSelectedTime").value) {
    showAlert("Seleccione un horario disponible antes de agendar.", "error");
    return;
  }

  await createPatientAppointment(getSelectedPortalSlot(), $("#confirmPortalAppointment"), event.currentTarget);
}

async function createPatientAppointment(slot, button, formEl = $("#portalAppointmentForm")) {
  const slotPanel = $("#slotPanel");
  if (!slot) {
    showAlert("Seleccione un horario disponible antes de agendar.", "error");
    return;
  }

  const patientId = getCurrentPatientId();
  if (!patientId) {
    showAlert("No se pudo identificar el paciente de la sesión.", "error");
    return;
  }

  if (hasBlockingAppointment(patientId)) {
    showAlert("Ya tiene una cita agendada o pendiente.", "error");
    return;
  }

  if (slot.fecha <= todayIso()) {
    showAlert("No se pueden agendar citas para el mismo dia.", "error");
    return;
  }

  if (!hasAnyAppointment(patientId) && !isGeneralSpecialty(slot.doctor.especialidad)) {
    showAlert("La primera cita debe ser con medicina general.", "error");
    return;
  }

  const form = new FormData(formEl);
  const payload = Object.fromEntries(form.entries());
  payload.pacienteId = patientId;
  payload.especialistaId = String(payload.especialistaId || slot.doctor.id);
  payload.fecha = String(payload.fecha || slot.fecha);
  payload.hora = String(payload.hora || slot.hora).slice(0, 5);

  try {
    if (button) {
      button.disabled = true;
      button.textContent = "Agendando...";
    }

    slotPanel?.setAttribute("aria-busy", "true");
    const citaCreada = await api.agendarPaciente(payload);
    const citaParaVista = citaCreada && typeof citaCreada === "object"
      ? {
          ...citaCreada,
          pacienteId: Number(citaCreada.pacienteId || patientId),
          pacienteNombre: citaCreada.pacienteNombre || getUserDisplayName()
        }
      : {
          id: `temp-${Date.now()}`,
          pacienteId,
          pacienteNombre: getUserDisplayName(),
          especialistaId: slot.doctor.id,
          especialistaNombre: slot.doctor.nombre,
          especialistaEspecialidad: slot.doctor.especialidad,
          fecha: slot.fecha,
          hora: slot.hora,
          estado: "AGENDADA"
        };

    state.citas = [
      ...state.citas.filter(cita => cita.id !== citaParaVista.id),
      citaParaVista
    ];

    showAlert("Cita agendada correctamente.", "success");

    if (slotPanel) {
      slotPanel.innerHTML = `
        <div class="info-box">
          <strong>Cita agendada correctamente.</strong><br />
          ${formatDate(slot.fecha)} - ${formatTime(slot.hora)} con ${escapeHtml(slot.doctor.nombre)}
        </div>
        <div class="actions"><a class="btn btn-primary" href="#/mis-citas">Ver mis citas</a></div>
      `;
    }

    await loadCitas({ silent: true });
    state.portalSlots = [];
    state.selectedPortalSlot = null;

    location.hash = "#/mis-citas";
    state.currentRoute = "mis-citas";
    updateMenuByRole();
    setActiveNav("mis-citas");
    renderMisCitas();
  } catch (error) {
    const message = error?.message || "Error desconocido";
    showAlert(`No se pudo agendar la cita: ${message}`, "error");

    if (slotPanel) {
      const summary = $("#slotSummary");
      if (summary) {
        summary.insertAdjacentHTML("beforeend", `<div class="warning-box"><strong>No se pudo agendar la cita.</strong><br />${escapeHtml(message)}</div>`);
      }
    }
  } finally {
    slotPanel?.removeAttribute("aria-busy");
    if (button) {
      button.disabled = false;
      button.textContent = "Agendar cita";
    }
  }
}

function renderMisCitas() {
  if (getCurrentRole() !== "PACIENTE") {
    showAlert("Esta sección es exclusiva para pacientes.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }

  const patientId = getCurrentPatientId();
  const citas = state.citas.filter(c => Number(c.pacienteId) === patientId);

  viewRoot.innerHTML = `
    ${pageHeader("Mis Citas", "Consulte y gestione sus citas médicas agendadas")}
    <section class="panel"><h3>Filtrar por estado</h3><div class="filter-pills"><button class="btn btn-small active" data-status="TODAS">Todas</button><button class="btn btn-small" data-status="AGENDADA">Agendadas</button><button class="btn btn-small" data-status="PENDIENTE">Pendientes</button><button class="btn btn-small" data-status="CANCELADA">Canceladas</button><button class="btn btn-small" data-status="ASISTIDA">Asistidas</button><button class="btn btn-small" data-status="NO_ASISTIDA">No asistidas</button></div></section>
    <section class="panel"><h3>Mis citas (${citas.length})</h3><div id="patientAppointments"></div></section>
  `;
  const render = status => renderAppointmentCards(citas.filter(c => status === "TODAS" || String(c.estado).toUpperCase() === status), "#patientAppointments", true);
  render("TODAS");
  document.querySelectorAll("[data-status]").forEach(btn => btn.addEventListener("click", () => {
    document.querySelectorAll("[data-status]").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    render(btn.dataset.status);
  }));
}

function renderAppointmentCards(citas, target, patientActions = false) {
  $(target).innerHTML = citas.length ? `<div class="appointment-list">${citas.map(citaCard(patientActions)).join("")}</div>` : emptyState("No hay citas para mostrar");
  document.querySelectorAll("[data-detail]").forEach(btn => btn.addEventListener("click", () => openDetailModal(state.citas.find(c => c.id === btn.dataset.detail))));
  document.querySelectorAll("[data-cancel]").forEach(btn => btn.addEventListener("click", () => openCancelModal(state.citas.find(c => c.id === btn.dataset.cancel))));
}

function citaCard(patientActions) {
  return c => {
    const specialist = state.especialistas.find(e => e.id === c.especialistaId);
    const specialty = specialist?.especialidad || c.especialistaEspecialidad || c.tipoAtencion || "Consulta médica";
    const canCancel = canCancelAppointment(c);
    return `<article class="appointment-card"><header><h4>${formatSpecialty(specialty)}</h4>${statusBadge(c.estado)}</header><p class="muted">${escapeHtml(c.especialistaNombre || specialist?.nombre || "Profesional")}</p><div class="appointment-meta"><div><span>Fecha</span><strong>${formatDate(c.fecha)}</strong></div><div><span>Hora</span><strong>${formatTime(c.hora)}</strong></div><div><span>Tipo</span><strong>Consulta médica</strong></div></div><div class="actions"><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button>${patientActions && canCancel ? `<button class="btn btn-danger btn-small" data-cancel="${escapeHtml(c.id)}">Cancelar cita</button>` : ""}</div></article>`;
  };
}

function appointmentPatientFallback(c) {
  const parsed = splitFullName(c?.pacienteNombre);
  return {
    id: c?.pacienteId,
    nombre: c?.pacienteApellido ? c?.pacienteNombre : parsed.nombre,
    apellido: c?.pacienteApellido || parsed.apellido,
    telefono: c?.pacienteTelefono || "",
    fechaNacimiento: c?.pacienteFechaNacimiento || "",
    genero: c?.pacienteGenero || "",
    correo: c?.pacienteCorreo || ""
  };
}

async function loadAppointmentPatientInfo(c) {
  const fallback = appointmentPatientFallback(c);

  try {
    const paciente = await api.getPaciente(c.pacienteId);
    return {
      ...fallback,
      ...paciente,
      nombre: paciente.nombre || fallback.nombre,
      apellido: paciente.apellido || fallback.apellido
    };
  } catch (_) {
    return fallback;
  }
}

function legacyPatientInfoHtml(paciente, { loading = false } = {}) {
  const nombre = fullName(paciente.nombre, paciente.apellido) || `Paciente ${paciente.id || "-"}`;
  return `
    <h4>InformaciÃ³n del paciente</h4>
    ${loading ? `<p class="hint">Consultando datos registrados del paciente...</p>` : ""}
    <div class="detail-grid">
      <div class="detail-box"><span>Documento</span><strong>${escapeHtml(paciente.id || "-")}</strong></div>
      <div class="detail-box"><span>Nombre completo</span><strong>${escapeHtml(nombre)}</strong></div>
    </div>
    <div class="detail-grid">
      <div class="detail-box"><span>TelÃ©fono</span><strong>${escapeHtml(paciente.telefono || "-")}</strong></div>
      <div class="detail-box"><span>Fecha nacimiento</span><strong>${formatDate(paciente.fechaNacimiento)}</strong></div>
    </div>
    <div class="detail-grid">
      <div class="detail-box"><span>GÃ©nero</span><strong>${escapeHtml(paciente.genero || "-")}</strong></div>
      <div class="detail-box"><span>Correo</span><strong>${escapeHtml(paciente.correo || "-")}</strong></div>
    </div>
  `;
}

function patientInfoHtml(paciente, { loading = false } = {}) {
  const nombre = fullName(paciente.nombre, paciente.apellido) || `Paciente ${paciente.id || "-"}`;
  return `
    <h4>Informacion del paciente</h4>
    ${loading ? `<p class="hint">Consultando datos registrados del paciente...</p>` : ""}
    <div class="detail-grid">
      <div class="detail-box"><span>Documento</span><strong>${escapeHtml(paciente.id || "-")}</strong></div>
      <div class="detail-box"><span>Nombre completo</span><strong>${escapeHtml(nombre)}</strong></div>
    </div>
    <div class="detail-grid">
      <div class="detail-box"><span>Telefono</span><strong>${escapeHtml(paciente.telefono || "-")}</strong></div>
      <div class="detail-box"><span>Fecha nacimiento</span><strong>${formatDate(paciente.fechaNacimiento)}</strong></div>
    </div>
    <div class="detail-grid">
      <div class="detail-box"><span>Genero</span><strong>${escapeHtml(paciente.genero || "-")}</strong></div>
      <div class="detail-box"><span>Correo</span><strong>${escapeHtml(paciente.correo || "-")}</strong></div>
    </div>
  `;
}

function renderDetailModal(c) {
  if (!c) return;
  const specialist = state.especialistas.find(e => e.id === c.especialistaId);
  showModal(`
    <div class="modal">
      <div class="modal-header"><h3>Detalle de la cita</h3><button class="modal-close" data-modal-close>×</button></div>
      <div class="modal-body">
        <div class="detail-box"><span>Especialidad</span><strong>${formatSpecialty(specialist?.especialidad || c.especialistaEspecialidad || "Consulta médica")}</strong></div>
        <div class="detail-box"><span>Profesional</span><strong>${escapeHtml(c.especialistaNombre || specialist?.nombre || "Profesional")}</strong></div>
        <div class="detail-grid"><div class="detail-box"><span>Fecha</span><strong>${formatDate(c.fecha)}</strong></div><div class="detail-box"><span>Hora</span><strong>${formatTime(c.hora)}</strong></div></div>
        <div class="detail-box"><span>Tipo de atención</span><strong>Consulta médica</strong></div>
        <div class="detail-box"><span>Estado</span>${statusBadge(c.estado)}</div>
      </div>
      <div class="modal-footer">
        ${specialist ? `<button class="btn" id="detailSpecialist">Ver especialista</button>` : ""}
        <button class="btn" id="detailPatient">Ver paciente</button>
        ${canCancelAppointment(c) ? `<button class="btn btn-danger" id="detailCancel">Cancelar cita</button>` : ""}
        ${canReagendarAppointment(c) ? `<button class="btn btn-primary" id="detailReagendar">Reagendar cita</button>` : ""}
        ${attendanceActions(c)}
        <button class="btn" data-modal-close>Cerrar</button>
      </div>
    </div>
  `);
  $("#detailSpecialist")?.addEventListener("click", () => openEspecialistaDetailModal(specialist.id));
  $("#detailPatient")?.addEventListener("click", () => openPatientInfoModal(c));
  $("#detailCancel")?.addEventListener("click", () => openCancelModal(c));
  $("#detailReagendar")?.addEventListener("click", () => openReagendarModal(c));
  bindAttendanceActions(() => { closeModal(); route(); }, modalRoot);
}

function openDetailModal(c) {
  if (!c) return;
  renderDetailModal(c);
}

async function openPatientInfoModal(c) {
  if (!c) return;
  const modalToken = `patient-${c.id}-${Date.now()}`;
  modalRoot.dataset.detailToken = modalToken;
  renderPatientInfoModal(c, appointmentPatientFallback(c), { loading: true });
  const paciente = await loadAppointmentPatientInfo(c);
  if (!modalRoot.classList.contains("hidden") && modalRoot.dataset.detailToken === modalToken) {
    renderPatientInfoModal(c, paciente);
  }
}

function renderPatientInfoModal(c, paciente, { loading = false } = {}) {
  showModal(`
    <div class="modal">
      <div class="modal-header"><h3>Informacion del paciente</h3><button class="modal-close" data-modal-close>Ã—</button></div>
      <div class="modal-body">
        ${patientInfoHtml(paciente, { loading })}
      </div>
      <div class="modal-footer">
        <button class="btn" id="backToAppointmentDetail">Volver al detalle</button>
        <button class="btn" data-modal-close>Cerrar</button>
      </div>
    </div>
  `);
  modalRoot.querySelector(".modal-close")?.replaceChildren(document.createTextNode("x"));
  $("#backToAppointmentDetail")?.addEventListener("click", () => renderDetailModal(c));
}

function openReagendarModal(c) {
  if (!canReagendarAppointment(c)) {
    showAlert("Solo se pueden reagendar citas asistidas.", "error");
    return;
  }

  const specialist = state.especialistas.find(e => e.id === c.especialistaId);
  const specialistName = c.especialistaNombre || specialist?.nombre || "Profesional";

  showModal(`
    <div class="modal">
      <div class="modal-header"><h3>Reagendar cita</h3><button class="modal-close" data-modal-close>×</button></div>
      <form id="reagendarForm">
        <div class="modal-body">
          <div class="detail-box"><span>Paciente</span><strong>${escapeHtml(getAppointmentPatientName(c))}</strong></div>
          <div class="detail-box"><span>Especialista</span><strong>${escapeHtml(specialistName)}</strong></div>
          <div class="field"><label class="required">Nueva fecha</label><input id="reagendarDate" name="fecha" type="date" min="${tomorrowIso()}" value="${tomorrowIso()}" required /></div>
          <input type="hidden" id="reagendarSelectedTime" name="hora" />
          <div id="reagendarSlotPanel" class="scroll-panel"><div class="empty-state"><strong>Seleccione una fecha para ver horarios disponibles</strong></div></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" id="confirmReagendar">Agendar nueva cita</button>
          <button class="btn" type="button" data-modal-close>Cancelar</button>
        </div>
      </form>
    </div>
  `);

  $("#reagendarDate").addEventListener("change", () => updateReagendarSlots(c));
  $("#reagendarForm").addEventListener("submit", event => submitReagendarAppointment(event, c));
  updateReagendarSlots(c);
}

async function updateReagendarSlots(c) {
  const date = $("#reagendarDate").value;
  const panel = $("#reagendarSlotPanel");
  $("#reagendarSelectedTime").value = "";

  if (!date) {
    panel.innerHTML = `<div class="empty-state"><strong>Seleccione una fecha para ver horarios disponibles</strong></div>`;
    return;
  }

  if (date <= todayIso()) {
    panel.innerHTML = `<div class="warning-box"><strong>Seleccione una fecha posterior a hoy.</strong><br />No se pueden reagendar citas para el mismo dia.</div>`;
    return;
  }

  let horarios = [];
  try {
    const result = await api.getHorarios(c.especialistaId, date);
    horarios = Array.isArray(result) ? result : [];
  } catch (error) {
    panel.innerHTML = emptyState("No se pudieron consultar horarios disponibles");
    showAlert(`No se pudieron consultar horarios: ${error.message}`, "error");
    return;
  }

  const doctor = state.especialistas.find(e => e.id === c.especialistaId) || {
    id: c.especialistaId,
    nombre: c.especialistaNombre || "Profesional",
    especialidad: c.especialistaEspecialidad
  };
  const slots = horarios.map(hora => ({ hora: String(hora).slice(0, 5), fecha: date, doctor }));

  panel.innerHTML = `
    <h3>Horarios disponibles</h3>
    <div class="slot-list">
      ${slots.map((slot, idx) => `<label class="slot-option"><input type="radio" name="reagendarSlot" value="${idx}" /><span><strong>${formatDate(slot.fecha)}</strong>${formatTime(slot.hora)} - ${escapeHtml(slot.doctor.nombre)}</span></label>`).join("") || emptyState("No hay horarios disponibles")}
    </div>
    <div id="reagendarSlotSummary"></div>
  `;

  document.querySelectorAll("input[name='reagendarSlot']").forEach(input => {
    input.addEventListener("change", () => showReagendarSlotSummary(slots[Number(input.value)]));
  });
}

function showReagendarSlotSummary(slot) {
  if (!slot) return;
  document.querySelectorAll(".slot-option").forEach(label => label.classList.toggle("selected", label.querySelector("input").checked));
  $("#reagendarSelectedTime").value = slot.hora;
  $("#reagendarSlotSummary").innerHTML = `
    <div class="info-box">
      <strong>Nuevo horario seleccionado:</strong><br />
      ${formatDate(slot.fecha)} - ${formatTime(slot.hora)} con ${escapeHtml(slot.doctor.nombre)}
    </div>
  `;
}

async function submitReagendarAppointment(event, c) {
  event.preventDefault();
  const formEl = event.currentTarget;
  const selectedTime = $("#reagendarSelectedTime").value;

  if (!selectedTime) {
    showAlert("Seleccione uno de los horarios disponibles.", "error");
    return;
  }

  const payload = Object.fromEntries(new FormData(formEl).entries());
  payload.hora = String(payload.hora).slice(0, 5);

  try {
    $("#confirmReagendar").disabled = true;
    $("#confirmReagendar").textContent = "Agendando...";
    const nuevaCita = await api.reagendarCita(c.id, payload);

    if (nuevaCita && typeof nuevaCita === "object") {
      state.citas = [
        ...state.citas.filter(cita => cita.id !== nuevaCita.id),
        nuevaCita
      ];
    }

    showAlert("Nueva cita agendada correctamente.", "success");
    closeModal();
    await loadCitas({ silent: true });
    route();
  } catch (error) {
    showAlert(`No se pudo reagendar la cita: ${error.message}`, "error");
  } finally {
    const button = $("#confirmReagendar");
    if (button) {
      button.disabled = false;
      button.textContent = "Agendar nueva cita";
    }
  }
}

function openCancelModal(c, afterCancel = null) {
  if (!c) return;
  const specialist = state.especialistas.find(e => e.id === c.especialistaId);
  showModal(`<div class="modal"><div class="modal-header"><h3>Confirmar cancelación</h3><button class="modal-close" data-modal-close>×</button></div><div class="modal-body"><p><strong>¿Está seguro que desea cancelar esta cita?</strong></p><div class="detail-box"><strong>${formatSpecialty(specialist?.especialidad || "Consulta médica")}</strong><br />${escapeHtml(c.especialistaNombre || specialist?.nombre || "Profesional")}<br />${formatDate(c.fecha)} - ${formatTime(c.hora)}</div></div><div class="modal-footer"><button class="btn btn-danger" id="confirmCancel">Sí, cancelar cita</button><button class="btn" data-modal-close>No cancelar</button></div></div>`);
  $("#confirmCancel").addEventListener("click", async () => {
    try {
      await api.cancelarCita(c.id);
      showAlert("Cita cancelada correctamente.", "success");
      await loadCitas({ silent: true });
    } catch (error) {
      showAlert(`No se pudo cancelar en el backend: ${error.message}`, "error");
    }
    closeModal();
    if (afterCancel) {
      afterCancel();
    } else if (state.currentRoute === "mis-citas") {
      renderMisCitas();
    } else {
      route();
    }
  });
}

function renderAgendarCitaManual() {
  if (getCurrentRole() !== "AGENDADOR") {
    showAlert("No tiene permisos para agendar citas manualmente.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }
  viewRoot.innerHTML = `
    ${pageHeader("Agendar Cita", "Registre una cita seleccionando un horario disponible")}
    <section class="panel">
      <h3>Datos del paciente</h3>
      <form id="manualAppointmentForm">
        <div class="grid-2">
          <div class="field autocomplete-field"><label class="required">Documento paciente</label><input name="pacienteId" id="manualPatientId" type="number" min="1" placeholder="Ej: 1054123456" autocomplete="off" required /><div id="manualPatientSuggestions" class="autocomplete-list hidden"></div><small class="hint">Ingrese al menos 3 números para buscar pacientes registrados o con citas existentes.</small></div>
          <div class="field"><label class="required">Nombres</label><input name="nombrePaciente" placeholder="Ej: Maria Camila" minlength="2" maxlength="60" required /><small class="hint">Use solo letras. Se corregirán espacios, mayúsculas y tildes.</small></div>
          <div class="field"><label class="required">Apellidos</label><input name="apellidoPaciente" placeholder="Ej: Perez Gomez" minlength="2" maxlength="60" required /><small class="hint">Use solo letras y espacios.</small></div>
          <div class="field"><label class="required">Celular</label><input name="telefono" inputmode="numeric" pattern="[0-9]{10}" maxlength="10" placeholder="Ej: 3001234567" required /><small class="hint">Ingrese exactamente 10 números.</small></div>
          <div class="field"><label>Fecha nacimiento</label><input name="fechaNacimiento" type="date" value="1998-01-01" /></div>
          <div class="field"><label>Género</label><select name="genero"><option>MASCULINO</option><option>FEMENINO</option><option>OTRO</option></select></div>
        </div>
        <div class="field"><label>Correo electrónico</label><input name="correo" type="email" placeholder="Ej: paciente@correo.com" /></div>
        <h3>Datos de la cita</h3>
        <div class="grid-2">
          <div class="field"><label class="required">Médico/Terapeuta</label><select name="especialistaId" id="manualDoctor" required>${specialistOptions()}</select></div>
          <div class="field"><label class="required">Fecha</label><input name="fecha" id="manualDate" type="date" min="${tomorrowIso()}" value="${tomorrowIso()}" required /></div>
        </div>
        <div class="actions"><button type="button" class="btn" id="manualSpecialistDetail">Ver detalle del especialista</button></div>
        <section class="panel" id="manualSlotPanel"><div class="empty-state"><strong>Seleccione paciente, profesional y fecha para ver horarios disponibles</strong></div></section>
        <input type="hidden" name="hora" id="manualSelectedTime" />
        <div class="actions"><button class="btn btn-primary">Agendar</button><button class="btn" type="reset">Limpiar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
  `;
  ["nombrePaciente", "apellidoPaciente"].forEach(field => {
    $("#manualAppointmentForm").elements[field].addEventListener("blur", event => {
      event.target.value = normalizePersonName(event.target.value);
    });
  });
  setupManualPatientAutocomplete();
  $("#manualDoctor").addEventListener("change", updateManualSlots);
  $("#manualDate").addEventListener("change", updateManualSlots);
  $("#manualSpecialistDetail").addEventListener("click", () => openEspecialistaDetailModal($("#manualDoctor").value));
  updateManualSlots();
  $("#manualAppointmentForm").addEventListener("submit", async event => {
    event.preventDefault();
    const formEl = event.currentTarget;
    const nombre = normalizeFormName(formEl, "nombrePaciente");
    const apellido = normalizeFormName(formEl, "apellidoPaciente");
    if (!isValidPersonName(nombre) || !isValidPersonName(apellido)) {
      showAlert("Revise nombres y apellidos: use solo letras y espacios.", "error");
      return;
    }

    if (!$("#manualSelectedTime").value) {
      showAlert("Seleccione uno de los horarios disponibles.", "error");
      return;
    }

    const f = new FormData(formEl);
    const payload = Object.fromEntries(f.entries());
    payload.pacienteId = Number(payload.pacienteId);
    payload.telefono = String(payload.telefono || "").trim();
    payload.hora = String(payload.hora).slice(0, 5);
    if (hasBlockingAppointment(payload.pacienteId)) {
      showAlert("El paciente ya tiene una cita agendada o pendiente.", "error");
      return;
    }

    if (payload.fecha <= todayIso()) {
      showAlert("No se pueden agendar citas para el mismo dia.", "error");
      return;
    }

    try {
      await api.agendarAgendador(payload);
      showAlert("Cita agendada correctamente.", "success");
      await loadCitas({ silent: true });
      location.hash = "#/consultar-citas";
    } catch (error) {
      showAlert(`No se pudo agendar: ${error.message}`, "error");
    }
  });
}

function refreshManualPatientRules() {
  const patientId = Number($("#manualPatientId").value || 0);
  const slotPanel = $("#manualSlotPanel");
  $("#manualSelectedTime").value = "";

  if (!patientId) {
    $("#manualDoctor").disabled = false;
    $("#manualDate").disabled = false;
    slotPanel.innerHTML = `<div class="empty-state"><strong>Ingrese el documento del paciente para continuar</strong></div>`;
    return;
  }

  if (hasBlockingAppointment(patientId)) {
    $("#manualDoctor").disabled = true;
    $("#manualDate").disabled = true;
    slotPanel.innerHTML = `<div class="warning-box"><strong>Paciente con cita activa.</strong><br />No se puede crear una nueva cita mientras tenga una cita Agendada, Pendiente o Reagendada.</div>`;
    return;
  }

  $("#manualDoctor").disabled = false;
  $("#manualDate").disabled = false;
  updateManualSlots();
}

async function updateManualSlots() {
  const doctorId = $("#manualDoctor").value;
  const date = $("#manualDate").value;
  const patientId = Number($("#manualPatientId").value || 0);
  $("#manualSelectedTime").value = "";

  if (!patientId || !doctorId || !date) return;
  if (hasBlockingAppointment(patientId)) {
    refreshManualPatientRules();
    return;
  }

  if (date <= todayIso()) {
    $("#manualSlotPanel").innerHTML = `<div class="warning-box"><strong>Seleccione una fecha posterior a hoy.</strong><br />No se pueden agendar citas para el mismo dia.</div>`;
    return;
  }

  const doctor = state.especialistas.find(e => e.id === doctorId);
  let horarios = [];
  try {
    const result = await api.getHorarios(doctorId, date);
    horarios = Array.isArray(result) ? result : [];
  } catch (error) {
    $("#manualSlotPanel").innerHTML = emptyState("No se pudieron consultar horarios disponibles");
    showAlert(`No se pudieron consultar horarios: ${error.message}`, "error");
    return;
  }

  const slots = horarios.map(hora => ({ hora: String(hora).slice(0, 5), fecha: date, doctor })).filter(slot => slot.doctor);
  $("#manualSlotPanel").innerHTML = `
    <h3>Horarios disponibles</h3>
    <div class="slot-list">
      ${slots.map((slot, idx) => `<label class="slot-option"><input type="radio" name="manualSlot" value="${idx}" /><span><strong>${formatDate(slot.fecha)}</strong>${formatTime(slot.hora)} - ${escapeHtml(slot.doctor.nombre)}</span></label>`).join("") || emptyState("No hay horarios disponibles")}
    </div>
    <div id="manualSlotSummary"></div>
  `;
  document.querySelectorAll("input[name='manualSlot']").forEach(input => {
    input.addEventListener("change", () => showManualSlotSummary(slots[Number(input.value)]));
  });
}

function showManualSlotSummary(slot) {
  if (!slot) return;
  document.querySelectorAll(".slot-option").forEach(label => label.classList.toggle("selected", label.querySelector("input").checked));
  $("#manualSelectedTime").value = slot.hora;
  $("#manualSlotSummary").innerHTML = `
    <div class="info-box">
      <strong>Horario seleccionado:</strong><br />
      ${formatDate(slot.fecha)} - ${formatTime(slot.hora)} con ${escapeHtml(slot.doctor.nombre)}
    </div>
  `;
}

function patientOptionFromCita(cita) {
  return {
    ...appointmentPatientFallback(cita),
    origen: "Cita programada"
  };
}

async function buildManualPatientOptions(prefix) {
  let registrados = [];
  try {
    const result = await api.buscarPacientes(prefix);
    registrados = Array.isArray(result) ? result : [];
  } catch (_) {
    registrados = [];
  }

  const porId = new Map();
  state.citas
    .filter(cita => String(cita.pacienteId || "").startsWith(prefix))
    .map(patientOptionFromCita)
    .forEach(paciente => porId.set(String(paciente.id), paciente));

  registrados.forEach(paciente => {
    const id = String(paciente.id);
    const cita = porId.get(id);
    porId.set(id, {
      ...(cita || {}),
      ...paciente,
      origen: cita ? "Cuenta registrada y cita programada" : "Cuenta registrada"
    });
  });

  return [...porId.values()]
    .sort((a, b) => String(a.id).localeCompare(String(b.id)))
    .slice(0, 10);
}

function renderManualPatientSuggestions(options) {
  const box = $("#manualPatientSuggestions");
  if (!box) return;

  if (!options.length) {
    box.innerHTML = `<div class="autocomplete-empty">No se encontraron pacientes</div>`;
    box.classList.remove("hidden");
    return;
  }

  box.innerHTML = options.map((paciente, idx) => {
    const nombre = fullName(paciente.nombre, paciente.apellido) || `Paciente ${paciente.id}`;
    return `
      <button type="button" class="autocomplete-option" data-patient-option="${idx}">
        <strong>${escapeHtml(paciente.id)} - ${escapeHtml(nombre)}</strong>
        <span>${escapeHtml(paciente.origen || "Paciente")}</span>
      </button>
    `;
  }).join("");
  box.classList.remove("hidden");
  box.querySelectorAll("[data-patient-option]").forEach(btn => {
    btn.addEventListener("click", () => selectManualPatientOption(options[Number(btn.dataset.patientOption)]));
  });
}

function hideManualPatientSuggestions() {
  $("#manualPatientSuggestions")?.classList.add("hidden");
}

function selectManualPatientOption(paciente) {
  if (!paciente) return;
  const form = $("#manualAppointmentForm");
  const parsed = splitFullName(fullName(paciente.nombre, paciente.apellido));
  form.elements.pacienteId.value = paciente.id || "";
  form.elements.nombrePaciente.value = paciente.nombre || parsed.nombre || "";
  form.elements.apellidoPaciente.value = paciente.apellido || parsed.apellido || "";
  form.elements.telefono.value = paciente.telefono || "";
  form.elements.fechaNacimiento.value = paciente.fechaNacimiento || "1998-01-01";
  form.elements.genero.value = paciente.genero || "MASCULINO";
  form.elements.correo.value = paciente.correo || "";
  hideManualPatientSuggestions();
  refreshManualPatientRules();
}

function setupManualPatientAutocomplete() {
  const input = $("#manualPatientId");
  const box = $("#manualPatientSuggestions");
  if (!input || !box) return;

  input.addEventListener("input", () => {
    refreshManualPatientRules();
    clearTimeout(manualPatientAutocomplete.timer);
    const prefix = String(input.value || "").replace(/\D/g, "");

    if (prefix.length < 3) {
      hideManualPatientSuggestions();
      return;
    }

    const requestId = ++manualPatientAutocomplete.requestId;
    box.innerHTML = `<div class="autocomplete-empty">Buscando pacientes...</div>`;
    box.classList.remove("hidden");
    manualPatientAutocomplete.timer = setTimeout(async () => {
      const options = await buildManualPatientOptions(prefix);
      if (requestId === manualPatientAutocomplete.requestId) {
        renderManualPatientSuggestions(options);
      }
    }, 250);
  });

  input.addEventListener("blur", () => setTimeout(hideManualPatientSuggestions, 160));
}

function renderConsultarCitas() {
  if (getCurrentRole() !== "AGENDADOR") {
    showAlert("No tiene permisos para consultar todas las citas.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }
  viewRoot.innerHTML = `
    ${pageHeader("Consultar Citas", "Busque y consulte citas agendadas")}
    <section class="panel">
      <h3>Filtros de búsqueda</h3>
      <form id="appointmentFilters">
        <div class="grid-3">
          <div class="field"><label>Fecha de la cita</label><input name="fecha" type="date" /></div>
          <div class="field"><label>Especialista</label><select name="especialistaId">${specialistOptions()}</select></div>
          <div class="field"><label>Estado</label><select name="estado"><option value="">Todas</option><option>AGENDADA</option><option>PENDIENTE</option><option>REAGENDADA</option><option>CANCELADA</option><option>ASISTIDA</option><option>NO_ASISTIDA</option></select></div>
        </div>
        <div class="actions"><button class="btn btn-primary">Buscar</button><button class="btn" type="reset">Limpiar filtros</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel"><h3 id="appointmentsFoundTitle">Citas encontradas (${state.citas.length})</h3><div id="appointmentsTable"></div></section>
  `;
  const render = citas => {
    $("#appointmentsFoundTitle").textContent = `Citas encontradas (${citas.length})`;
    $("#appointmentsTable").innerHTML = citas.length ? `<div class="table-wrap"><table><thead><tr><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Tipo de atención</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${citas.map(c => `<tr><td>${formatDate(c.fecha)}</td><td>${formatTime(c.hora)}</td><td>${escapeHtml(getAppointmentPatientName(c))}</td><td>Consulta médica</td><td>${statusBadge(c.estado)}</td><td><div class="actions table-actions"><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button>${canCancelAppointment(c) ? `<button class="btn btn-danger btn-small" data-cancel="${escapeHtml(c.id)}">Cancelar</button>` : ""}${attendanceActions(c)}</div></td></tr>`).join("")}</tbody></table></div>` : emptyState("No hay citas encontradas");
    document.querySelectorAll("[data-detail]").forEach(btn => btn.addEventListener("click", () => openDetailModal(state.citas.find(c => c.id === btn.dataset.detail))));
    document.querySelectorAll("[data-cancel]").forEach(btn => btn.addEventListener("click", () => openCancelModal(state.citas.find(c => c.id === btn.dataset.cancel), () => route())));
    bindAttendanceActions(() => route());
  };
  render(state.citas);
  $("#appointmentFilters").addEventListener("submit", event => {
    event.preventDefault();
    const f = new FormData(event.currentTarget);
    const filtered = state.citas.filter(c =>
      (!f.get("estado") || c.estado === f.get("estado")) &&
      (!f.get("fecha") || c.fecha === f.get("fecha")) &&
      (!f.get("especialistaId") || c.especialistaId === f.get("especialistaId"))
    );
    render(filtered);
  });
  $("#appointmentFilters").addEventListener("reset", () => setTimeout(() => render(state.citas), 0));
}

function renderCitasMedico() {
  viewRoot.innerHTML = `
    ${pageHeader("Mis Citas Programadas", "Consulte sus citas como profesional de la salud")}
    <section class="panel"><h3>Filtros de búsqueda</h3><div class="field"><label>Profesional</label><select id="doctorAppointmentFilter">${specialistOptions(state.especialistas[0]?.id)}</select></div><div class="grid-2"><div class="field"><label>Fecha inicio</label><input type="date" id="doctorStart" /></div><div class="field"><label>Fecha fin</label><input type="date" id="doctorEnd" /></div></div><div class="field"><label>Estado</label><select id="doctorStatus"><option value="">Todas</option><option>AGENDADA</option><option>PENDIENTE</option><option>REAGENDADA</option><option>CANCELADA</option><option>ASISTIDA</option><option>NO_ASISTIDA</option></select></div><div class="actions"><button class="btn" id="clearDoctorFilters">Limpiar filtros</button><a class="btn" href="#/inicio">Volver</a></div></section>
    <section class="panel"><h3 id="doctorFoundTitle">Citas encontradas</h3><div id="doctorAppointmentsTable"></div></section>
  `;
  const filter = () => {
    const doc = $("#doctorAppointmentFilter").value;
    const start = $("#doctorStart").value;
    const end = $("#doctorEnd").value;
    const status = $("#doctorStatus").value;
    const citas = state.citas.filter(c => (!doc || c.especialistaId === doc) && (!status || c.estado === status) && (!start || c.fecha >= start) && (!end || c.fecha <= end));
    $("#doctorFoundTitle").textContent = `Citas encontradas (${citas.length})`;
    $("#doctorAppointmentsTable").innerHTML = citas.length ? `<div class="table-wrap"><table><thead><tr><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Tipo de atención</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${citas.map(c => `<tr><td>${formatDate(c.fecha)}</td><td>${formatTime(c.hora)}</td><td>${escapeHtml(getAppointmentPatientName(c))}</td><td>Consulta médica</td><td>${statusBadge(c.estado)}</td><td><div class="actions table-actions"><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button>${canCancelAppointment(c) ? `<button class="btn btn-danger btn-small" data-cancel="${escapeHtml(c.id)}">Cancelar</button>` : ""}${attendanceActions(c)}</div></td></tr>`).join("")}</tbody></table></div>` : emptyState("No hay citas para este profesional");
    document.querySelectorAll("[data-detail]").forEach(btn => btn.addEventListener("click", () => openDetailModal(state.citas.find(c => c.id === btn.dataset.detail))));
    document.querySelectorAll("[data-cancel]").forEach(btn => btn.addEventListener("click", () => openCancelModal(state.citas.find(c => c.id === btn.dataset.cancel), () => route())));
    bindAttendanceActions(() => route());
  };
  ["#doctorAppointmentFilter", "#doctorStart", "#doctorEnd", "#doctorStatus"].forEach(sel => $(sel).addEventListener("change", filter));
  $("#clearDoctorFilters").addEventListener("click", () => { $("#doctorStart").value = ""; $("#doctorEnd").value = ""; $("#doctorStatus").value = ""; filter(); });
  filter();
}

function openExportCitasModal() {
  showModal(`
    <div class="modal">
      <div class="modal-header"><h3>Exportar citas</h3><button class="modal-close" data-modal-close>×</button></div>
      <div class="modal-body">
        <div class="field"><label class="required">Especialista</label><select id="exportSpecialist" required>${specialistOptions()}</select></div>
        <div class="field"><label class="required">Fecha</label><input id="exportDate" type="date" value="${todayIso()}" required /></div>
        <p class="hint">El CSV incluirá únicamente las citas del especialista seleccionado en esa fecha.</p>
      </div>
      <div class="modal-footer"><button class="btn btn-primary" id="confirmExportCsv">Exportar CSV</button><button class="btn" data-modal-close>Cancelar</button></div>
    </div>
  `);
  $("#confirmExportCsv").addEventListener("click", () => {
    const especialistaId = $("#exportSpecialist").value;
    const fecha = $("#exportDate").value;
    if (!especialistaId || !fecha) {
      showAlert("Seleccione especialista y fecha para exportar.", "error");
      return;
    }
    exportCitasCsv({ especialistaId, fecha });
  });
}

function exportCitasCsv({ especialistaId, fecha }) {
  const citas = state.citas.filter(c => c.especialistaId === especialistaId && c.fecha === fecha);
  if (!citas.length) {
    showAlert("No hay citas para ese especialista en la fecha seleccionada.", "info");
    return;
  }

  const rows = [["id", "pacienteId", "pacienteNombre", "especialistaId", "especialistaNombre", "fecha", "hora", "estado"], ...citas.map(c => [c.id, c.pacienteId, getAppointmentPatientName(c), c.especialistaId, c.especialistaNombre, c.fecha, c.hora, c.estado])];
  const csv = rows.map(row => row.map(v => `"${String(v ?? "").replaceAll('"', '""')}"`).join(",")).join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `citas-${especialistaId}-${fecha}.csv`;
  link.click();
  URL.revokeObjectURL(url);
  closeModal();
}

function route() {
  state.currentRoute = (location.hash.replace("#/", "") || "inicio").split("?")[0];

  if (!state.user && !isPublicRoute(state.currentRoute)) {
    location.hash = "#/login";
    return;
  }

  if (state.user && isPublicRoute(state.currentRoute)) {
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }

  if (state.user && !canAccessRoute(state.currentRoute)) {
    showAlert("No tiene permisos para acceder a esta sección.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }

  updateMenuByRole();
  setActiveNav(state.currentRoute);

  const routes = {
    login: renderLogin,
    registro: renderRegistro,
    inicio: renderInicio,

    "registrar-medico": renderRegistrarMedico,
    "dias-atencion": renderDiasAtencion,
    "intervalo-citas": renderDiasAtencion,
    "ventana-agendamiento": renderDiasAtencion,

    "portal-agendar": renderPortalAgendar,
    "mis-citas": renderMisCitas,

    "agendar-cita": renderAgendarCitaManual,
    "consultar-citas": renderConsultarCitas,
    "citas-medico": renderCitasMedico
  };

  const render = routes[state.currentRoute] || routes[getDefaultRouteByRole()];
  render();
}

async function init() {
  saveUser();
  if (state.user) {
    await Promise.all([loadEspecialistas({ silent: true }), loadCitas({ silent: true })]);
  }
  document.querySelector("#exportAppointments").addEventListener("click", () => {
    if (getCurrentRole() !== "AGENDADOR") {
      showAlert("No tiene permisos para exportar citas.", "error");
      return;
    }

    openExportCitasModal();
  });
  document.querySelector("#logoutButton").addEventListener("click", logout);
  window.addEventListener("hashchange", route);
  route();
}

init();
