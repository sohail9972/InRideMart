import { AuthGate } from "@/components/auth-gate";
import { StorefrontPage } from "@/components/storefront-page";

export const dynamic = "force-dynamic";

export default function OrdersPage() {
  return <AuthGate><StorefrontPage initialScreen="orders" /></AuthGate>;
}
