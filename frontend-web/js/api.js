const API_URLS = {
  auth: "http://localhost:8080",
  especialistas: "http://localhost:8081",
  citas: "http://localhost:8082"
};

const api = (() => {
  async function request(base, path, options = {}) {
    const response = await fetch(`${base}${path}`, {
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      ...options
    });

    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
      const message = typeof data === "string" ? data : data.message || data.error || "Error en la solicitud";
      throw new Error(message);
    }
    return data;
  }

  return {
    login: payload => request(API_URLS.auth, "/auth/login", { method: "POST", body: JSON.stringify(payload) }),
    register: payload => request(API_URLS.auth, "/auth/register", { method: "POST", body: JSON.stringify(payload) }),
    getPaciente: id => request(API_URLS.auth, `/auth/pacientes/${id}`),

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
    reagendarCita: (id, payload) => request(API_URLS.citas, `/citas/reagendar/${id}`, { method: "PUT", body: JSON.stringify(payload) })
  };
})();
