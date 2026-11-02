// Accounts directory: businesses, users (username + password) and sessions.
// Single instance ("global"). Only reachable from the entrypoint (internal paths).
import { DurableObject } from "cloudflare:workers";

type UserRow = {
  username: string;
  business_id: string;
  display_name: string;
  role: string;
  salt: string;
  hash: string;
};

const ITERATIONS = 100_000;
const MAX_FAILS = 8;
const LOCK_WINDOW_MS = 15 * 60_000;

function toHex(buf: ArrayBuffer | Uint8Array): string {
  const bytes = buf instanceof Uint8Array ? buf : new Uint8Array(buf);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

function randomHex(bytes: number): string {
  const arr = new Uint8Array(bytes);
  crypto.getRandomValues(arr);
  return toHex(arr);
}

async function hashPassword(password: string, saltHex: string): Promise<string> {
  const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(password), "PBKDF2", false, [
    "deriveBits",
  ]);
  const salt = new Uint8Array(saltHex.match(/.{2}/g)!.map((h) => parseInt(h, 16)));
  const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt, iterations: ITERATIONS }, key, 256);
  return toHex(bits);
}

function safeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

export function normalizeUsername(raw: unknown): string {
  return String(raw ?? "").trim().toLowerCase();
}

export function validUsername(u: string): boolean {
  return /^[a-z0-9._-]{3,32}$/.test(u);
}

function err(message: string, status = 400): Response {
  return Response.json({ error: message }, { status });
}

export class Accounts extends DurableObject {
  constructor(ctx: DurableObjectState, env: unknown) {
    super(ctx, env as never);
    const sql = this.ctx.storage.sql;
    sql.exec(`CREATE TABLE IF NOT EXISTS businesses (id TEXT PRIMARY KEY, name TEXT NOT NULL, created_at INTEGER NOT NULL)`);
    sql.exec(`CREATE TABLE IF NOT EXISTS users (
      username TEXT PRIMARY KEY, business_id TEXT NOT NULL, display_name TEXT NOT NULL,
      role TEXT NOT NULL, salt TEXT NOT NULL, hash TEXT NOT NULL, created_at INTEGER NOT NULL)`);
    sql.exec(`CREATE TABLE IF NOT EXISTS sessions (token TEXT PRIMARY KEY, username TEXT NOT NULL, created_at INTEGER NOT NULL)`);
    sql.exec(`CREATE TABLE IF NOT EXISTS fails (username TEXT PRIMARY KEY, count INTEGER NOT NULL, first_at INTEGER NOT NULL)`);
  }

  private getUser(username: string): UserRow | null {
    const rows = this.ctx.storage.sql
      .exec<UserRow>("SELECT username, business_id, display_name, role, salt, hash FROM users WHERE username = ?", username)
      .toArray();
    return rows[0] ?? null;
  }

  private businessName(id: string): string {
    const rows = this.ctx.storage.sql.exec<{ name: string }>("SELECT name FROM businesses WHERE id = ?", id).toArray();
    return rows[0]?.name ?? "";
  }

  private createSession(username: string): string {
    const token = randomHex(32);
    this.ctx.storage.sql.exec("INSERT INTO sessions (token, username, created_at) VALUES (?, ?, ?)", token, username, Date.now());
    return token;
  }

  private sessionPayload(user: UserRow, token: string) {
    return {
      token,
      user: { username: user.username, displayName: user.display_name, role: user.role },
      business: { id: user.business_id, name: this.businessName(user.business_id) },
    };
  }

  private async createUser(businessId: string, username: string, displayName: string, role: string, password: string) {
    const salt = randomHex(16);
    const hash = await hashPassword(password, salt);
    this.ctx.storage.sql.exec(
      "INSERT INTO users (username, business_id, display_name, role, salt, hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
      username,
      businessId,
      displayName,
      role,
      salt,
      hash,
      Date.now(),
    );
  }

