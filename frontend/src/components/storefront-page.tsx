import { Storefront, type Screen } from "@/components/storefront";
import { loadStorefrontData } from "@/lib/storefront-data";

export async function StorefrontPage({ initialScreen = "home" }: { initialScreen?: Screen }) {
  const { categories, productPage } = await loadStorefrontData();
  return <Storefront categories={categories} initialProducts={productPage} initialScreen={initialScreen} />;
}
