"use client";

import Image from "next/image";
import { MapPin, Search, ShoppingBag, SlidersHorizontal, Sparkles } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import type { Category, Product, ProductPage } from "@/lib/catalog";

type CatalogBrowserProps = {
  categories: Category[];
  initialProducts: ProductPage | null;
};

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0
});

export function CatalogBrowser({ categories, initialProducts }: CatalogBrowserProps) {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [budget, setBudget] = useState("");
  const [sort, setSort] = useState("RELEVANCE");
  const [productPage, setProductPage] = useState<ProductPage | null>(initialProducts);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(initialProducts ? "" : "Catalog service is unavailable. Start catalog-service and refresh this page.");

  const params = useMemo(() => {
    const value = new URLSearchParams();
    if (query.trim()) value.set("query", query.trim());
    if (category) value.set("category", category);
    if (budget) value.set("maxPrice", budget);
    if (sort !== "RELEVANCE") value.set("sort", sort);
    return value.toString();
  }, [budget, category, query, sort]);

  useEffect(() => {
    const timer = window.setTimeout(async () => {
      setLoading(true);
      try {
        const response = await fetch(`/api/catalog/products?size=12&${params}`);
        if (!response.ok) throw new Error("Catalog request failed");
        setProductPage((await response.json()) as ProductPage);
        setError("");
      } catch {
        setError("Catalog service is unavailable. Start catalog-service and refresh this page.");
      } finally {
        setLoading(false);
      }
    }, 250);
    return () => window.clearTimeout(timer);
  }, [params]);

  const products = productPage?.items ?? [];

  return (
    <main className="min-h-screen">
      <header className="border-b border-[#dbe4ee] bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="grid size-10 place-items-center rounded-lg bg-[#007f82] text-white"><ShoppingBag size={21} /></div>
            <div>
              <p className="text-lg font-bold text-[#172033]">InRideMart</p>
              <p className="text-xs text-[#64748b]">In-ride marketplace</p>
            </div>
          </div>
          <div className="hidden items-center gap-2 text-sm text-[#64748b] sm:flex"><MapPin size={16} /> Bengaluru ride</div>
        </div>
      </header>

      <section className="border-b border-[#dbe4ee] bg-[#e7f5f3]">
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
          <div className="flex items-center gap-2 text-sm font-semibold text-[#065b64]"><Sparkles size={17} /> Ride picks</div>
          <h1 className="mt-2 text-3xl font-bold text-[#172033] sm:text-4xl">Find something useful for the road.</h1>
          <p className="mt-2 max-w-2xl text-base text-[#46566e]">Browse essentials, refreshments, and travel tech selected for the moments between pickup and destination.</p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
        <div className="grid gap-3 rounded-lg border border-[#dbe4ee] bg-white p-3 sm:grid-cols-[1fr_170px_170px]">
          <label className="flex items-center gap-2 rounded-md border border-[#cbd7e3] bg-white px-3 py-2 text-[#64748b]">
            <Search size={18} />
            <input className="min-w-0 flex-1 border-0 bg-transparent text-[#172033] outline-none" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search products" />
          </label>
          <label className="flex items-center gap-2 rounded-md border border-[#cbd7e3] px-3 py-2 text-[#64748b]">
            <SlidersHorizontal size={17} />
            <input className="min-w-0 w-full border-0 bg-transparent text-[#172033] outline-none" inputMode="numeric" min="0" type="number" value={budget} onChange={(event) => setBudget(event.target.value)} placeholder="Budget up to INR" />
          </label>
          <select className="rounded-md border border-[#cbd7e3] bg-white px-3 py-2 text-[#172033]" value={sort} onChange={(event) => setSort(event.target.value)} aria-label="Sort products">
            <option value="RELEVANCE">Recommended</option>
            <option value="PRICE_ASC">Price: low to high</option>
            <option value="PRICE_DESC">Price: high to low</option>
            <option value="NEWEST">Newest</option>
          </select>
        </div>

        <div className="mt-5 flex gap-2 overflow-x-auto pb-1">
          <button className={`shrink-0 rounded-md border px-3 py-2 text-sm font-medium ${category === "" ? "border-[#007f82] bg-[#007f82] text-white" : "border-[#cbd7e3] bg-white text-[#334155]"}`} onClick={() => setCategory("")}>All products</button>
          {categories.map((item) => <button key={item.id} className={`shrink-0 rounded-md border px-3 py-2 text-sm font-medium ${category === item.slug ? "border-[#007f82] bg-[#007f82] text-white" : "border-[#cbd7e3] bg-white text-[#334155]"}`} onClick={() => setCategory(item.slug)}>{item.name}</button>)}
        </div>

        <div className="mt-7 flex items-center justify-between gap-3">
          <h2 className="text-xl font-bold text-[#172033]">Catalog</h2>
          <p className="text-sm text-[#64748b]">{loading ? "Updating results" : `${productPage?.totalItems ?? 0} products`}</p>
        </div>

        {error ? <p className="mt-4 rounded-md border border-[#f2b8a8] bg-[#fff4f0] px-4 py-3 text-sm text-[#9a3412]">{error}</p> : null}

        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {products.map((product) => <ProductCard key={product.id} product={product} />)}
        </div>

        {!loading && !error && products.length === 0 ? <p className="mt-8 text-center text-[#64748b]">No products match these filters.</p> : null}
      </section>
    </main>
  );
}

function ProductCard({ product }: { product: Product }) {
  return (
    <article className="overflow-hidden rounded-lg border border-[#dbe4ee] bg-white">
      <div className="relative aspect-[4/3] bg-[#eaf0f6]">
        {product.imageUrl ? <Image src={product.imageUrl} alt={product.name} fill sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw" className="object-cover" /> : null}
      </div>
      <div className="p-4">
        <p className="text-xs font-semibold text-[#007f82]">{product.category.name}</p>
        <h3 className="mt-1 text-base font-bold text-[#172033]">{product.name}</h3>
        <p className="mt-2 line-clamp-2 text-sm leading-5 text-[#64748b]">{product.description}</p>
        <p className="mt-4 text-lg font-bold text-[#172033]">{currency.format(product.price)}</p>
      </div>
    </article>
  );
}
