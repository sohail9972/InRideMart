export type Category = {
  id: string;
  slug: string;
  name: string;
  description: string | null;
};

export type Product = {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  imageUrl: string | null;
  category: Category;
  createdAt: string;
};

export type ProductPage = {
  items: Product[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
};

const catalogApiBaseUrl = process.env.CATALOG_API_BASE_URL ?? "http://localhost:8083";

async function catalogFetch<T>(path: string): Promise<T> {
  const response = await fetch(`${catalogApiBaseUrl}${path}`, { cache: "no-store" });
  if (!response.ok) {
    throw new Error(`Catalog request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export function getCategories(): Promise<Category[]> {
  return catalogFetch<Category[]>("/api/v1/catalog/categories");
}

export function getProducts(search = ""): Promise<ProductPage> {
  return catalogFetch<ProductPage>(`/api/v1/catalog/products?size=12${search}`);
}
