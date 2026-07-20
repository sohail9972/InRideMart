"use client";

import { Bot, Languages, LoaderCircle, LogIn, MapPin, Send, ShoppingCart, Sparkles, UserRound } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useState } from "react";
import { accessTokenUserId, clearAccessToken, readAccessToken, saveAccessToken } from "@/lib/auth-session";
import { clearActiveAiSession, readAiSession, removeAiSession, writeAiSession } from "@/lib/ai-session";
import { ASSIGNED_CAB, type RideContext, DEFAULT_RIDE_CONTEXT, readRideContext } from "@/lib/ride-context";

type Product = {
  id: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  category: string;
  imageUrl?: string;
  reason?: string;
};
type Bundle = { name: string; products: Product[]; totalAmount: number };
type Message = { role: "user" | "assistant"; text: string; products?: Product[]; reason?: string; bundle?: Bundle };
type ConversationTurn = { role: "user" | "assistant"; message: string };
type TranslationExchange = {
  passengerMessage: string;
  passengerLanguage: string;
  driverLanguage: string;
  driverTranslation: string;
  driverReply: string;
  passengerTranslation: string;
  supported: boolean;
  fallbackUsed: boolean;
  fallbackReason?: string | null;
};
type AssistantMode = "shopping" | "translate";

const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 });
const DRIVER_LANGUAGES = [
  ["ENGLISH", "English"], ["HINDI", "Hindi"], ["KANNADA", "Kannada"], ["TELUGU", "Telugu"], ["TAMIL", "Tamil"], ["MALAYALAM", "Malayalam"], ["MARATHI", "Marathi"], ["BENGALI", "Bengali"], ["GUJARATI", "Gujarati"], ["PUNJABI", "Punjabi"], ["URDU", "Urdu"],
  ["SPANISH", "Spanish"], ["FRENCH", "French"], ["GERMAN", "German"], ["PORTUGUESE", "Portuguese"], ["ITALIAN", "Italian"], ["DUTCH", "Dutch"], ["RUSSIAN", "Russian"], ["ARABIC", "Arabic"], ["TURKISH", "Turkish"], ["CHINESE", "Chinese"], ["JAPANESE", "Japanese"], ["KOREAN", "Korean"], ["THAI", "Thai"], ["VIETNAMESE", "Vietnamese"], ["INDONESIAN", "Indonesian"]
] as const;

