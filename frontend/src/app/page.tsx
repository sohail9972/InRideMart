import { Storefront } from "@/components/storefront";
import { getCategories, getProducts } from "@/lib/catalog";

export const dynamic = "force-dynamic";

export default async function Home() {
  const { categories, productPage } = await loadCatalog();
  return <Storefront categories={categories} initialProducts={productPage} />;
}

async function loadCatalog() {
  try {
    const [categories, productPage] = await Promise.all([getCategories(), getProducts()]);
    return { categories, productPage };
  } catch {
    return { categories: [], productPage: null };
  }
}
