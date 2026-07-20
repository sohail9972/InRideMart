import { NextRequest } from "next/server";

const bases: Record<string, string> = {
  auth: process.env.AUTH_API_BASE_URL ?? "http://localhost:8081",
  customer: process.env.CUSTOMER_API_BASE_URL ?? "http://localhost:8082",
  catalog: process.env.CATALOG_API_BASE_URL ?? "http://localhost:8083",
  cart: process.env.CART_API_BASE_URL ?? "http://localhost:8084",
  orders: process.env.ORDER_API_BASE_URL ?? "http://localhost:8085",
  ai: process.env.AI_API_BASE_URL ?? "http://localhost:8086",
  payments: process.env.PAYMENT_API_BASE_URL ?? "http://localhost:8087"
};

const roots: Record<string, string> = {
  auth: "auth",
  customer: "customers",
  catalog: "catalog",
  cart: "carts",
  orders: "orders",
  ai: "ai",
  payments: "payments"
};

export async function forwardServiceRequest(request: NextRequest, service: string, path: string[] = []) {
  const base = bases[service];
  const root = roots[service];
  if (!base || !root) return Response.json({ message: "Unknown service" }, { status: 404 });

  const suffix = path.length === 0 ? "" : `/${path.join("/")}`;
  const target = new URL(`/api/v1/${root}${suffix}`, base);
  target.search = request.nextUrl.search;
  const headers = new Headers();
  ["authorization", "content-type", "idempotency-key"].forEach((name) => {
    const value = request.headers.get(name);
    if (value) headers.set(name, value);
  });

  try {
    const response = await fetch(target, {
      method: request.method,
      headers,
      body: ["GET", "HEAD"].includes(request.method) ? undefined : await request.text(),
      cache: "no-store"
    });
    return new Response(response.body, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("Content-Type") ?? "application/json" }
    });
  } catch {
    return Response.json({ message: `${service} service is unavailable` }, { status: 503 });
  }
}