  override async fetch(request: Request): Promise<Response> {
    const path = new URL(request.url).pathname;
    const body = (await request.json().catch(() => ({}))) as Record<string, unknown>;
    const sql = this.ctx.storage.sql;

    if (path === "/register") {
      const businessName = String(body.businessName ?? "").trim();
      const displayName = String(body.displayName ?? "").trim();
      const username = normalizeUsername(body.username);
      const password = String(body.password ?? "");
      if (businessName.length < 2) return err("Escribe el nombre del negocio");
      if (displayName.length < 2) return err("Escribe tu nombre");
      if (!validUsername(username)) return err("El usuario debe tener 3 a 32 letras, números, punto o guion");
      if (password.length < 6) return err("La contraseña debe tener al menos 6 caracteres");
      if (this.getUser(username)) return err("Ese usuario ya existe", 409);
      const businessId = randomHex(12);
      sql.exec("INSERT INTO businesses (id, name, created_at) VALUES (?, ?, ?)", businessId, businessName, Date.now());
      await this.createUser(businessId, username, displayName, "owner", password);
      const user = this.getUser(username)!;
      return Response.json(this.sessionPayload(user, this.createSession(username)));
    }

    if (path === "/login") {
      const username = normalizeUsername(body.username);
      const password = String(body.password ?? "");
      const now = Date.now();
      const fail = sql
        .exec<{ count: number; first_at: number }>("SELECT count, first_at FROM fails WHERE username = ?", username)
        .toArray()[0];
      if (fail && now - fail.first_at < LOCK_WINDOW_MS && fail.count >= MAX_FAILS) {
        return err("Demasiados intentos. Espera unos minutos.", 429);
      }
      const user = this.getUser(username);
      const ok = user ? safeEqual(await hashPassword(password, user.salt), user.hash) : false;
      if (!user || !ok) {
        if (!fail || now - fail.first_at >= LOCK_WINDOW_MS) {
          sql.exec("INSERT OR REPLACE INTO fails (username, count, first_at) VALUES (?, 1, ?)", username, now);
        } else {
          sql.exec("UPDATE fails SET count = count + 1 WHERE username = ?", username);
        }
        return err("Usuario o contraseña incorrectos", 401);
      }
      sql.exec("DELETE FROM fails WHERE username = ?", username);
      return Response.json(this.sessionPayload(user, this.createSession(username)));
    }

    if (path === "/verify") {
      const token = String(body.token ?? "");
      const row = sql.exec<{ username: string }>("SELECT username FROM sessions WHERE token = ?", token).toArray()[0];
      if (!row) return err("Sesión inválida", 401);
      const user = this.getUser(row.username);
      if (!user) return err("Sesión inválida", 401);
      return Response.json({
        username: user.username,
        displayName: user.display_name,
        role: user.role,
        businessId: user.business_id,
        businessName: this.businessName(user.business_id),
      });
    }

    if (path === "/logout") {
      sql.exec("DELETE FROM sessions WHERE token = ?", String(body.token ?? ""));
      return Response.json({ ok: true });
    }

    if (path === "/team/list") {
      const rows = sql
        .exec<{ username: string; display_name: string; role: string }>(
          "SELECT username, display_name, role FROM users WHERE business_id = ? ORDER BY created_at",
          String(body.businessId ?? ""),
        )
        .toArray();
      return Response.json({
        users: rows.map((r) => ({ username: r.username, displayName: r.display_name, role: r.role })),
      });
    }

    if (path === "/team/add") {
      const businessId = String(body.businessId ?? "");
      const displayName = String(body.displayName ?? "").trim();
      const username = normalizeUsername(body.username);
      const password = String(body.password ?? "");
      if (displayName.length < 2) return err("Escribe el nombre del trabajador");
      if (!validUsername(username)) return err("El usuario debe tener 3 a 32 letras, números, punto o guion");
      if (password.length < 6) return err("La contraseña debe tener al menos 6 caracteres");
      if (this.getUser(username)) return err("Ese usuario ya existe", 409);
      await this.createUser(businessId, username, displayName, "worker", password);
      return Response.json({ ok: true });
    }

    if (path === "/team/remove") {
      const businessId = String(body.businessId ?? "");
      const username = normalizeUsername(body.username);
      const user = this.getUser(username);
      if (!user || user.business_id !== businessId) return err("Usuario no encontrado", 404);
      if (user.role === "owner") return err("No puedes eliminar al dueño");
      sql.exec("DELETE FROM users WHERE username = ?", username);
      sql.exec("DELETE FROM sessions WHERE username = ?", username);
      return Response.json({ ok: true });
    }

    return err("not found", 404);
  }
}
