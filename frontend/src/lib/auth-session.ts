const ACCESS_TOKEN_KEY = "inridemart.access-token";

export function readAccessToken(): string {
  if (typeof window === "undefined") return "";
  const token = window.localStorage.getItem(ACCESS_TOKEN_KEY) ?? "";
  if (!token || !isExpired(token)) return token;
  clearAccessToken();
  return "";
}

export function saveAccessToken(token: string): void {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken(): void {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
}

export function accessTokenUserId(token: string): string {
  const subject = tokenPayload(token)?.sub;
  return typeof subject === "string" && subject.length > 0 ? subject : "";
}

function isExpired(token: string): boolean {
  const expiresAt = tokenPayload(token)?.exp;
  return typeof expiresAt !== "number" || expiresAt * 1000 <= Date.now();
}

function tokenPayload(token: string): { exp?: number; sub?: string } | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    return JSON.parse(window.atob(padded)) as { exp?: number; sub?: string };
  } catch {
    return null;
  }
}
