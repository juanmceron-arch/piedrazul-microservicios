const state = {
  user: JSON.parse(localStorage.getItem("piedrazulUser") || "null"),
  especialistas: [],
  citas: [],
  settings: JSON.parse(localStorage.getItem("piedrazulSettings") || "null") || {
    intervaloMinutos: 30,
    semanasHabilitadas: 8,
    horaInicio: "08:00",
    horaFin: "12:00"
  },
  currentRoute: "inicio"
};

const SPECIALTY_LABELS = {
  MEDICINA_GENERAL: "Medicina General",
  FISIOTERAPIA: "Fisioterapia",
  PSICOLOGIA: "Psicología",
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
  const cls = normalized.includes("CANCEL") ? "badge-danger" : normalized.includes("COMPLET") ? "badge-success" : normalized.includes("REAGEND") || normalized.includes("AGEND") ? "badge-info" : "badge-muted";
  const label = normalized.charAt(0) + normalized.slice(1).toLowerCase();
  return `<span class="badge ${cls}">${label}</span>`;
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
    state.citas = [];
    if (!silent) showAlert(`No se pudo conectar con appointment-service: ${error.message}`, "error");
  }
}

function specialistOptions(selected = "") {
  return [`<option value="">Seleccione...</option>`, ...state.especialistas.map(e => `<option value="${escapeHtml(e.id)}" ${selected === e.id ? "selected" : ""}>${escapeHtml(e.nombre)} - ${formatSpecialty(e.especialidad)}</option>`)].join("");
}

