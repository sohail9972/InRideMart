"use client";

import Image from "next/image";
import Link from "next/link";
import {
  BadgeCheck,
  CircleCheck,
  CloudRain,
  CreditCard,
  History,
  Home,
  Landmark,
  LoaderCircle,
  MapPin,
  Menu,
  Navigation,
  Package,
  Search,
  ShoppingBag,
  ShoppingCart,
  Smartphone,
  Sparkles,
  UserRound,
  WalletCards,
  X
} from "lucide-react";
import { type FormEvent, type ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { Category, Product, ProductPage } from "@/lib/catalog";
import { clearAccessToken, readAccessToken, saveAccessToken } from "@/lib/auth-session";
import { clearActiveAiSession } from "@/lib/ai-session";
import {
  ASSIGNED_CAB,
  type RideContext,
  DEFAULT_RIDE_CONTEXT,
  isAvailableInAssignedCab,
  readRideContext,
  saveRideContext
} from "@/lib/ride-context";

type Props = { categories: Category[]; initialProducts: ProductPage | null; initialScreen?: Screen };
export type Screen = "home" | "products" | "cart" | "orders" | "profile";
type Cart = { id?: string; items: { productId: string; quantity: number }[] };
type PaymentMethod = "UPI" | "CREDIT_CARD" | "DEBIT_CARD" | "WALLET";
type PaymentStage = "idle" | "checkout" | "processing" | "paid" | "driver-notified" | "confirmed";
type Order = {
  id: string;
  status: "PLACED" | "CONFIRMED" | "HANDED_OVER" | "COMPLETED";
  totalAmount: number;
  currency: string;
  paymentStatus?: string;
  paymentMethod?: PaymentMethod;
  driverNotified?: boolean;
  items: { productName: string; quantity: number; unitPrice: number }[];
};
type Bundle = { name: string; note: string; products: Product[] };

const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 });
const paymentMethods: { value: PaymentMethod; label: string; icon: typeof CreditCard }[] = [
  { value: "UPI", label: "UPI", icon: Smartphone },
  { value: "CREDIT_CARD", label: "Credit Card", icon: CreditCard },
  { value: "DEBIT_CARD", label: "Debit Card", icon: Landmark },
  { value: "WALLET", label: "Wallet", icon: WalletCards }
];
const screens: { key: Screen; label: string; icon: typeof Home }[] = [
  { key: "home", label: "Home", icon: Home },
  { key: "products", label: "Cab shop", icon: Package },
  { key: "cart", label: "Cart", icon: ShoppingCart },
  { key: "orders", label: "Orders", icon: History },
  { key: "profile", label: "Profile", icon: UserRound }
];

