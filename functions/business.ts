// One instance per business. Holds the WebSocket connections of every phone of the
// business and fans out {"type":"changed"} pings when the Worker reports new data.
// The data itself lives in Supabase Postgres (see fv_sync).
import { DurableObject } from "cloudflare:workers";

export class Business extends DurableObject {
  private broadcast(payload: unknown): void {
    const msg = JSON.stringify(payload);
    for (const ws of this.ctx.getWebSockets()) {
      try {
        ws.send(msg);
      } catch {
        // socket already gone
      }
    }
  }

  override async fetch(request: Request): Promise<Response> {
    const url = new URL(request.url);

    if (request.headers.get("Upgrade") === "websocket") {
      const pair = new WebSocketPair();
      const [client, server] = Object.values(pair);
      this.ctx.acceptWebSocket(server);
      return new Response(null, { status: 101, webSocket: client });
    }

    if (url.pathname === "/broadcast" && request.method === "POST") {
      this.broadcast({ type: "changed" });
      return Response.json({ ok: true });
    }

    return new Response("not found", { status: 404 });
  }

  override webSocketMessage(ws: WebSocket, message: string | ArrayBuffer): void {
    const text = typeof message === "string" ? message : new TextDecoder().decode(message);
    if (text === "ping") ws.send("pong");
  }

  override webSocketClose(ws: WebSocket, code: number): void {
    try {
      ws.close(code === 1005 ? 1000 : code, "bye");
    } catch {
      // ignore
    }
  }
}
