import { AuthGate } from "@/components/auth-gate";
import { StorefrontPage } from "@/components/storefront-page";

export const dynamic = "force-dynamic";

export default function ProfilePage() {
  return <AuthGate><StorefrontPage initialScreen="profile" /></AuthGate>;
}