export function Storefront({ categories, initialProducts, initialScreen = "home" }: Props) {
  const router = useRouter();
  const [screen, setScreen] = useState<Screen>(initialScreen);
  const [menuOpen, setMenuOpen] = useState(false);
  const [token, setToken] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [notice, setNotice] = useState("");
  const [ride, setRide] = useState<RideContext>(DEFAULT_RIDE_CONTEXT);
  const [catalogProducts, setCatalogProducts] = useState<Product[]>(initialProducts?.items ?? []);
  const [products, setProducts] = useState<Product[]>(initialProducts?.items.filter((product) => isAvailableInAssignedCab(product.id)) ?? []);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [sort, setSort] = useState("RELEVANCE");
  const [loading, setLoading] = useState(false);
  const [processingPayment, setProcessingPayment] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("UPI");
  const [paymentStage, setPaymentStage] = useState<PaymentStage>("idle");
  const [cart, setCart] = useState<Cart>({ items: [] });
  const [orders, setOrders] = useState<Order[]>([]);

  const productById = useMemo(() => new Map(catalogProducts.map((product) => [product.id, product])), [catalogProducts]);
  const cabProducts = useMemo(() => catalogProducts.filter((product) => isAvailableInAssignedCab(product.id)), [catalogProducts]);
  const cartTotal = cart.items.reduce((total, item) => total + (productById.get(item.productId)?.price ?? 0) * item.quantity, 0);
  const hasUnavailableCartItem = cart.items.some((item) => !isAvailableInAssignedCab(item.productId));
  const recommendedProducts = cabProducts.filter((product) => /charger|cable|pillow|umbrella/i.test(`${product.name} ${product.description}`)).slice(0, 3);
  const bundles = useMemo(() => createBundles(cabProducts), [cabProducts]);

  const loadCart = useCallback(async () => {
    if (!token) return;
    const response = await fetch("/api/proxy/cart/me", { headers: { Authorization: `Bearer ${token}` } });
    if (response.ok) setCart(await response.json());
  }, [token]);

  const loadOrders = useCallback(async () => {
    if (!token) return;
    const response = await fetch("/api/proxy/orders", { headers: { Authorization: `Bearer ${token}` } });
    if (!response.ok) return;
    const orderList: Order[] = await response.json();
    const ordersWithPayments = await Promise.all(orderList.map(async (order) => {
      const payment = await fetch(`/api/proxy/payments/orders/${order.id}`, { headers: { Authorization: `Bearer ${token}` } });
      if (!payment.ok) return order;
      const paymentDetails = await payment.json() as { status: string; method?: PaymentMethod; driverNotified?: boolean };
      return { ...order, paymentStatus: paymentDetails.status, paymentMethod: paymentDetails.method, driverNotified: paymentDetails.driverNotified };
    }));
    setOrders(ordersWithPayments);
  }, [token]);

  useEffect(() => {
    queueMicrotask(() => {
      setToken(readAccessToken());
      setRide(readRideContext());
    });
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(async () => {
      setLoading(true);
      try {
        const parameters = new URLSearchParams({ size: "50" });
        if (query) parameters.set("query", query);
        if (category) parameters.set("category", category);
        if (sort !== "RELEVANCE") parameters.set("sort", sort);
        const response = await fetch(`/api/catalog/products?${parameters}`);
        if (!response.ok) throw new Error();
        const page = await response.json() as ProductPage;
        setProducts(page.items.filter((product) => isAvailableInAssignedCab(product.id)));
        setCatalogProducts((current) => mergeProducts(current, page.items));
      } catch {
        setNotice("Catalog is unavailable. Please try again.");
      } finally {
        setLoading(false);
      }
    }, 250);
    return () => window.clearTimeout(timer);
  }, [query, category, sort]);

  useEffect(() => {
    if (!token) return;
    queueMicrotask(() => {
      void loadCart();
      void loadOrders();
    });
  }, [token, loadCart, loadOrders]);

  function openScreen(next: Screen) {
    if (next === "home") {
      router.push("/home");
      return;
    }
    if (next === "products") {
      setScreen("products");
      return;
    }
    if (!token) {
      router.push(`/login?next=${encodeURIComponent("/home")}`);
      return;
    }
    router.push(`/${next}`);
  }

  function updateRide(change: Partial<RideContext>) {
    setRide((current) => {
      const next = { ...current, ...change };
      saveRideContext(next);
      return next;
    });
  }

  async function login(event: FormEvent) {
    event.preventDefault();
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
      setNotice("Welcome back. Your cab shop is ready.");
    } catch {
      setNotice("Could not sign in. Check your email and password.");
    }
  }

  async function add(productId: string) {
    if (!isAvailableInAssignedCab(productId)) {
      setNotice("That product is not stocked in your assigned cab.");
      return;
    }
    if (!token) {
      router.push(`/login?next=${encodeURIComponent("/home")}`);
      return;
    }
    const response = await fetch("/api/proxy/cart/me/items", {
      method: "POST",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify({ productId, quantity: 1 })
    });
    if (response.ok) {
      await loadCart();
      setNotice("Added to your cab cart.");
    } else {
      setNotice("Could not add this product.");
    }
  }

  async function addBundle(bundle: Bundle) {
    for (const product of bundle.products) await add(product.id);
  }

  async function checkout() {
    if (!token || cart.items.length === 0 || hasUnavailableCartItem) return;
    setProcessingPayment(true);
    setPaymentStage("checkout");
    setNotice("Securing your order...");
    try {
      const checkoutResponse = await fetch("/api/proxy/cart/me/checkout", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}`, "Idempotency-Key": crypto.randomUUID() }
      });
      if (checkoutResponse.status === 401 || checkoutResponse.status === 403) {
        clearAccessToken();
        clearActiveAiSession();
        setToken("");
        router.replace(`/login?next=${encodeURIComponent("/cart")}`);
        throw new Error("Your session expired. Sign in again to complete checkout.");
      }
      const order = await checkoutResponse.json();
      if (!checkoutResponse.ok) throw new Error(order.message ?? "Checkout was rejected");
      setPaymentStage("processing");
      await wait(450);
      const paymentResponse = await fetch("/api/proxy/payments", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ orderId: order.id, method: paymentMethod })
      });
      if (paymentResponse.status === 401 || paymentResponse.status === 403) {
        clearAccessToken();
        clearActiveAiSession();
        setToken("");
        router.replace(`/login?next=${encodeURIComponent("/cart")}`);
        throw new Error("Your session expired. Sign in again to complete payment.");
      }
      const payment = await paymentResponse.json();
      if (!paymentResponse.ok) throw new Error(payment.message ?? "Payment was rejected");
      setPaymentStage("paid");
      await wait(400);
      setPaymentStage("driver-notified");
      await wait(400);
      setPaymentStage("confirmed");
      await Promise.all([loadCart(), loadOrders()]);
      await wait(350);
      openScreen("orders");
      setNotice(`Payment successful via ${payment.method ?? paymentMethod}. Driver notified and order confirmed.`);
    } catch (cause) {
      setNotice(cause instanceof Error ? cause.message : "Checkout could not be completed. Please try again.");
    } finally {
      setProcessingPayment(false);
      setPaymentStage("idle");
    }
  }

  async function advanceOrder(order: Order) {
    const nextStatus = order.status === "CONFIRMED" ? "HANDED_OVER" : order.status === "HANDED_OVER" ? "COMPLETED" : null;
    if (!token || !nextStatus) return;
    const response = await fetch(`/api/proxy/orders/${order.id}/tracking`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify({ status: nextStatus })
    });
    if (response.ok) {
      await loadOrders();
      setNotice(nextStatus === "COMPLETED" ? "Order completed." : "Order is ready for handover.");
    } else {
      setNotice("Could not update tracking.");
    }
  }

  function logout() {
    clearAccessToken();
    clearActiveAiSession();
    setToken("");
    setCart({ items: [] });
    setOrders([]);
    router.replace("/");
  }

  return <main className="min-h-screen bg-[#f6f8fb] text-[#172033]">
    <header className="sticky top-0 z-30 border-b border-[#dbe4ee] bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <button onClick={() => openScreen("home")} className="flex items-center gap-2 font-bold" aria-label="Go to home">
          <span className="grid size-9 place-items-center rounded-lg bg-[#007f82] text-white"><ShoppingBag size={18} /></span>
          InRideMart
        </button>
        <nav className="hidden items-center gap-5 text-sm md:flex" aria-label="Primary navigation">
          {screens.map(({ key, label }) => <button key={key} onClick={() => openScreen(key)} className={screen === key ? "font-semibold text-[#007f82]" : "text-[#46566e]"}>{label}</button>)}
          <Link href="/ai" className="flex items-center gap-1 font-semibold text-[#007f82]"><Sparkles size={15} /> AI assistant</Link>
          <button onClick={() => openScreen("cart")} className="relative" aria-label="Open cart"><ShoppingCart size={19} />{cart.items.length > 0 ? <span className="absolute -right-2 -top-2 grid size-4 place-items-center rounded-full bg-[#ee6c4d] text-[10px] text-white">{cart.items.length}</span> : null}</button>
        </nav>
        <button className="md:hidden" aria-label="Open navigation" onClick={() => setMenuOpen((value) => !value)}>{menuOpen ? <X /> : <Menu />}</button>
      </div>
      {menuOpen ? <nav className="border-t px-4 py-3 md:hidden" aria-label="Mobile navigation">{screens.map(({ key, label }) => <button key={key} onClick={() => { openScreen(key); setMenuOpen(false); }} className="block w-full py-2 text-left">{label}</button>)}<Link className="block py-2 font-semibold text-[#007f82]" href="/ai">AI assistant</Link></nav> : null}
    </header>
    {notice ? <div role="status" className="fixed bottom-4 right-4 z-40 max-w-sm rounded-lg bg-[#172033] px-4 py-3 text-sm text-white shadow-xl">{notice}<button onClick={() => setNotice("")} className="ml-3 text-[#b6c4d5]">Dismiss</button></div> : null}
    <div className="mx-auto max-w-7xl px-4 py-6">
      {screen === "home" ? <HomeScreen ride={ride} updateRide={updateRide} cabProducts={cabProducts} recommendedProducts={recommendedProducts} bundles={bundles} onAdd={add} onAddBundle={addBundle} onOpenShop={() => openScreen("products")} /> : null}
      {screen === "products" ? <ProductScreen categories={categories} products={products} query={query} category={category} sort={sort} loading={loading} onQuery={setQuery} onCategory={setCategory} onSort={setSort} onAdd={add} /> : null}
      {screen === "cart" ? <CartScreen token={token} cart={cart} products={productById} total={cartTotal} unavailable={hasUnavailableCartItem} processing={processingPayment} paymentMethod={paymentMethod} paymentStage={paymentStage} onPaymentMethod={setPaymentMethod} onCheckout={checkout} onSignIn={() => openScreen("cart")} /> : null}
      {screen === "orders" ? <OrdersScreen token={token} orders={orders} onAdvance={advanceOrder} onSignIn={() => openScreen("orders")} /> : null}
      {screen === "profile" ? <ProfileScreen token={token} email={email} password={password} onEmail={setEmail} onPassword={setPassword} onLogin={login} onLogout={logout} /> : null}
    </div>
  </main>;
}

function HomeScreen({ ride, updateRide, cabProducts, recommendedProducts, bundles, onAdd, onAddBundle, onOpenShop }: { ride: RideContext; updateRide: (change: Partial<RideContext>) => void; cabProducts: Product[]; recommendedProducts: Product[]; bundles: Bundle[]; onAdd: (id: string) => void; onAddBundle: (bundle: Bundle) => void; onOpenShop: () => void }) {
  return <>
    <section className="overflow-hidden rounded-lg bg-[#075c64] px-6 py-10 text-white sm:px-10">
      <p className="flex items-center gap-2 text-sm font-semibold text-[#9de4dc]"><Navigation size={16} /> In your assigned ride</p>
      <h1 className="mt-3 max-w-2xl text-4xl font-bold sm:text-5xl">A smarter shop for the road ahead.</h1>
      <p className="mt-4 max-w-xl text-[#d6f3ef]">Your cab has useful essentials ready now. Let the AI concierge tailor them to this ride.</p>
      <div className="mt-7 flex flex-wrap gap-3"><Link href="/ai" className="rounded-md bg-white px-4 py-2 font-semibold text-[#075c64]">Talk to AI concierge</Link><button onClick={onOpenShop} className="rounded-md border border-[#9de4dc] px-4 py-2 font-semibold">Browse cab inventory</button></div>
    </section>
    <RideContextPanel ride={ride} updateRide={updateRide} />
    <section className="mt-9">
      <SectionTitle eyebrow="Assigned cab inventory" title="Available in your cab" action="View all" onAction={onOpenShop} />
      <p className="-mt-3 mb-1 text-sm text-[#64748b]">{ASSIGNED_CAB.id} has {cabProducts.length} items ready for this ride.</p>
      <ProductGrid products={cabProducts.slice(0, 4)} onAdd={onAdd} />
    </section>
    <section className="mt-10">
      <SectionTitle eyebrow="For this journey" title="Recommended for you" />
      <ProductGrid products={recommendedProducts} onAdd={onAdd} />
    </section>
    <section className="mt-10 grid gap-5 lg:grid-cols-[1.25fr_1fr]">
      <div><SectionTitle eyebrow="Pack smarter" title="Popular bundles" /><div className="mt-5 grid gap-3 sm:grid-cols-2">{bundles.map((bundle) => <BundleCard key={bundle.name} bundle={bundle} onAdd={onAddBundle} />)}</div></div>
      <aside className="rounded-lg border border-[#dbe4ee] bg-white p-5"><Sparkles className="text-[#007f82]" size={22} /><h2 className="mt-3 text-xl font-bold">Your AI concierge remembers this ride.</h2><p className="mt-2 text-sm leading-6 text-[#64748b]">Share a budget, weather concern, or travel need. It will ask when context is missing, then recommend only inventory in this cab.</p><Link href="/ai" className="mt-5 inline-flex rounded-md bg-[#007f82] px-4 py-2 text-sm font-semibold text-white">Start a conversation</Link></aside>
    </section>
  </>;
}

function RideContextPanel({ ride, updateRide }: { ride: RideContext; updateRide: (change: Partial<RideContext>) => void }) {
  return <section aria-labelledby="ride-context" className="mt-6 border-y border-[#dbe4ee] py-5">
    <div className="flex flex-wrap items-center justify-between gap-3"><div><p className="text-sm font-semibold text-[#007f82]">Ride context</p><h2 id="ride-context" className="text-2xl font-bold">Personalize this journey</h2></div><span className="flex items-center gap-2 text-sm text-[#64748b]"><BadgeCheck size={16} className="text-[#007f82]" /> {ASSIGNED_CAB.id}</span></div>
    <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <label className="grid gap-1 text-sm font-medium"><span className="flex items-center gap-1 text-[#64748b]"><MapPin size={15} /> Destination</span><input value={ride.destination} onChange={(event) => updateRide({ destination: event.target.value })} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-2" /></label>
      <label className="grid gap-1 text-sm font-medium"><span className="text-[#64748b]">Journey duration</span><select value={ride.journeyDurationMinutes} onChange={(event) => updateRide({ journeyDurationMinutes: Number(event.target.value) })} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-2"><option value={15}>15 min</option><option value={35}>35 min</option><option value={60}>1 hour</option><option value={120}>2 hours</option></select></label>
      <label className="grid gap-1 text-sm font-medium"><span className="flex items-center gap-1 text-[#64748b]"><CloudRain size={15} /> Weather</span><select value={ride.weather} onChange={(event) => updateRide({ weather: event.target.value })} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-2"><option>Clear</option><option>Rainy</option><option>Hot</option><option>Cool</option></select></label>
      <label className="grid gap-1 text-sm font-medium"><span className="text-[#64748b]">Travel purpose</span><select value={ride.travelPurpose} onChange={(event) => updateRide({ travelPurpose: event.target.value })} className="rounded-md border border-[#cbd7e3] bg-white px-3 py-2"><option>Leisure</option><option>Business</option><option>Airport transfer</option><option>Family trip</option><option>Office commute</option></select></label>
    </div>
  </section>;
}

function ProductScreen({ categories, products, query, category, sort, loading, onQuery, onCategory, onSort, onAdd }: { categories: Category[]; products: Product[]; query: string; category: string; sort: string; loading: boolean; onQuery: (value: string) => void; onCategory: (value: string) => void; onSort: (value: string) => void; onAdd: (id: string) => void }) {
  return <section><SectionTitle eyebrow="Cab shop" title="Only what is ready in your cab" /><div className="mt-5 grid gap-3 rounded-lg border border-[#dbe4ee] bg-white p-3 md:grid-cols-4"><label className="flex items-center gap-2 rounded-md border border-[#cbd7e3] px-3 py-2"><Search size={17} /><input aria-label="Search cab inventory" value={query} onChange={(event) => onQuery(event.target.value)} placeholder="Search inventory" className="min-w-0 flex-1 outline-none" /></label><select aria-label="Category" value={category} onChange={(event) => onCategory(event.target.value)} className="rounded-md border border-[#cbd7e3] px-3 py-2"><option value="">All categories</option>{categories.map((item) => <option key={item.id} value={item.slug}>{item.name}</option>)}</select><select aria-label="Sort products" value={sort} onChange={(event) => onSort(event.target.value)} className="rounded-md border border-[#cbd7e3] px-3 py-2"><option value="RELEVANCE">Recommended</option><option value="PRICE_ASC">Price: low to high</option><option value="PRICE_DESC">Price: high to low</option><option value="NEWEST">Newest</option></select><Link href="/ai" className="rounded-md bg-[#007f82] px-3 py-2 text-center font-semibold text-white">Ask AI instead</Link></div>{loading ? <LoadingGrid /> : products.length > 0 ? <ProductGrid products={products} onAdd={onAdd} /> : <EmptyState title="No cab inventory matches that search" text="Try a different search or ask the AI concierge for help." />}</section>;
}

function CartScreen({ token, cart, products, total, unavailable, processing, paymentMethod, paymentStage, onPaymentMethod, onCheckout, onSignIn }: { token: string; cart: Cart; products: Map<string, Product>; total: number; unavailable: boolean; processing: boolean; paymentMethod: PaymentMethod; paymentStage: PaymentStage; onPaymentMethod: (method: PaymentMethod) => void; onCheckout: () => void; onSignIn: () => void }) {
  if (!token) return <EmptyState title="Sign in to see your cab cart" text="Your cart is tied to your InRideMart account." action="Sign in" onAction={onSignIn} />;
  if (cart.items.length === 0) return <EmptyState title="Your cab cart is empty" text="Add available items from the shop or ask the AI concierge for a bundle." />;
  return <section><SectionTitle eyebrow="Cab cart" title="Ready when you are" /><div className="mt-5 grid gap-5 lg:grid-cols-[1fr_340px]"><div className="space-y-3">{cart.items.map((item) => { const product = products.get(item.productId); return <article key={item.productId} className="flex items-start justify-between rounded-lg border border-[#dbe4ee] bg-white p-4"><div><p className="font-semibold">{product?.name ?? item.productId}</p><p className="mt-1 text-sm text-[#64748b]">Quantity {item.quantity} {product && !isAvailableInAssignedCab(product.id) ? "| Not available in this cab" : "| In your cab"}</p></div><strong>{money.format((product?.price ?? 0) * item.quantity)}</strong></article>; })}</div><aside className="h-fit rounded-lg border border-[#dbe4ee] bg-white p-5"><h2 className="font-bold">Order summary</h2><div className="mt-4 flex justify-between text-sm"><span>Cab inventory subtotal</span><strong>{money.format(total)}</strong></div><div className="mt-2 flex justify-between border-t border-[#dbe4ee] pt-3"><span className="font-semibold">Total</span><strong>{money.format(total)}</strong></div><fieldset className="mt-5"><legend className="text-sm font-semibold">Mock payment method</legend><div className="mt-3 grid grid-cols-2 gap-2">{paymentMethods.map(({ value, label, icon: Icon }) => <label key={value} className={paymentMethod === value ? "flex cursor-pointer items-center gap-2 rounded-md border border-[#007f82] bg-[#f2f8f7] px-3 py-2 text-sm font-semibold text-[#075c64]" : "flex cursor-pointer items-center gap-2 rounded-md border border-[#cbd7e3] px-3 py-2 text-sm text-[#46566e]"}><input className="sr-only" type="radio" name="payment-method" value={value} checked={paymentMethod === value} disabled={processing} onChange={() => onPaymentMethod(value)} /><Icon size={16} />{label}</label>)}</div></fieldset>{unavailable ? <p className="mt-4 text-sm text-[#9a3412]">Remove unavailable items before checkout.</p> : null}{paymentStage !== "idle" ? <PaymentProgress stage={paymentStage} method={paymentMethod} /> : null}<button disabled={processing || unavailable} onClick={onCheckout} className="mt-5 flex w-full items-center justify-center gap-2 rounded-md bg-[#007f82] py-3 font-semibold text-white disabled:cursor-not-allowed disabled:bg-[#9aabb9]"><CreditCard size={17} />{processing ? "Processing payment..." : "Checkout and pay"}</button><p className="mt-3 text-xs leading-5 text-[#64748b]">This is a secure mock flow. No card or UPI details are collected.</p></aside></div></section>;
}

function OrdersScreen({ token, orders, onAdvance, onSignIn }: { token: string; orders: Order[]; onAdvance: (order: Order) => void; onSignIn: () => void }) {
  if (!token) return <EmptyState title="Sign in to see your orders" text="Track handover and completion from your account." action="Sign in" onAction={onSignIn} />;
  if (orders.length === 0) return <EmptyState title="No orders yet" text="Your paid cab orders will appear here with their live tracking status." />;
  return <section><SectionTitle eyebrow="Order tracking" title="From cab shelf to handover" /><div className="mt-5 space-y-4">{orders.map((order) => <article key={order.id} className="rounded-lg border border-[#dbe4ee] bg-white p-5"><div className="flex flex-wrap items-start justify-between gap-3"><div><h2 className="font-semibold">Order {order.id.slice(0, 8)}</h2><p className="mt-1 text-sm text-[#64748b]">{order.items.map((item) => `${item.quantity} x ${item.productName} at ${money.format(item.unitPrice)}`).join(", ")}</p><p className="mt-2 text-xs text-[#64748b]">Payment: {order.paymentStatus ?? "PENDING"}{order.paymentMethod ? ` via ${paymentMethodLabel(order.paymentMethod)}` : ""} | {order.driverNotified ? "Driver notified | " : ""}Currency: {order.currency}</p></div><strong>{money.format(order.totalAmount)}</strong></div><OrderTimeline status={order.status} /><div className="mt-4 flex items-center justify-between gap-3"><span className="text-sm font-medium text-[#007f82]">Current: {order.status}</span>{order.status === "CONFIRMED" || order.status === "HANDED_OVER" ? <button onClick={() => onAdvance(order)} className="rounded-md border border-[#007f82] px-3 py-2 text-sm font-semibold text-[#007f82]">{order.status === "CONFIRMED" ? "Mark handed over" : "Complete order"}</button> : null}</div></article>)}</div></section>;
}

function ProfileScreen({ token, email, password, onEmail, onPassword, onLogin, onLogout }: { token: string; email: string; password: string; onEmail: (value: string) => void; onPassword: (value: string) => void; onLogin: (event: FormEvent) => void; onLogout: () => void }) {
  return <section className="mx-auto max-w-md"><SectionTitle eyebrow="Your account" title="Profile" />{token ? <div className="mt-5 rounded-lg border border-[#dbe4ee] bg-white p-5"><p className="font-medium text-[#007f82]">Signed in and ready for this ride.</p><button onClick={onLogout} className="mt-4 rounded-md border border-[#cbd7e3] px-3 py-2 text-sm font-semibold">Log out</button></div> : <form onSubmit={onLogin} className="mt-5 grid gap-3 rounded-lg border border-[#dbe4ee] bg-white p-5"><input required aria-label="Email" type="email" value={email} onChange={(event) => onEmail(event.target.value)} placeholder="Email" className="rounded-md border border-[#cbd7e3] px-3 py-2" /><input required aria-label="Password" type="password" value={password} onChange={(event) => onPassword(event.target.value)} placeholder="Password" className="rounded-md border border-[#cbd7e3] px-3 py-2" /><button className="rounded-md bg-[#007f82] py-2 font-semibold text-white">Log in</button></form>}</section>;
}

function ProductGrid({ products, onAdd }: { products: Product[]; onAdd: (id: string) => void }) {
  return <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">{products.map((product) => <ProductCard key={product.id} product={product} onAdd={onAdd} />)}</div>;
}

function ProductCard({ product, onAdd }: { product: Product; onAdd: (id: string) => void }) {
  return <article className="overflow-hidden rounded-lg border border-[#dbe4ee] bg-white"><div className="relative aspect-[4/3] bg-[#e7f5f3]">{product.imageUrl ? <Image src={product.imageUrl} alt={product.name} fill sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw" className="object-cover" /> : null}<span className="absolute left-2 top-2 rounded bg-white px-2 py-1 text-xs font-semibold text-[#007f82]">In this cab</span></div><div className="p-4"><p className="text-xs font-semibold text-[#007f82]">{product.category.name}</p><h2 className="mt-1 font-bold">{product.name}</h2><p className="mt-2 line-clamp-2 text-sm text-[#64748b]">{product.description}</p><div className="mt-4 flex items-center justify-between gap-3"><strong>{money.format(product.price)}</strong><button onClick={() => onAdd(product.id)} className="rounded-md border border-[#007f82] px-2 py-1 text-sm font-semibold text-[#007f82]">Add</button></div></div></article>;
}

function BundleCard({ bundle, onAdd }: { bundle: Bundle; onAdd: (bundle: Bundle) => void }) {
  const total = bundle.products.reduce((sum, product) => sum + product.price, 0);
  return <article className="rounded-lg border border-[#dbe4ee] bg-white p-4"><p className="text-xs font-semibold text-[#007f82]">Cab bundle</p><h2 className="mt-1 font-bold">{bundle.name}</h2><p className="mt-2 text-sm text-[#64748b]">{bundle.note}</p><p className="mt-3 text-sm text-[#46566e]">{bundle.products.map((product) => product.name).join(" + ")}</p><div className="mt-4 flex items-center justify-between gap-3"><strong>{money.format(total)}</strong><button onClick={() => onAdd(bundle)} className="rounded-md bg-[#007f82] px-3 py-2 text-sm font-semibold text-white">Add bundle</button></div></article>;
}

function OrderTimeline({ status }: { status: Order["status"] }) {
  const progress = status === "PLACED" ? 0 : status === "CONFIRMED" ? 1 : status === "HANDED_OVER" ? 2 : 3;
  return <ol className="mt-5 grid grid-cols-4 gap-1 text-center text-[10px] font-semibold sm:text-xs">{["PLACED", "CONFIRMED", "HANDED_OVER", "COMPLETED"].map((step, index) => <li key={step} className={index <= progress ? "rounded bg-[#007f82] px-1 py-2 text-white transition-all duration-500" : "rounded bg-[#e7f5f3] px-1 py-2 text-[#075c64] transition-all duration-500"}>{step}</li>)}</ol>;
}

function PaymentProgress({ stage, method }: { stage: PaymentStage; method: PaymentMethod }) {
  const stages: { key: PaymentStage; label: string }[] = [{ key: "checkout", label: "Order secured" }, { key: "processing", label: `Processing ${paymentMethodLabel(method)}` }, { key: "paid", label: "Payment successful" }, { key: "driver-notified", label: "Driver notified" }, { key: "confirmed", label: "Order confirmed" }];
  const progress = stages.findIndex((item) => item.key === stage);
  return <ol aria-label="Mock payment progress" className="mt-5 space-y-2">{stages.map((item, index) => <li key={item.key} className={index <= progress ? "flex items-center gap-2 text-sm text-[#075c64] transition-all duration-500" : "flex items-center gap-2 text-sm text-[#94a3b8] transition-all duration-500"}>{index === progress && stage !== "confirmed" ? <LoaderCircle className="animate-spin" size={16} /> : index <= progress ? <CircleCheck size={16} /> : <span className="size-4 rounded-full border border-current" />}{item.label}</li>)}</ol>;
}

function paymentMethodLabel(method: PaymentMethod): string {
  return paymentMethods.find((item) => item.value === method)?.label ?? method;
}

function SectionTitle({ eyebrow, title, action, onAction }: { eyebrow: string; title: string; action?: string; onAction?: () => void }) {
  return <div className="flex items-end justify-between gap-4"><div><p className="text-sm font-semibold text-[#007f82]">{eyebrow}</p><h1 className="text-2xl font-bold">{title}</h1></div>{action && onAction ? <button onClick={onAction} className="text-sm font-semibold text-[#007f82]">{action}</button> : null}</div>;
}

function EmptyState({ title, text, action, onAction }: { title: string; text: string; action?: string; onAction?: () => void }) {
  return <section className="mx-auto mt-12 max-w-lg rounded-lg border border-dashed border-[#b6c4d5] bg-white p-8 text-center"><ShoppingBag className="mx-auto text-[#007f82]" /><h1 className="mt-3 text-xl font-bold">{title}</h1><p className="mt-2 text-sm leading-6 text-[#64748b]">{text}</p>{action && onAction ? <button onClick={onAction} className="mt-5 rounded-md bg-[#007f82] px-4 py-2 text-sm font-semibold text-white">{action}</button> : null}</section>;
}

function LoadingGrid() {
  return <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">{[1, 2, 3, 4].map((item) => <div key={item} className="h-72 animate-pulse rounded-lg bg-[#dbe4ee]" />)}</div>;
}

function createBundles(products: Product[]): Bundle[] {
  const find = (pattern: RegExp) => products.find((product) => pattern.test(`${product.name} ${product.description}`));
  const airport = [find(/neck pillow/i), find(/charg|cable|usb/i), find(/coffee|trail mix/i)].filter(Boolean) as Product[];
  const rain = [find(/umbrella/i), find(/coffee|trail mix/i)].filter(Boolean) as Product[];
  const charging = [find(/charger/i), find(/cable|usb/i)].filter(Boolean) as Product[];
  return [
    airport.length > 1 ? { name: "Airport Essentials", note: "Comfort, charging, and a quick refresh for the transfer.", products: airport } : null,
    charging.length > 1 ? { name: "Charging Kit", note: "Keep your phone connected for the rest of the ride.", products: charging } : null,
    rain.length > 1 ? { name: "Rainy Day Kit", note: "Compact weather cover with a ride-friendly snack.", products: rain } : null
  ].filter(Boolean) as Bundle[];
}

function mergeProducts(existing: Product[], incoming: Product[]): Product[] {
  const merged = new Map(existing.map((product) => [product.id, product]));
  incoming.forEach((product) => merged.set(product.id, product));
  return [...merged.values()];
}

function wait(milliseconds: number): Promise<void> {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds));
}
