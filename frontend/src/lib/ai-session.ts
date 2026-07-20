const CONVERSATION_PREFIX = "inridemart.ai.conversation";
const TRANSLATION_PREFIX = "inridemart.ai.translations";
const LEGACY_KEYS = [CONVERSATION_PREFIX, TRANSLATION_PREFIX];

type AiSessionKind = "conversation" | "translations";

export function readAiSession<T>(userId: string, kind: AiSessionKind): T | null {
  const value = sessionStorage.getItem(aiSessionKey(userId, kind));
  if (!value) return null;
  return JSON.parse(value) as T;
}

export function writeAiSession<T>(userId: string, kind: AiSessionKind, value: T): void {
  sessionStorage.setItem(aiSessionKey(userId, kind), JSON.stringify(value));
}

export function removeAiSession(userId: string, kind: AiSessionKind): void {
  sessionStorage.removeItem(aiSessionKey(userId, kind));
}

export function clearActiveAiSession(): void {
  for (const key of LEGACY_KEYS) {
    sessionStorage.removeItem(key);
    localStorage.removeItem(key);
  }
}

function aiSessionKey(userId: string, kind: AiSessionKind): string {
  return `${kind === "conversation" ? CONVERSATION_PREFIX : TRANSLATION_PREFIX}:${userId}`;
}
