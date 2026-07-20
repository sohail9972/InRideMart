import { CatalogBrowser } from "@/components/catalog-browser";
import { loadStorefrontData } from "@/lib/storefront-data";

export const dynamic = "force-dynamic";

export default async function CatalogPage() {
  const { categories, productPage } = await loadStorefrontData();
  return <CatalogBrowser categories={categories} initialProducts={productPage} />;
}
