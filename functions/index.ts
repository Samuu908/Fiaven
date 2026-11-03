// FiaVen backend.
// - Data (users, sessions, synced records) lives in Supabase Postgres and is only
//   reachable through SECURITY DEFINER functions (fv_login, fv_sync, ...) that check
//   the session token themselves. Tables have RLS on and no grants.
// - The Business Durable Object only fans out "changed" pings over WebSockets so every
//   phone of the business pulls new data in real time.
export { Accounts } from "./accounts"; // legacy, unused (kept so existing DO storage isn't orphaned)
export { Business } from "./business";
import { deletePage, privacyPage } from "./pages";

type Env = {
  DO: Fetcher;
  EXPO_PUBLIC_SUPABASE_URL: string;
  EXPO_PUBLIC_SUPABASE_ANON_KEY: string;
};

type Identity = {
  username: string;
  displayName: string;
  role: string;
  businessId: string;
  businessName: string;
};

type RpcResult = Record<string, unknown> & { error?: string; status?: number };

async function rpc(env: Env, fn: string, args: Record<string, unknown>): Promise<RpcResult> {
  const res = await fetch(`${env.EXPO_PUBLIC_SUPABASE_URL}/rest/v1/rpc/${fn}`, {
    method: "POST",
    headers: {
      apikey: env.EXPO_PUBLIC_SUPABASE_ANON_KEY,
      Authorization: `Bearer ${env.EXPO_PUBLIC_SUPABASE_ANON_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(args),
  });
  if (!res.ok) {
    console.log("rpc failed", fn, res.status, await res.text().catch(() => ""));
    return { error: "Error del servidor, inténtalo de nuevo", status: 502 };
  }
  return (await res.json()) as RpcResult;
}

function reply(result: RpcResult): Response {
  if (result.error) return Response.json({ error: result.error }, { status: result.status ?? 400 });
  return Response.json(result);
}

function bearer(request: Request): string {
  const header = request.headers.get("Authorization") ?? "";
  if (header.startsWith("Bearer ")) return header.slice(7).trim();
  return new URL(request.url).searchParams.get("token") ?? "";
}

async function identify(token: string, env: Env): Promise<Identity | null> {
  if (!token) return null;
  const r = await rpc(env, "fv_verify", { p_token: token });
  return r.error ? null : (r as unknown as Identity);
}

function business(env: Env, businessId: string, request: Request): Promise<Response> {
  const wrapped = new Request(request.url, request);
  wrapped.headers.delete("Authorization");
  wrapped.headers.set("X-Rork-DO-Class", "Business");
  wrapped.headers.set("X-Rork-DO-Id", businessId);
  return env.DO.fetch(wrapped);
}

async function json(request: Request): Promise<Record<string, unknown>> {
  return ((await request.json().catch(() => ({}))) ?? {}) as Record<string, unknown>;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;
    const token = bearer(request);

    if (path === "/ping") return Response.json({ ok: true, now: Date.now() });
    if (path === "/privacidad" || path === "/privacy") return privacyPage();
    if (path === "/eliminar-cuenta") return deletePage();

    if (request.method === "POST" && path === "/auth/recover/question") {
      const b = await json(request);
      return reply(await rpc(env, "fv_recovery_question", { p_username: String(b.username ?? "") }));
    }

    if (request.method === "POST" && path === "/auth/recover") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_recover", {
          p_username: String(b.username ?? ""),
          p_answer: String(b.answer ?? ""),
          p_new_password: String(b.newPassword ?? ""),
        }),
      );
    }

    if (path === "/account/security" && request.method === "GET") {
      return reply(await rpc(env, "fv_security_get", { p_token: token }));
    }

    if (path === "/account/security" && request.method === "POST") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_security_set", {
          p_token: token,
          p_password: String(b.password ?? ""),
          p_question: String(b.question ?? ""),
          p_answer: String(b.answer ?? ""),
        }),
      );
    }

    if (path === "/account/update" && request.method === "POST") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_account_update", {
          p_token: token,
          p_password: String(b.password ?? ""),
          p_username: String(b.username ?? ""),
          p_display_name: String(b.displayName ?? ""),
          p_new_password: String(b.newPassword ?? ""),
        }),
      );
    }

    if (path === "/account/delete" && request.method === "POST") {
      const b = await json(request);
      const result = await rpc(env, "fv_account_delete", { p_token: token, p_password: String(b.password ?? "") });
      if (!result.error && result.businessDeleted && typeof result.businessId === "string") {
        // Other phones of the deleted business pull, get 401 and sign out.
        await business(env, result.businessId, new Request(`${url.origin}/broadcast`, { method: "POST" })).catch(() => undefined);
      }
      return reply(result.error ? result : { ok: true });
    }

    if (path === "/credits" && request.method === "GET") {
      return reply(await rpc(env, "fv_credits_get", { p_token: token }));
    }

    if (path === "/credits" && request.method === "POST") {
      const b = await json(request);
      return reply(await rpc(env, "fv_credits_set", { p_token: token, p_credits: b.credits ?? null }));
    }

    if (request.method === "POST" && path === "/auth/register") {
      // Anyone can create their own business; they become its owner.
      const b = await json(request);
      return reply(
        await rpc(env, "fv_register", {
          p_business_name: String(b.businessName ?? ""),
          p_display_name: String(b.displayName ?? ""),
          p_username: String(b.username ?? ""),
          p_password: String(b.password ?? ""),
          p_question: String(b.question ?? ""),
          p_answer: String(b.answer ?? ""),
          p_ip: request.headers.get("CF-Connecting-IP") ?? "",
        }),
      );
    }

    if (request.method === "POST" && path === "/auth/login") {
      const body = await json(request);
      return reply(await rpc(env, "fv_login", { p_username: String(body.username ?? ""), p_password: String(body.password ?? "") }));
    }

    if (request.method === "POST" && path === "/auth/logout") {
      return reply(await rpc(env, "fv_logout", { p_token: token }));
    }

    if (path === "/me") return reply(await rpc(env, "fv_verify", { p_token: token }));

    if (path === "/team" && request.method === "GET") return reply(await rpc(env, "fv_team_list", { p_token: token }));

    if (path === "/team/add" && request.method === "POST") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_team_add", {
          p_token: token,
          p_display_name: String(b.displayName ?? ""),
          p_username: String(b.username ?? ""),
          p_password: String(b.password ?? ""),
          p_title: String(b.title ?? ""),
        }),
      );
    }

    if (path === "/team/title" && request.method === "POST") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_team_set_title", {
          p_token: token,
          p_username: String(b.username ?? ""),
          p_title: String(b.title ?? ""),
        }),
      );
    }

    if (path === "/team/update" && request.method === "POST") {
      const b = await json(request);
      return reply(
        await rpc(env, "fv_team_update", {
          p_token: token,
          p_username: String(b.username ?? ""),
          p_display_name: String(b.displayName ?? ""),
          p_new_password: String(b.newPassword ?? ""),
        }),
      );
    }

    if (path === "/attachment" && request.method === "POST") {
      const b = await json(request);
      return reply(await rpc(env, "fv_file_put", { p_token: token, p_id: String(b.id ?? ""), p_data: String(b.data ?? "") }));
    }

    if (path.startsWith("/attachment/") && request.method === "GET") {
      return reply(await rpc(env, "fv_file_get", { p_token: token, p_id: decodeURIComponent(path.slice(12)) }));
    }

    if (path === "/team/remove" && request.method === "POST") {
      const b = await json(request);
      return reply(await rpc(env, "fv_team_remove", { p_token: token, p_username: String(b.username ?? "") }));
    }

    if (path === "/sync" && request.method === "POST") {
      const b = await json(request);
      const since = Number(b.since ?? 0) || 0;
      const changes = Array.isArray(b.changes) ? b.changes.slice(0, 500) : [];
      const result = await rpc(env, "fv_sync", { p_token: token, p_since: since, p_changes: changes });
      if (result.error) return reply(result);
      if (Number(result.applied ?? 0) > 0) {
        const me = await identify(token, env);
        if (me) {
          await business(env, me.businessId, new Request(`${url.origin}/broadcast`, { method: "POST" })).catch(() => undefined);
        }
      }
      const { applied: _applied, ...out } = result;
      return Response.json(out);
    }

    if (path === "/ws") {
      const me = await identify(token, env);
      if (!me) return Response.json({ error: "Sesión inválida" }, { status: 401 });
      const target = new URL(request.url);
      target.searchParams.delete("token");
      return business(env, me.businessId, new Request(target.toString(), request));
    }

    return Response.json({ error: "not found" }, { status: 404 });
  },
} satisfies ExportedHandler<Env>;
