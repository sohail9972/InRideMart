import { AuthGate } from "@/components/auth-gate";
import { StorefrontPage } from "@/components/storefront-page";

export const dynamic = "force-dynamic";

export default function CartPage() {
  return <AuthGate><StorefrontPage initialScreen="cart" /></AuthGate>;
}