function uniqueSpecialtiesOptions(selected = "") {
  const values = [...new Set([...state.especialistas.map(e => e.especialidad), "MEDICINA_GENERAL", "FISIOTERAPIA", "PSICOLOGIA"].filter(Boolean))];
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


function renderLogin() {
  viewRoot.innerHTML = `
    ${pageHeader("Iniciar sesión", "Ingrese con un usuario registrado en auth-service")}
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
    ${pageHeader("Registro de usuario", "Cree un paciente o agendador para usar el sistema")}
    <section class="panel auth-panel">
      <form id="registerForm">
        <div class="grid-2">
          <div class="field"><label class="required">Documento</label><input name="id" type="number" required /></div>
          <div class="field"><label class="required">Rol</label><select name="rol" required><option value="PACIENTE">Paciente</option><option value="AGENDADOR">Agendador</option></select></div>
          <div class="field"><label class="required">Nombre</label><input name="nombre" required /></div>
          <div class="field"><label class="required">Apellido</label><input name="apellido" required /></div>
          <div class="field"><label class="required">Contraseña</label><input name="passwordHash" type="password" required /></div>
          <div class="field"><label>Género</label><select name="genero"><option value="MASCULINO">Masculino</option><option value="FEMENINO">Femenino</option><option value="OTRO">Otro</option></select></div>
          <div class="field"><label>Teléfono</label><input name="telefono" /></div>
          <div class="field"><label>Fecha nacimiento</label><input name="fechaNacimiento" type="date" /></div>
        </div>
        <div class="field"><label>Correo electrónico</label><input name="correo" type="email" /></div>
        <div class="actions"><button class="btn btn-primary">Registrarme</button><a class="btn" href="#/login">Volver al login</a></div>
      </form>
    </section>
  `;
  $("#registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const f = new FormData(event.currentTarget);
    const payload = Object.fromEntries(f.entries());
    payload.id = Number(payload.id);
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
        ${quickCard("#/portal-agendar", "▣", "Agendar mi cita", "Seleccione una especialidad, médico y horario disponible")}
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
      ${quickCard("#", "⇩", "Exportar citas", "Generar reporte en CSV", "orange", "export-card")}
      ${quickCard("#/consultar-citas", "◷", "Reagendar", "Modificar fecha y hora de una cita", "purple")}
      ${quickCard("#/registrar-medico", "♙", "Registrar médico", "Gestionar médicos y terapeutas")}
      ${quickCard("#/dias-atencion", "▣", "Configurar días", "Días de atención de profesionales", "green")}
      ${quickCard("#/ventana-agendamiento", "⚙", "Configuraciones", "Intervalos y ventanas de agendamiento", "gray")}
    </div>
  `;

  $("#export-card")?.addEventListener("click", event => {
    event.preventDefault();
    exportCitasCsv();
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
          <div class="field"><label class="required">Tipo de documento</label><select name="tipoDocumento" required><option value="">Seleccione...</option><option>CC</option><option>CE</option><option>TI</option></select></div>
          <div class="field"><label class="required">Número de documento</label><input name="id" placeholder="Ej: 1234567890" required /></div>
          <div class="field"><label class="required">Nombres</label><input name="nombres" placeholder="Ej: Juan Carlos" required /></div>
          <div class="field"><label class="required">Apellidos</label><input name="apellidos" placeholder="Ej: Gómez Pérez" required /></div>
          <div class="field"><label class="required">Especialidad</label><select name="especialidad" required>${uniqueSpecialtiesOptions()}</select></div>
          <div class="field"><label class="required">Celular</label><input name="celular" placeholder="Ej: 3001234567" required /></div>
        </div>
        <div class="field"><label class="required">Correo electrónico</label><input name="correo" type="email" placeholder="Ej: medico@piedrazul.com" required /></div>
        <div class="actions"><button class="btn btn-primary">Guardar</button><button class="btn" type="reset">Limpiar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel">
      <h3>Listado de médicos/terapeutas registrados</h3>
      <div id="doctorsTable"></div>
    </section>
  `;
  renderDoctorsTable();
  $("#doctorForm").addEventListener("submit", handleDoctorSubmit);
}

function renderDoctorsTable() {
  const rows = state.especialistas.map(e => `<tr><td>CC ${escapeHtml(e.id)}</td><td>${escapeHtml(e.nombre)}</td><td>${formatSpecialty(e.especialidad)}</td><td>3001234567</td><td>${escapeHtml((e.nombre || "").toLowerCase().replaceAll(" ", "."))}@piedrazul.com</td><td>${statusBadge("Activo")}</td><td><button class="btn btn-primary btn-small" data-edit-doctor="${escapeHtml(e.id)}">Editar</button></td></tr>`).join("");
  $("#doctorsTable").innerHTML = rows ? `<div class="table-wrap"><table><thead><tr><th>Documento</th><th>Nombre completo</th><th>Especialidad</th><th>Celular</th><th>Correo</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyState("No hay médicos registrados");
}

async function handleDoctorSubmit(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const payload = {
    id: `${form.get("tipoDocumento") || "CC"}-${form.get("id")}`.replace(/^CC-/, ""),
    nombre: `${form.get("nombres")} ${form.get("apellidos")}`.trim(),
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
}

async function renderDiasAtencion() {
  const selectedEspecialistaId = state.especialistas[0]?.id || "";

  viewRoot.innerHTML = `
    ${pageHeader("Configurar Días de Atención", "Defina los días de la semana en que cada profesional atiende")}
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

        <div class="actions">
          <button class="btn btn-primary">Guardar configuración</button>
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

  if (!diasAtencion.length) {
    showAlert("Debe seleccionar al menos un día de atención.", "error");
    return;
  }

  const payload = {
    diasAtencion,
    horaInicio: state.settings.horaInicio,
    horaFin: state.settings.horaFin,
    intervaloMinutos: Number(state.settings.intervaloMinutos),
    semanasHabilitadas: Number(state.settings.semanasHabilitadas)
  };

  try {
    await api.saveDisponibilidad(especialistaId, payload);
    showAlert("Días de atención guardados correctamente.", "success");

    await refreshDiasAtencionForm(especialistaId);
  } catch (error) {
    showAlert(`No se pudo guardar en especialista-service: ${error.message}`, "error");
  }
}
function renderIntervaloCitas() {
  viewRoot.innerHTML = `
    ${pageHeader("Configurar Intervalo entre Citas", "Defina el tiempo en minutos entre cada cita del profesional")}
    <section class="panel">
      <form id="intervalForm">
        <h3>Configuración del intervalo</h3>
        <div class="field"><label class="required">Médico/Terapeuta</label><select name="especialistaId" required>${specialistOptions(state.especialistas[0]?.id)}</select></div>
        <div class="field"><label class="required">Intervalo entre citas (minutos)</label><input name="intervalo" type="number" min="10" step="5" value="${state.settings.intervaloMinutos}" required /></div>
        <div class="current-box"><strong>Intervalo actual:</strong><br />${state.settings.intervaloMinutos} minutos</div>
        <div class="warning-box"><strong>Nota:</strong> Este intervalo determina el tiempo mínimo entre citas consecutivas. Por ejemplo, si configura 30 minutos y una cita es a las 09:00 AM, la siguiente cita disponible será a las 09:30 AM.</div>
        <div class="actions"><button class="btn btn-primary">Guardar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel"><h3>Intervalos configurados</h3><div class="table-wrap"><table><thead><tr><th>Profesional</th><th>Intervalo (minutos)</th></tr></thead><tbody>${state.especialistas.slice(0, 5).map(e => `<tr><td>${escapeHtml(e.nombre)} - ${formatSpecialty(e.especialidad)}</td><td>${state.settings.intervaloMinutos} minutos</td></tr>`).join("")}</tbody></table></div></section>
  `;
  $("#intervalForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
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
  viewRoot.innerHTML = `
    ${pageHeader("Agendar Mi Cita", "Seleccione una especialidad y un horario disponible")}
    <section class="panel">
      <h3>Seleccionar especialidad</h3>
      <div class="field"><label class="required">Especialidad médica</label><select id="portalSpecialty">${uniqueSpecialtiesOptions()}</select></div>
      <div class="grid-2">
        <div class="field"><label>Médico/Terapeuta</label><select id="portalDoctor"><option value="">Seleccione una especialidad primero...</option></select></div>
        <div class="field"><label>Fecha</label><input id="portalDate" type="date" /></div>
      </div>
    </section>
    <section class="panel" id="slotPanel"><div class="empty-state"><strong>Seleccione una especialidad para ver los horarios disponibles</strong></div></section>
  `;
  $("#portalSpecialty").addEventListener("change", updatePortalDoctors);
  $("#portalDoctor").addEventListener("change", updateSlots);
  $("#portalDate").addEventListener("change", updateSlots);
}

function updatePortalDoctors() {
  const specialty = $("#portalSpecialty").value;
  const doctors = state.especialistas.filter(e => !specialty || e.especialidad === specialty);
  $("#portalDoctor").innerHTML = [`<option value="">Seleccione...</option>`, ...doctors.map(e => `<option value="${escapeHtml(e.id)}">${escapeHtml(e.nombre)} - ${formatSpecialty(e.especialidad)}</option>`)].join("");
  $("#slotPanel").innerHTML = `<div class="empty-state"><strong>Seleccione un profesional y fecha para consultar horarios</strong></div>`;
}

async function updateSlots() {
  const specialty = $("#portalSpecialty").value;
  const doctorId = $("#portalDoctor").value;
  const date = $("#portalDate").value;
  if (!specialty || !doctorId || !date) return;
  const doctor = state.especialistas.find(e => e.id === doctorId);
  let horarios = [];
  try {
    const result = await api.getHorarios(doctorId, date);
    horarios = Array.isArray(result) ? result : [];
  } catch (_) {
    horarios = ["08:00", "09:00", "10:30", "11:00"];
  }
  const slots = horarios.map(hora => ({ hora: String(hora).slice(0, 5), fecha: date, doctor })).filter(slot => slot.doctor);
  $("#slotPanel").innerHTML = `
    <h3>Horarios disponibles</h3>
    <p class="muted">Estos horarios están disponibles según la disponibilidad del profesional, días festivos y horarios de atención.</p>
    <div class="slot-list" id="slotList">
      ${slots.map((slot, idx) => `<label class="slot-option"><input type="radio" name="slot" value="${idx}" /><span><strong>${formatDate(slot.fecha)}</strong>${formatTime(slot.hora)} - ${escapeHtml(slot.doctor.nombre)}</span></label>`).join("") || emptyState("No hay horarios disponibles")}
    </div>
    <div id="slotSummary"></div>
  `;
  document.querySelectorAll("input[name='slot']").forEach(input => input.addEventListener("change", () => showSlotSummary(slots[Number(input.value)], specialty)));
}

function showSlotSummary(slot, specialty) {
  document.querySelectorAll(".slot-option").forEach(label => label.classList.toggle("selected", label.querySelector("input").checked));
  $("#slotSummary").innerHTML = `
    <section class="panel" style="margin-top:18px;">
      <h3>Resumen de su cita</h3>
      <table class="summary-table"><tbody>
        <tr><td>Especialidad:</td><td>${formatSpecialty(specialty)}</td></tr>
        <tr><td>Profesional:</td><td>${escapeHtml(slot.doctor.nombre)}</td></tr>
        <tr><td>Fecha:</td><td>${formatDate(slot.fecha)}</td></tr>
        <tr><td>Hora:</td><td>${formatTime(slot.hora)}</td></tr>
      </tbody></table>
      <div class="actions"><button class="btn btn-primary" id="confirmPortalAppointment">Agendar cita</button><button class="btn" id="cancelSlotSelection">Cancelar selección</button></div>
    </section>
  `;
  $("#cancelSlotSelection").addEventListener("click", updateSlots);
  $("#confirmPortalAppointment").addEventListener("click", () => createPatientAppointment(slot));
}

async function createPatientAppointment(slot) {
  const payload = {
    pacienteId: Number(state.user?.idUsuario || 0),
    especialistaId: slot.doctor.id,
    fecha: slot.fecha,
    hora: slot.hora
  };
  try {
    await api.agendarPaciente(payload);
    showAlert("Cita agendada correctamente.", "success");
    await loadCitas({ silent: true });
    location.hash = "#/mis-citas";
  } catch (error) {
    showAlert(`No se pudo agendar la cita: ${error.message}`, "error");
  }
}

function renderMisCitas() {
  if (getCurrentRole() !== "PACIENTE") {
    showAlert("Esta sección es exclusiva para pacientes.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }

  const patientId = Number(state.user?.idUsuario || 0);
  const citas = state.citas.filter(c => Number(c.pacienteId) === patientId);

  viewRoot.innerHTML = `
    ${pageHeader("Mis Citas", "Consulte y gestione sus citas médicas agendadas")}
    <section class="panel"><h3>Filtrar por estado</h3><div class="filter-pills"><button class="btn btn-small active" data-status="TODAS">Todas</button><button class="btn btn-small" data-status="AGENDADA">Agendadas</button><button class="btn btn-small" data-status="CANCELADA">Canceladas</button></div></section>
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
    const specialty = specialist?.especialidad || c.tipoAtencion || "Consulta médica";
    const canCancel = !String(c.estado || "").toUpperCase().includes("CANCEL");
    return `<article class="appointment-card"><header><h4>${formatSpecialty(specialty)}</h4>${statusBadge(c.estado)}</header><p class="muted">${escapeHtml(c.especialistaNombre || specialist?.nombre || "Profesional")}</p><div class="appointment-meta"><div><span>Fecha</span><strong>${formatDate(c.fecha)}</strong></div><div><span>Hora</span><strong>${formatTime(c.hora)}</strong></div><div><span>Tipo</span><strong>Consulta médica</strong></div></div><div class="actions"><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button>${patientActions && canCancel ? `<button class="btn btn-danger btn-small" data-cancel="${escapeHtml(c.id)}">Cancelar cita</button>` : ""}</div></article>`;
  };
}

function openDetailModal(c) {
  if (!c) return;
  const specialist = state.especialistas.find(e => e.id === c.especialistaId);
  showModal(`<div class="modal"><div class="modal-header"><h3>Detalle de la cita</h3><button class="modal-close" data-modal-close>×</button></div><div class="modal-body"><div class="detail-box"><span>Especialidad</span><strong>${formatSpecialty(specialist?.especialidad || "Consulta médica")}</strong></div><div class="detail-box"><span>Profesional</span><strong>${escapeHtml(c.especialistaNombre || specialist?.nombre || "Profesional")}</strong></div><div class="detail-grid"><div class="detail-box"><span>Fecha</span><strong>${formatDate(c.fecha)}</strong></div><div class="detail-box"><span>Hora</span><strong>${formatTime(c.hora)}</strong></div></div><div class="detail-box"><span>Tipo de atención</span><strong>Consulta médica</strong></div><div class="detail-box"><span>Estado</span>${statusBadge(c.estado)}</div></div><div class="modal-footer"><button class="btn" data-modal-close>Cerrar</button></div></div>`);
}

function openCancelModal(c) {
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
    renderMisCitas();
  });
}

function renderAgendarCitaManual() {
  if (getCurrentRole() !== "AGENDADOR") {
    showAlert("No tiene permisos para agendar citas manualmente.", "error");
    location.hash = `#/${getDefaultRouteByRole()}`;
    return;
  }
  viewRoot.innerHTML = `
    ${pageHeader("Agendar Cita", "Registre manualmente una cita para un paciente")}
    <section class="panel">
      <h3>Datos del paciente</h3>
      <form id="manualAppointmentForm">
        <div class="grid-2">
          <div class="field"><label class="required">Documento paciente</label><input name="pacienteId" type="number" required /></div>
          <div class="field"><label class="required">Nombres</label><input name="nombrePaciente" required /></div>
          <div class="field"><label class="required">Apellidos</label><input name="apellidoPaciente" required /></div>
          <div class="field"><label class="required">Celular</label><input name="telefono" type="number" required /></div>
          <div class="field"><label>Fecha nacimiento</label><input name="fechaNacimiento" type="date" value="1998-01-01" /></div>
          <div class="field"><label>Género</label><select name="genero"><option>MASCULINO</option><option>FEMENINO</option><option>OTRO</option></select></div>
        </div>
        <div class="field"><label>Correo electrónico</label><input name="correo" type="email" /></div>
        <h3>Datos de la cita</h3>
        <div class="grid-3">
          <div class="field"><label class="required">Médico/Terapeuta</label><select name="especialistaId" required>${specialistOptions()}</select></div>
          <div class="field"><label class="required">Fecha</label><input name="fecha" type="date" required /></div>
          <div class="field"><label class="required">Hora</label><input name="hora" type="time" required /></div>
        </div>
        <div class="actions"><button class="btn btn-primary">Agendar</button><button class="btn" type="reset">Limpiar</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
  `;
  $("#manualAppointmentForm").addEventListener("submit", async event => {
    event.preventDefault();
    const f = new FormData(event.currentTarget);
    const payload = Object.fromEntries(f.entries());
    payload.pacienteId = Number(payload.pacienteId);
    payload.telefono = Number(payload.telefono);
    payload.hora = String(payload.hora).slice(0, 5);
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
        <div class="grid-2"><div class="field"><label>Fecha inicio</label><input name="inicio" type="date" /></div><div class="field"><label>Fecha fin</label><input name="fin" type="date" /></div></div>
        <div class="field"><label>Estado</label><select name="estado"><option value="">Todas</option><option>AGENDADA</option><option>REAGENDADA</option><option>CANCELADA</option></select></div>
        <div class="actions"><button class="btn btn-primary">Buscar</button><button class="btn" type="reset">Limpiar filtros</button><a class="btn" href="#/inicio">Volver</a></div>
      </form>
    </section>
    <section class="panel"><h3 id="appointmentsFoundTitle">Citas encontradas (${state.citas.length})</h3><div id="appointmentsTable"></div></section>
  `;
  const render = citas => {
    $("#appointmentsFoundTitle").textContent = `Citas encontradas (${citas.length})`;
    $("#appointmentsTable").innerHTML = citas.length ? `<div class="table-wrap"><table><thead><tr><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Tipo de atención</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${citas.map(c => `<tr><td>${formatDate(c.fecha)}</td><td>${formatTime(c.hora)}</td><td>${escapeHtml(c.pacienteNombre || `Paciente ${c.pacienteId}`)}</td><td>Consulta médica</td><td>${statusBadge(c.estado)}</td><td><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button></td></tr>`).join("")}</tbody></table></div>` : emptyState("No hay citas encontradas");
    document.querySelectorAll("[data-detail]").forEach(btn => btn.addEventListener("click", () => openDetailModal(state.citas.find(c => c.id === btn.dataset.detail))));
  };
  render(state.citas);
  $("#appointmentFilters").addEventListener("submit", event => {
    event.preventDefault();
    const f = new FormData(event.currentTarget);
    const filtered = state.citas.filter(c => (!f.get("estado") || c.estado === f.get("estado")) && (!f.get("inicio") || c.fecha >= f.get("inicio")) && (!f.get("fin") || c.fecha <= f.get("fin")));
    render(filtered);
  });
}

function renderCitasMedico() {
  viewRoot.innerHTML = `
    ${pageHeader("Mis Citas Programadas", "Consulte sus citas como profesional de la salud")}
    <section class="panel"><h3>Filtros de búsqueda</h3><div class="field"><label>Profesional</label><select id="doctorAppointmentFilter">${specialistOptions(state.especialistas[0]?.id)}</select></div><div class="grid-2"><div class="field"><label>Fecha inicio</label><input type="date" id="doctorStart" /></div><div class="field"><label>Fecha fin</label><input type="date" id="doctorEnd" /></div></div><div class="field"><label>Estado</label><select id="doctorStatus"><option value="">Todas</option><option>AGENDADA</option><option>REAGENDADA</option><option>CANCELADA</option></select></div><div class="actions"><button class="btn" id="clearDoctorFilters">Limpiar filtros</button><a class="btn" href="#/inicio">Volver</a></div></section>
    <section class="panel"><h3 id="doctorFoundTitle">Citas encontradas</h3><div id="doctorAppointmentsTable"></div></section>
  `;
  const filter = () => {
    const doc = $("#doctorAppointmentFilter").value;
    const start = $("#doctorStart").value;
    const end = $("#doctorEnd").value;
    const status = $("#doctorStatus").value;
    const citas = state.citas.filter(c => (!doc || c.especialistaId === doc) && (!status || c.estado === status) && (!start || c.fecha >= start) && (!end || c.fecha <= end));
    $("#doctorFoundTitle").textContent = `Citas encontradas (${citas.length})`;
    $("#doctorAppointmentsTable").innerHTML = citas.length ? `<div class="table-wrap"><table><thead><tr><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Tipo de atención</th><th>Estado</th><th>Acción</th></tr></thead><tbody>${citas.map(c => `<tr><td>${formatDate(c.fecha)}</td><td>${formatTime(c.hora)}</td><td>${escapeHtml(c.pacienteNombre || `Paciente ${c.pacienteId}`)}</td><td>Consulta médica</td><td>${statusBadge(c.estado)}</td><td><button class="btn btn-primary btn-small" data-detail="${escapeHtml(c.id)}">Ver detalle</button></td></tr>`).join("")}</tbody></table></div>` : emptyState("No hay citas para este profesional");
    document.querySelectorAll("[data-detail]").forEach(btn => btn.addEventListener("click", () => openDetailModal(state.citas.find(c => c.id === btn.dataset.detail))));
  };
  ["#doctorAppointmentFilter", "#doctorStart", "#doctorEnd", "#doctorStatus"].forEach(sel => $(sel).addEventListener("change", filter));
  $("#clearDoctorFilters").addEventListener("click", () => { $("#doctorStart").value = ""; $("#doctorEnd").value = ""; $("#doctorStatus").value = ""; filter(); });
  filter();
}

function exportCitasCsv() {
  const rows = [["id", "pacienteId", "pacienteNombre", "especialistaId", "especialistaNombre", "fecha", "hora", "estado"], ...state.citas.map(c => [c.id, c.pacienteId, c.pacienteNombre, c.especialistaId, c.especialistaNombre, c.fecha, c.hora, c.estado])];
  const csv = rows.map(row => row.map(v => `"${String(v ?? "").replaceAll('"', '""')}"`).join(",")).join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `citas-piedrazul-${new Date().toISOString().slice(0, 10)}.csv`;
  link.click();
  URL.revokeObjectURL(url);
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
    "intervalo-citas": renderIntervaloCitas,
    "ventana-agendamiento": renderVentanaAgendamiento,

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

    exportCitasCsv();
  });
  document.querySelector("#logoutButton").addEventListener("click", logout);
  window.addEventListener("hashchange", route);
  route();
}

init();
