// Public web pages required by Google Play: privacy policy and account deletion instructions.
const UPDATED = "2 de noviembre de 2026";

function page(title: string, body: string): Response {
  const html = `<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>${title} · FiaVen</title>
<style>
  body{margin:0;background:#F6F8F6;color:#1B2A22;font:16px/1.6 system-ui,-apple-system,Roboto,sans-serif}
  main{max-width:720px;margin:0 auto;padding:32px 20px 64px}
  .card{background:#fff;border:1px solid #E2E8E4;border-radius:20px;padding:24px;margin-top:16px}
  h1{font-size:28px;margin:0 0 4px;color:#1F5C42} h2{font-size:18px;margin:24px 0 6px}
  p,li{color:#33433A} small{color:#5F6F66} a{color:#2E7D5B}
</style></head><body><main>
<h1>${title}</h1><small>FiaVen · Actualizado el ${UPDATED}</small>
<div class="card">${body}</div>
<p><small>FiaVen fue creado por Samuel Martinez.</small></p>
</main></body></html>`;
  return new Response(html, { headers: { "Content-Type": "text/html; charset=utf-8", "Cache-Control": "public, max-age=3600" } });
}

export function privacyPage(): Response {
  return page(
    "Política de privacidad",
    `<p>FiaVen es una app para que negocios registren ventas, fiados, abonos, productos y la tasa del dólar. Esta política explica qué datos guarda y cómo se usan.</p>
<h2>Datos que guardamos</h2>
<ul>
<li><b>Cuenta:</b> nombre del negocio, tu nombre, usuario, contraseña (guardada cifrada, nadie puede leerla) y una pregunta de seguridad con su respuesta (también cifrada).</li>
<li><b>Datos del negocio:</b> productos y precios, ventas, fiados, abonos, nombres y teléfonos de clientes que el negocio decide anotar, y la tasa de cambio.</li>
<li><b>Datos técnicos:</b> la dirección IP se usa solo de forma temporal para evitar abusos al crear cuentas.</li>
</ul>
<p>No pedimos correo, número de teléfono del usuario, ubicación, contactos, fotos ni datos de pago. No mostramos publicidad.</p>
<h2>Para qué los usamos</h2>
<p>Solo para que la app funcione: guardar la información del negocio y sincronizarla entre los teléfonos de su equipo. No vendemos ni compartimos datos con terceros para publicidad.</p>
<h2>Dónde se guardan</h2>
<p>En el teléfono y en servidores seguros (Supabase y Cloudflare), con conexión cifrada (HTTPS). Cada negocio solo puede ver sus propios datos.</p>
<h2>Servicios de terceros</h2>
<p>La tasa oficial BCV se consulta en un servicio público (ve.dolarapi.com); esa consulta no envía datos de tu negocio.</p>
<h2>Tus derechos y eliminación</h2>
<p>Puedes eliminar tu cuenta en cualquier momento desde la app: <b>menú ⋮ → Mi cuenta → Eliminar cuenta</b>. Si eres dueño, se borran el negocio y todos sus datos. Más detalles en <a href="/eliminar-cuenta">Eliminar cuenta</a>.</p>
<h2>Menores de edad</h2>
<p>FiaVen está pensada para negocios y no está dirigida a menores de 13 años.</p>
<h2>Contacto</h2>
<p>Instagram: <a href="https://instagram.com/Samuuj_m">@Samuuj_m</a></p>`,
  );
}

export function deletePage(): Response {
  return page(
    "Eliminar cuenta",
    `<p>Puedes borrar tu cuenta de FiaVen y sus datos cuando quieras, directamente desde la app.</p>
<h2>Cómo hacerlo</h2>
<ol>
<li>Abre FiaVen y entra con tu usuario.</li>
<li>Toca el menú <b>⋮</b> arriba a la derecha y elige <b>Mi cuenta</b>.</li>
<li>Toca <b>Eliminar cuenta</b> y confirma con tu contraseña.</li>
</ol>
<h2>Qué se borra</h2>
<ul>
<li><b>Si eres el dueño:</b> se borra el negocio completo: productos, ventas, fiados, abonos, clientes, la tasa y los accesos de todos los trabajadores. No se puede deshacer.</li>
<li><b>Si eres trabajador:</b> se borra tu acceso. Las ventas que registraste siguen siendo parte de los registros del negocio.</li>
</ul>
<p>El borrado es inmediato en nuestros servidores. No guardamos copias de los datos borrados.</p>
<h2>¿No puedes entrar?</h2>
<p>Usa “¿Olvidaste tu contraseña?” en la pantalla de entrada, o escríbenos por Instagram a <a href="https://instagram.com/Samuuj_m">@Samuuj_m</a> indicando tu usuario.</p>`,
  );
}
