import { NextRequest } from "next/server";

const catalogApiBaseUrl = process.env.CATALOG_API_BASE_URL ?? "http://localhost:8083";

export async function GET(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const { path } = await context.params;
  const upstreamUrl = new URL(`/api/v1/catalog/${path.join("/")}`, catalogApiBaseUrl);
  upstreamUrl.search = request.nextUrl.search;

  try {
    const upstreamResponse = await fetch(upstreamUrl, { cache: "no-store" });
    return new Response(upstreamResponse.body, {
      status: upstreamResponse.status,
      headers: {
        "Content-Type": upstreamResponse.headers.get("Content-Type") ?? "application/json"
      }
    });
  } catch {
    return Response.json({ message: "Catalog service is unavailable" }, { status: 503 });
  }
}
