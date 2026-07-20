import { getCategories, getProducts } from "@/lib/catalog";

export async function loadStorefrontData() {
  try {
    const [categories, productPage] = await Promise.all([getCategories(), getProducts()]);
    return { categories, productPage };
  } catch {
    return { categories: [], productPage: null };
  }
}
