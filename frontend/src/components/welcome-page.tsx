import Image from "next/image";
import Link from "next/link";
import { ArrowRight, MapPin, PackageCheck, ShieldCheck, ShoppingBag, Sparkles } from "lucide-react";

export function WelcomePage() {
  return (
    <main className="min-h-screen bg-[#f5f8fb] text-[#172033]">
      <header className="border-b border-[#dbe4ee] bg-white/95">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
          <Link href="/" className="flex items-center gap-2 font-bold" aria-label="InRideMart home">
            <span className="grid size-9 place-items-center rounded-lg bg-[#007f82] text-white"><ShoppingBag size={18} /></span>
            InRideMart
          </Link>
          <nav className="flex items-center gap-3 text-sm" aria-label="Account navigation">
            <Link href="/catalog" className="hidden text-[#46566e] sm:inline">Browse catalog</Link>
            <Link href="/login" className="font-semibold text-[#007f82]">Sign in</Link>
            <Link href="/register" className="rounded-md bg-[#007f82] px-3 py-2 font-semibold text-white">Create account</Link>
          </nav>
        </div>
      </header>

      <section className="mx-auto grid max-w-7xl gap-10 px-4 py-12 sm:px-6 lg:grid-cols-[1.05fr_0.95fr] lg:items-center lg:py-20">
        <div>
          <p className="flex items-center gap-2 text-sm font-semibold text-[#007f82]"><Sparkles size={16} /> Your in-ride shopping concierge</p>
          <h1 className="mt-4 max-w-2xl text-4xl font-bold leading-tight sm:text-6xl">Useful things, ready before you arrive.</h1>
          <p className="mt-5 max-w-xl text-lg leading-8 text-[#46566e]">Discover cab-stocked snacks, charging essentials, and comfort picks, then let the AI assistant tailor the ride to you.</p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link href="/register" className="inline-flex items-center gap-2 rounded-md bg-[#007f82] px-5 py-3 font-semibold text-white">Create account <ArrowRight size={17} /></Link>
            <Link href="/catalog" className="rounded-md border border-[#b9c9d8] bg-white px-5 py-3 font-semibold text-[#172033]">Browse products</Link>
          </div>
          <div className="mt-9 grid max-w-xl gap-4 text-sm text-[#46566e] sm:grid-cols-3">
            <p className="flex items-start gap-2"><MapPin size={17} className="mt-0.5 shrink-0 text-[#007f82]" />Matched to your current ride</p>
            <p className="flex items-start gap-2"><PackageCheck size={17} className="mt-0.5 shrink-0 text-[#007f82]" />Only available cab inventory</p>
            <p className="flex items-start gap-2"><ShieldCheck size={17} className="mt-0.5 shrink-0 text-[#007f82]" />Secure checkout and tracking</p>
          </div>
        </div>
        <div className="relative overflow-hidden rounded-lg border border-[#dbe4ee] bg-[#e7f5f3] p-5 sm:p-8">
          <div className="flex items-center justify-between gap-3 text-sm font-semibold text-[#075c64]"><span>In your assigned cab</span><span className="rounded-full bg-white px-3 py-1 text-xs">IRM-CAB-042</span></div>
          <Image src="/images/catalog/tech.svg" alt="Charging essentials available in the cab" width={640} height={480} className="mt-5 w-full rounded-md" priority />
          <div className="mt-5 grid gap-3 sm:grid-cols-2">
            <div className="rounded-md bg-white p-4"><p className="text-xs font-semibold uppercase tracking-wide text-[#007f82]">Ask naturally</p><p className="mt-2 font-semibold">“I forgot my charger.”</p></div>
            <div className="rounded-md bg-white p-4"><p className="text-xs font-semibold uppercase tracking-wide text-[#007f82]">Get a useful answer</p><p className="mt-2 font-semibold">Relevant picks within your budget.</p></div>
          </div>
        </div>
      </section>

      <section className="border-y border-[#dbe4ee] bg-white">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 md:grid-cols-3">
          <div><p className="text-sm font-semibold text-[#007f82]">01</p><h2 className="mt-2 text-xl font-bold">Browse what is onboard</h2><p className="mt-2 text-sm leading-6 text-[#64748b]">See products stocked for this ride before you decide.</p></div>
          <div><p className="text-sm font-semibold text-[#007f82]">02</p><h2 className="mt-2 text-xl font-bold">Ask the AI assistant</h2><p className="mt-2 text-sm leading-6 text-[#64748b]">Share your destination, appetite, weather, or budget.</p></div>
          <div><p className="text-sm font-semibold text-[#007f82]">03</p><h2 className="mt-2 text-xl font-bold">Order and track</h2><p className="mt-2 text-sm leading-6 text-[#64748b]">Pay with the mock checkout and follow handover to completion.</p></div>
        </div>
      </section>
    </main>
  );
}
