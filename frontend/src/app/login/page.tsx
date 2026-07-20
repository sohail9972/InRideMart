import { LoginForm } from "@/components/auth-forms";

function safeNextPath(value: string | undefined): string {
  return value && value.startsWith("/") && !value.startsWith("//") ? value : "/home";
}

export default async function LoginPage({ searchParams }: { searchParams: Promise<{ next?: string }> }) {
  const params = await searchParams;
  return <LoginForm nextPath={safeNextPath(params.next)} />;
}
