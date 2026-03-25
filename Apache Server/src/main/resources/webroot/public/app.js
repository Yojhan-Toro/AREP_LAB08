// Helper: obtiene el token de sesión guardado
function getToken() {
    return localStorage.getItem('session_token');
}

// Helper: hace fetch con el token de autorización incluido
async function authFetch(url, options = {}) {
    const token = getToken();
    const headers = { ...options.headers };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return fetch(url, { ...options, headers });
}

async function fetchHello() {
    const name = document.getElementById('nameInput').value || 'mundo';
    const text = await fetchAndExtract('/App/hello?name=' + encodeURIComponent(name));
    document.getElementById('result').textContent = text;
}

async function fetchEndpoint(url) {
    const text = await fetchAndExtract(url);
    document.getElementById('result2').textContent = text;
}

async function fetchAndExtract(url) {
    try {
        const res  = await authFetch(url);
        if (res.status === 401) {
            localStorage.removeItem('session_token');
            localStorage.removeItem('session_user');
            window.location.href = '/login.html';
            return '';
        }
        const html = await res.text();
        const doc  = new DOMParser().parseFromString(html, 'text/html');
        return doc.body.textContent.trim();
    } catch (e) {
        return 'Error: ' + e.message;
    }
}

async function shutdownServer() {
    const msg = document.getElementById('shutdownMsg');
    msg.textContent = 'Enviando señal...';
    try {
        await authFetch('/shutdown');
        msg.textContent = 'Servidor apagándose. Esta página dejará de responder.';
    } catch (e) {
        msg.textContent = 'Servidor apagándose. Esta página dejará de responder.';
    }
}
