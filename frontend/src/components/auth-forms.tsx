"use client";

import Link from "next/link";
import { ArrowRight, LoaderCircle, LockKeyhole, UserRound } from "lucide-react";
import { type FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { clearAccessToken, saveAccessToken } from "@/lib/auth-session";

type AuthLayoutProps = { eyebrow: string; title: string; description: string; children: React.ReactNode };

function AuthLayout({ eyebrow, title, description, children }: AuthLayoutProps) {
  return <main className="min-h-screen bg-[#f5f8fb] text-[#172033]"><div className="mx-auto grid min-h-screen max-w-6xl lg:grid-cols-[0.8fr_1.2fr]">
    <aside className="hidden flex-col justify-between bg-[#075c64] p-10 text-white lg:flex"><div><Link href="/" className="font-bold">InRideMart</Link><p className="mt-20 flex items-center gap-2 text-sm font-semibold text-[#9de4dc]"><UserRound size={16} /> Personal ride shopping</p><h2 className="mt-4 text-4xl font-bold leading-tight">Your ride has a little more waiting for you.</h2><p className="mt-5 max-w-sm leading-7 text-[#d6f3ef]">Sign in once and keep your cab cart, AI conversation, payments, and order tracking together.</p></div><p className="text-sm text-[#b7e6e0]">InRideMart · assigned cab inventory</p></aside>
    <section className="flex items-center px-4 py-8 sm:px-8 lg:px-16"><div className="w-full max-w-md"><Link href="/" className="font-bold text-[#075c64] lg:hidden">InRideMart</Link><p className="mt-10 text-sm font-semibold text-[#007f82] lg:mt-0">{eyebrow}</p><h1 className="mt-3 text-3xl font-bold sm:text-4xl">{title}</h1><p className="mt-3 leading-6 text-[#64748b]">{description}</p>{children}</div></section>
  </div></main>;
}

async function responseData(response: Response): Promise<Record<string, unknown>> {
  try { return await response.json() as Record<string, unknown>; } catch { return {}; }
}

function errorMessage(data: Record<string, unknown>, fallback: string): string {
  if (typeof data.message === "string") return data.message;
  if (typeof data.error === "string") return data.error;
  return fallback;
}

export function LoginForm({ nextPath = "/home" }: { nextPath?: string }) {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      const response = await fetch("/api/proxy/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) });
      const data = await responseData(response);
      if (!response.ok || typeof data.accessToken !== "string") throw new Error(errorMessage(data, "Could not sign in. Check your details and try again."));
      saveAccessToken(data.accessToken);
      router.replace(nextPath || "/home");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not sign in. Check your details and try again.");
    } finally { setBusy(false); }
  }

  return <AuthLayout eyebrow="Welcome back" title="Sign in to your ride shop" description="Your saved cart, AI assistant, and orders are ready when you are."><form onSubmit={submit} className="mt-8 grid gap-4" noValidate>
    <label className="grid gap-1.5 text-sm font-semibold" htmlFor="login-email">Email<input id="login-email" required type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label>
    <label className="grid gap-1.5 text-sm font-semibold" htmlFor="login-password">Password<input id="login-password" required minLength={10} type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label>
    {error ? <p role="alert" className="rounded-md border border-[#f2b8a8] bg-[#fff4f0] px-3 py-2 text-sm text-[#9a3412]">{error}</p> : null}
    <button disabled={busy} className="mt-2 flex items-center justify-center gap-2 rounded-md bg-[#007f82] px-4 py-3 font-semibold text-white disabled:cursor-wait disabled:opacity-60">{busy ? <LoaderCircle className="animate-spin" size={17} /> : <LockKeyhole size={17} />} {busy ? "Signing in..." : "Sign in"}</button>
    <p className="text-center text-sm text-[#64748b]">New to InRideMart? <Link href="/register" className="font-semibold text-[#007f82]">Create an account <ArrowRight className="inline" size={14} /></Link></p>
  </form></AuthLayout>;
}

export function RegisterForm() {
  const router = useRouter();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    if (fullName.trim().length < 2) { setError("Enter your full name."); return; }
    if (!/^[+0-9 ()-]{7,20}$/.test(phoneNumber.trim())) { setError("Enter a valid phone number."); return; }
    if (password.length < 10) { setError("Password must be at least 10 characters."); return; }
    if (password !== confirmPassword) { setError("Passwords do not match."); return; }
    setBusy(true);
    try {
      const registerResponse = await fetch("/api/proxy/auth/register", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password, role: "CUSTOMER" }) });
      const registered = await responseData(registerResponse);
      if (!registerResponse.ok) throw new Error(errorMessage(registered, "Could not create your account."));
      let token = typeof registered.accessToken === "string" ? registered.accessToken : "";
      if (!token) {
        const loginResponse = await fetch("/api/proxy/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) });
        const loggedIn = await responseData(loginResponse);
        if (!loginResponse.ok || typeof loggedIn.accessToken !== "string") throw new Error(errorMessage(loggedIn, "Account created, but automatic sign in failed."));
        token = loggedIn.accessToken;
      }
      saveAccessToken(token);
      const profileResponse = await fetch("/api/proxy/customer", { method: "POST", headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` }, body: JSON.stringify({ fullName: fullName.trim(), phoneNumber: phoneNumber.trim() }) });
      const profileData = await responseData(profileResponse);
      if (!profileResponse.ok) throw new Error(errorMessage(profileData, "Account created, but your customer profile could not be saved."));
      router.replace("/home");
    } catch (cause) {
      clearAccessToken();
      setError(cause instanceof Error ? cause.message : "Could not create your account.");
    } finally { setBusy(false); }
  }

  return <AuthLayout eyebrow="New passenger" title="Create your InRideMart account" description="Save your ride profile once, then shop the inventory assigned to your cab."><form onSubmit={submit} className="mt-8 grid gap-3.5" noValidate>
    <label className="grid gap-1.5 text-sm font-semibold" htmlFor="register-name">Name<input id="register-name" required minLength={2} autoComplete="name" value={fullName} onChange={(event) => setFullName(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label>
    <label className="grid gap-1.5 text-sm font-semibold" htmlFor="register-email">Email<input id="register-email" required type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label>
    <label className="grid gap-1.5 text-sm font-semibold" htmlFor="register-phone">Phone number<input id="register-phone" required type="tel" autoComplete="tel" value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label>
    <div className="grid gap-3 sm:grid-cols-2"><label className="grid gap-1.5 text-sm font-semibold" htmlFor="register-password">Password<input id="register-password" required minLength={10} type="password" autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label><label className="grid gap-1.5 text-sm font-semibold" htmlFor="register-confirm-password">Confirm password<input id="register-confirm-password" required minLength={10} type="password" autoComplete="new-password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-3 font-normal outline-none" /></label></div>
    {error ? <p role="alert" className="rounded-md border border-[#f2b8a8] bg-[#fff4f0] px-3 py-2 text-sm text-[#9a3412]">{error}</p> : null}
    <button disabled={busy} className="mt-2 flex items-center justify-center gap-2 rounded-md bg-[#007f82] px-4 py-3 font-semibold text-white disabled:cursor-wait disabled:opacity-60">{busy ? <LoaderCircle className="animate-spin" size={17} /> : <UserRound size={17} />} {busy ? "Creating account..." : "Create account"}</button>
    <p className="text-center text-sm text-[#64748b]">Already registered? <Link href="/login" className="font-semibold text-[#007f82]">Sign in <ArrowRight className="inline" size={14} /></Link></p>
  </form></AuthLayout>;
}
