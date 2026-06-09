const KEYCLOAK_URL = "http://localhost:8085";
const KEYCLOAK_REALM = "piedrazul";
const KEYCLOAK_CLIENT_ID = "piedrazul-frontend";

const API_URLS = window.PIEDRAZUL_API_URLS || {
  auth: "http://localhost:8080",
  especialistas: "http://localhost:8081",
  citas: "http://localhost:8082"
};

const api = (() => {
  async function request(base, path, options = {}) {
    const token = localStorage.getItem("piedrazulAccessToken");

    const response = await fetch(`${base}${path}`, {
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
      },
      ...options
    });

    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    let data = null;
    try {
      data = isJson ? await response.json() : await response.text();
    } catch (_) {
      data = null;
    }

    if (!response.ok) {
      const message = typeof data === "string" && data.trim()
        ? data
        : data?.message || data?.error || "Error en la solicitud";
      throw new Error(message);
    }

    return data;
  }

  return {
    login: async payload => {
      const body = new URLSearchParams();
      body.append("client_id", KEYCLOAK_CLIENT_ID);
      body.append("grant_type", "password");
      body.append("username", String(payload.id));
      body.append("password", String(payload.passwordHash));

      const response = await fetch(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body
      });

      if (!response.ok) {
        let errorData = null;
        try {
          errorData = await response.json();
        } catch (_) {
          errorData = null;
        }

        throw new Error(errorData?.error_description || errorData?.error || "Credenciales invalidas");
      }

      return await response.json();
    },
    register: payload => request(API_URLS.auth, "/auth/register", { method: "POST", body: JSON.stringify(payload) }),
    getPaciente: id => request(API_URLS.auth, `/auth/pacientes/${id}`),
    buscarPacientes: documento => request(API_URLS.auth, `/auth/pacientes?documento=${encodeURIComponent(documento)}`),

    getEspecialistas: () => request(API_URLS.especialistas, "/especialistas"),
    getEspecialista: id => request(API_URLS.especialistas, `/especialistas/${id}`),
    createEspecialista: payload => request(API_URLS.especialistas, "/especialistas", { method: "POST", body: JSON.stringify(payload) }),
    saveDisponibilidad: (especialistaId, payload) => request(API_URLS.especialistas, `/disponibilidad/${especialistaId}`, { method: "POST", body: JSON.stringify(payload) }),
    getDisponibilidad: especialistaId => request(API_URLS.especialistas, `/disponibilidad/${especialistaId}`),

    getCitas: () => request(API_URLS.citas, "/citas"),
    getHorarios: (especialistaId, fecha) => request(API_URLS.citas, `/citas/horarios?especialistaId=${encodeURIComponent(especialistaId)}&fecha=${encodeURIComponent(fecha)}`),
    agendarPaciente: payload => request(API_URLS.citas, "/citas/agendar/paciente", { method: "POST", body: JSON.stringify(payload) }),
    agendarAgendador: payload => request(API_URLS.citas, "/citas/agendar/agendador", { method: "POST", body: JSON.stringify(payload) }),
    cancelarCita: id => request(API_URLS.citas, `/citas/cancelar/${id}`, { method: "PUT" }),
    cambiarEstadoCita: (id, estado) => request(API_URLS.citas, `/citas/${encodeURIComponent(id)}/estado?estado=${encodeURIComponent(estado)}`, { method: "PUT" }),
    reagendarCita: (id, payload) => request(API_URLS.citas, `/citas/reagendar/${id}`, { method: "PUT", body: JSON.stringify(payload) })
  };
})();
