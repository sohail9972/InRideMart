import { AiShopping } from "@/components/ai-shopping";
import { AuthGate } from "@/components/auth-gate";

export default function AiPage() { return <AuthGate><AiShopping /></AuthGate>; }