export function AiShopping() {
  const router = useRouter();
  const [token, setToken] = useState("");
  const userId = accessTokenUserId(token);
  const [hydratedUserId, setHydratedUserId] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);
  const [busy, setBusy] = useState(false);
  const [assistantMode, setAssistantMode] = useState<AssistantMode>("shopping");
  const [error, setError] = useState("");
  const [ride, setRide] = useState<RideContext>(DEFAULT_RIDE_CONTEXT);
  const [translationPrompt, setTranslationPrompt] = useState("");
  const [driverLanguage, setDriverLanguage] = useState("KANNADA");
  const [driverReply, setDriverReply] = useState("");
  const [translations, setTranslations] = useState<TranslationExchange[]>([]);
  const [translationBusy, setTranslationBusy] = useState(false);

  useEffect(() => {
    queueMicrotask(() => {
      clearActiveAiSession();
      setToken(readAccessToken());
      setRide(readRideContext());
    });
  }, []);

  useEffect(() => {
    queueMicrotask(() => {
      setMessages([]);
      setTranslations([]);
      setPrompt("");
      setTranslationPrompt("");
      setDriverReply("");
      setError("");
      setAssistantMode("shopping");

      if (!userId) {
        setHydratedUserId("");
        return;
      }

      try {
        setMessages(readAiSession<Message[]>(userId, "conversation") ?? []);
        setTranslations(readAiSession<TranslationExchange[]>(userId, "translations") ?? []);
      } catch {
        removeAiSession(userId, "conversation");
        removeAiSession(userId, "translations");
      }

      setHydratedUserId(userId);
    });
  }, [userId]);

  useEffect(() => {
    if (!userId || hydratedUserId !== userId) return;

    if (messages.length > 0) {
      writeAiSession(userId, "conversation", messages);
    } else {
      removeAiSession(userId, "conversation");
    }
  }, [hydratedUserId, messages, userId]);

  useEffect(() => {
    if (!userId || hydratedUserId !== userId) return;

    if (translations.length > 0) {
      writeAiSession(userId, "translations", translations);
    } else {
      removeAiSession(userId, "translations");
    }
  }, [hydratedUserId, translations, userId]);

  function clearLiveAiState() {
    clearActiveAiSession();
    setMessages([]);
    setTranslations([]);
    setPrompt("");
    setTranslationPrompt("");
    setDriverReply("");
    setAssistantMode("shopping");
    setError("");
    setHydratedUserId("");
  }

  function expireSession() {
    clearAccessToken();
    clearLiveAiState();
    setToken("");
    router.replace(`/login?next=${encodeURIComponent("/ai")}`);
  }

  async function login(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await fetch("/api/proxy/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message ?? "Login failed");
      saveAccessToken(data.accessToken);
      setToken(data.accessToken);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Login failed");
    } finally {
      setBusy(false);
    }
  }

  async function ask(event: FormEvent) {
    event.preventDefault();
    const text = prompt.trim();
    if (!token || !text) return;
    const history: ConversationTurn[] = messages.map((message) => ({ role: message.role, message: message.text }));
    setPrompt("");
    setMessages((value) => [...value, { role: "user", text }]);
    setBusy(true);
    setError("");
    try {
      const response = await fetch("/api/proxy/ai/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          message: text,
          conversation: history,
          destination: ride.destination,
          weather: ride.weather,
          journeyDurationMinutes: ride.journeyDurationMinutes,
          travelPurpose: ride.travelPurpose,
          timeOfDay: timeOfDay(),
          availableProductIds: ASSIGNED_CAB.availableProductIds
        })
      });
      const data = await response.json();
      if (response.status === 401 || response.status === 403) {
        expireSession();
        throw new Error("Your session expired. Sign in again.");
      }
      if (!response.ok) throw new Error(data.message ?? "Assistant is unavailable");
      setMessages((value) => [...value, {
        role: "assistant",
        text: data.assistantMessage,
        products: data.recommendedProducts ?? [],
        reason: data.recommendationReason,
        bundle: data.suggestedBundle ?? undefined
      }]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Assistant is unavailable");
    } finally {
      setBusy(false);
    }
  }

  async function add(productId: string) {
    if (!token) return;
    try {
      const response = await addToCart(productId);
      if (response.status === 401 || response.status === 403) {
        expireSession();
        throw new Error("Your session expired. Sign in again.");
      }
      if (!response.ok) throw new Error("Could not add this product to your cart.");
      setError("Added to your cart.");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not add this product to your cart.");
    }
  }

  async function addBundle(bundle: Bundle) {
    if (!token) return;
    setBusy(true);
    setError("");
    try {
      for (const product of bundle.products) {
        const response = await addToCart(product.id);
        if (response.status === 401 || response.status === 403) {
          expireSession();
          throw new Error("Your session expired. Sign in again.");
        }
        if (!response.ok) throw new Error(`Could not add ${product.name}.`);
      }
      setError(`${bundle.name} added to your cart.`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not add the bundle.");
    } finally {
      setBusy(false);
    }
  }

  async function translatePassenger(event: FormEvent) {
    event.preventDefault();
    const passengerMessage = translationPrompt.trim();
    if (!token || !passengerMessage) return;
    setTranslationPrompt("");
    setTranslationBusy(true);
    setError("");
    try {
      const response = await fetch("/api/proxy/ai/translate", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ passengerMessage, driverLanguage })
      });
      const data = await response.json();
      if (response.status === 401 || response.status === 403) {
        expireSession();
        throw new Error("Your session expired. Sign in again.");
      }
      if (!response.ok) throw new Error(data.message ?? "Translation is unavailable");
      setTranslations((value) => [...value, data as TranslationExchange]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Translation is unavailable");
    } finally {
      setTranslationBusy(false);
    }
  }

  async function translateDriverReply(event: FormEvent) {
    event.preventDefault();
    const reply = driverReply.trim();
    const exchange = translations.at(-1);
    if (!token || !reply || !exchange) return;
    setDriverReply("");
    setTranslationBusy(true);
    setError("");
    try {
      const response = await fetch("/api/proxy/ai/translate", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ passengerMessage: exchange.passengerMessage, driverLanguage: exchange.driverLanguage, driverReply: reply })
      });
      const data = await response.json();
      if (response.status === 401 || response.status === 403) {
        expireSession();
        throw new Error("Your session expired. Sign in again.");
      }
      if (!response.ok) throw new Error(data.message ?? "Translation is unavailable");
      setTranslations((value) => [...value.slice(0, -1), data as TranslationExchange]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Translation is unavailable");
    } finally {
      setTranslationBusy(false);
    }
  }

  function addToCart(productId: string) {
    return fetch("/api/proxy/cart/me/items", {
      method: "POST",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify({ productId, quantity: 1 })
    });
  }

  function logout() {
    clearAccessToken();
    clearLiveAiState();
    setToken("");
  }

  return <main className="min-h-screen">
    <header className="border-b border-[#dbe4ee] bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
        <Link href="/home" className="font-bold text-[#172033]">InRideMart</Link>
        <span className="flex items-center gap-2 text-sm text-[#007f82]"><Sparkles size={16} /> AI Shopping</span>
      </div>
    </header>
    <div className="mx-auto grid max-w-5xl gap-6 px-4 py-6 lg:grid-cols-[280px_1fr]">
      <aside className="rounded-lg border border-[#dbe4ee] bg-white p-4">
        <h1 className="text-lg font-bold">Ride assistant</h1>
        <p className="mt-1 text-sm text-[#64748b]">Tell me what your journey needs. I will ask a question when I need more context.</p>
        <div className="mt-4 rounded-md bg-[#f2f8f7] p-3 text-sm"><p className="flex items-center gap-1 font-semibold text-[#075c64]"><MapPin size={15} /> {ride.destination}</p><p className="mt-1 text-[#46566e]">{ride.journeyDurationMinutes} min | {ride.weather} | {ride.travelPurpose}</p><p className="mt-2 text-xs text-[#64748b]">Only {ASSIGNED_CAB.availableProductIds.length} products stocked in this cab can be recommended.</p></div>
        {!token ? <form onSubmit={login} className="mt-5 grid gap-3">
          <input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Email" className="rounded-md border border-[#cbd7e3] px-3 py-2" />
          <input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" className="rounded-md border border-[#cbd7e3] px-3 py-2" />
          <button disabled={busy} className="flex items-center justify-center gap-2 rounded-md bg-[#007f82] px-3 py-2 font-medium text-white"><LogIn size={16} /> Sign in</button>
        </form> : <div className="mt-5 flex items-center justify-between gap-3"><p className="text-sm font-medium text-[#007f82]">Signed in and ready to shop.</p><button onClick={logout} className="text-sm text-[#64748b]">Log out</button></div>}
      </aside>
      <section className="flex min-h-[520px] flex-col rounded-lg border border-[#dbe4ee] bg-white">
        <div role="tablist" aria-label="AI assistant modes" className="flex border-b border-[#dbe4ee] p-2">
          <button type="button" role="tab" aria-selected={assistantMode === "shopping"} onClick={() => setAssistantMode("shopping")} className={assistantMode === "shopping" ? "rounded-md bg-[#e7f5f3] px-3 py-2 text-sm font-semibold text-[#075c64]" : "rounded-md px-3 py-2 text-sm text-[#64748b]"}>Shopping assistant</button>
          <button type="button" role="tab" aria-selected={assistantMode === "translate"} onClick={() => setAssistantMode("translate")} className={assistantMode === "translate" ? "ml-2 flex items-center gap-2 rounded-md bg-[#e7f5f3] px-3 py-2 text-sm font-semibold text-[#075c64]" : "ml-2 flex items-center gap-2 rounded-md px-3 py-2 text-sm text-[#64748b]"}><Languages size={16} /> Translate conversation</button>
        </div>
        {assistantMode === "shopping" ? <>
        <div className="flex-1 space-y-4 p-4">
          {messages.length === 0 ? <div><p className="text-sm text-[#64748b]">I can help with this ride, but I will not guess when context is missing.</p><div className="mt-4 flex flex-wrap gap-2">{["Hi", "I forgot my charger", "I am hungry", "It is raining"].map((suggestion) => <button key={suggestion} onClick={() => setPrompt(suggestion)} className="rounded-full border border-[#cbd7e3] px-3 py-1.5 text-sm text-[#075c64]">{suggestion}</button>)}</div></div> : null}
          {messages.map((message, index) => <div key={`${message.role}-${index}`} className={message.role === "user" ? "ml-auto flex max-w-[90%] flex-row-reverse gap-2" : "flex max-w-[95%] gap-2"}>
            <span className={message.role === "user" ? "grid size-8 shrink-0 place-items-center rounded-full bg-[#075c64] text-white" : "grid size-8 shrink-0 place-items-center rounded-full bg-[#e7f5f3] text-[#007f82]"}>{message.role === "user" ? <UserRound size={16} /> : <Bot size={16} />}</span>
            <div className={message.role === "user" ? "rounded-lg bg-[#e7f5f3] p-3" : "min-w-0"}><p className="text-sm">{message.text}</p>
            {message.reason ? <p className="mt-2 text-xs text-[#64748b]">{message.reason}</p> : null}
            {message.products && message.products.length > 0 ? <div className="mt-3 grid gap-3 sm:grid-cols-2">
              {message.products.map((product) => <article key={product.id} className="rounded-lg border border-[#dbe4ee] p-3">
                <p className="text-xs text-[#007f82]">{product.category}</p>
                <h2 className="font-bold">{product.name}</h2>
                <p className="mt-1 text-sm text-[#64748b]">{product.description}</p>
                {product.reason ? <p className="mt-2 text-xs text-[#46566e]">{product.reason}</p> : null}
                <div className="mt-3 flex items-center justify-between gap-2"><strong>{money.format(product.price)}</strong><button disabled={busy} onClick={() => add(product.id)} className="flex items-center gap-1 rounded-md border border-[#007f82] px-2 py-1 text-sm text-[#007f82]"><ShoppingCart size={14} /> Add</button></div>
              </article>)}
            </div> : null}
            {message.bundle ? <div className="mt-3 rounded-lg bg-[#f2f8f7] p-3"><div className="flex items-center justify-between gap-3"><div><p className="text-xs font-semibold uppercase tracking-wide text-[#007f82]">Suggested bundle</p><h2 className="font-bold">{message.bundle.name}</h2></div><strong>{money.format(message.bundle.totalAmount)}</strong></div><button disabled={busy} onClick={() => addBundle(message.bundle as Bundle)} className="mt-3 flex items-center gap-2 rounded-md bg-[#007f82] px-3 py-2 text-sm font-semibold text-white"><ShoppingCart size={15} /> Add bundle</button></div> : null}
            </div></div>)}
        </div>
        {error ? <p role="status" className="px-4 pb-2 text-sm text-[#9a3412]">{error}</p> : null}
        <form onSubmit={ask} className="flex gap-2 border-t border-[#dbe4ee] p-3">
          <input disabled={!token || busy} value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder={token ? "Ask about this ride" : "Sign in to chat"} className="min-w-0 flex-1 rounded-md border border-[#cbd7e3] px-3 py-2" />
          <button disabled={!token || busy} aria-label="Send message" className="grid size-10 place-items-center rounded-md bg-[#007f82] text-white">{busy ? <LoaderCircle className="animate-spin" size={18} /> : <Send size={18} />}</button>
        </form>
        </> : <>
          <div className="flex-1 space-y-4 p-4">
            <div>
              <h2 className="font-bold text-[#172033]">Passenger and driver translation</h2>
              <p className="mt-1 text-sm text-[#64748b]">Write naturally. The assistant detects the passenger language automatically and keeps both sides of the conversation visible.</p>
              <div className="mt-3 flex flex-wrap gap-2">{["Do you have a phone charger?", "Please stop here.", "Could you turn on the AC?"].map((suggestion) => <button type="button" key={suggestion} onClick={() => setTranslationPrompt(suggestion)} className="rounded-full border border-[#cbd7e3] px-3 py-1.5 text-sm text-[#075c64]">{suggestion}</button>)}</div>
            </div>
            {translations.map((exchange, index) => <article key={`${exchange.passengerMessage}-${index}`} className="space-y-3 rounded-lg border border-[#dbe4ee] p-3">
              <div className="flex gap-2"><span className="grid size-8 shrink-0 place-items-center rounded-full bg-[#075c64] text-white"><UserRound size={16} /></span><div><p className="text-xs font-semibold uppercase tracking-wide text-[#64748b]">Passenger | detected {languageLabel(exchange.passengerLanguage)}</p><p className="mt-1 text-sm">{exchange.passengerMessage}</p></div></div>
              <div className="ml-10 rounded-md bg-[#f2f8f7] p-3"><p className="text-xs font-semibold uppercase tracking-wide text-[#007f82]">Driver translation | {languageLabel(exchange.driverLanguage)}</p><p className="mt-1 text-sm">{exchange.driverTranslation}</p></div>
              {!exchange.supported ? <p role="status" className="ml-10 text-xs text-[#9a3412]">This language is not supported yet. The original message is shown so the conversation can continue safely.</p> : null}
              {exchange.fallbackUsed ? <p className="ml-10 text-xs text-[#64748b]">Using local phrase support: {exchange.fallbackReason ?? "the live translation model is unavailable"}.</p> : null}
              {exchange.driverReply ? <><div className="flex gap-2"><span className="grid size-8 shrink-0 place-items-center rounded-full bg-[#e7f5f3] text-[#007f82]"><UserRound size={16} /></span><div><p className="text-xs font-semibold uppercase tracking-wide text-[#64748b]">Driver reply | {languageLabel(exchange.driverLanguage)}</p><p className="mt-1 text-sm">{exchange.driverReply}</p></div></div><div className="ml-10 rounded-md bg-[#f2f8f7] p-3"><p className="text-xs font-semibold uppercase tracking-wide text-[#007f82]">Passenger translation | {languageLabel(exchange.passengerLanguage)}</p><p className="mt-1 text-sm">{exchange.passengerTranslation}</p></div></> : null}
              {index === translations.length - 1 && !exchange.driverReply ? <form onSubmit={translateDriverReply} className="ml-10 flex gap-2"><input disabled={!token || translationBusy} value={driverReply} onChange={(event) => setDriverReply(event.target.value)} placeholder={token ? "Enter the driver's reply" : "Sign in to translate a reply"} className="min-w-0 flex-1 rounded-md border border-[#cbd7e3] px-3 py-2 text-sm" /><button disabled={!token || translationBusy} aria-label="Translate driver reply" className="grid size-10 place-items-center rounded-md border border-[#007f82] text-[#007f82]">{translationBusy ? <LoaderCircle className="animate-spin" size={18} /> : <Send size={18} />}</button></form> : null}
            </article>)}
          </div>
          {error ? <p role="status" className="px-4 pb-2 text-sm text-[#9a3412]">{error}</p> : null}
          <form onSubmit={translatePassenger} className="border-t border-[#dbe4ee] p-3">
            <div className="mb-2 flex items-center justify-between gap-2"><label htmlFor="driver-language" className="text-xs font-semibold text-[#46566e]">Driver language</label><select id="driver-language" disabled={!token || translationBusy} value={driverLanguage} onChange={(event) => setDriverLanguage(event.target.value)} className="rounded-md border border-[#cbd7e3] px-2 py-1 text-sm">{DRIVER_LANGUAGES.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
            <div className="flex gap-2"><input disabled={!token || translationBusy} value={translationPrompt} onChange={(event) => setTranslationPrompt(event.target.value)} placeholder={token ? "Write the passenger's message" : "Sign in to translate"} className="min-w-0 flex-1 rounded-md border border-[#cbd7e3] px-3 py-2" /><button disabled={!token || translationBusy} aria-label="Translate passenger message" className="grid size-10 place-items-center rounded-md bg-[#007f82] text-white">{translationBusy ? <LoaderCircle className="animate-spin" size={18} /> : <Send size={18} />}</button></div>
          </form>
        </>}
      </section>
    </div>
  </main>;
}

function timeOfDay(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "morning";
  if (hour < 17) return "afternoon";
  if (hour < 21) return "evening";
  return "night";
}

function languageLabel(value: string): string {
  return DRIVER_LANGUAGES.find(([key]) => key === value)?.[1] ?? "Unsupported";
}
