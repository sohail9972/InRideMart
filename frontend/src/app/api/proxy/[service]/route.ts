import { NextRequest } from "next/server";
import { forwardServiceRequest } from "@/app/api/proxy/forward";

async function proxy(request: NextRequest, context: { params: Promise<{ service: string }> }) {
  const { service } = await context.params;
  return forwardServiceRequest(request, service);
}

export const GET = proxy;
export const POST = proxy;
export const PATCH = proxy;
