"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { readAccessToken } from "@/lib/auth-session";

export function AuthGate({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    queueMicrotask(() => {
      if (readAccessToken()) {
        setChecking(false);
        return;
      }
      router.replace(`/login?next=${encodeURIComponent(pathname || "/home")}`);
    });
  }, [pathname, router]);

  if (checking) {
    return <main className="grid min-h-screen place-items-center bg-[#f5f8fb] px-4"><p role="status" className="rounded-md border border-[#dbe4ee] bg-white px-4 py-3 text-sm text-[#46566e]">Checking your session...</p></main>;
  }

  return children;
}
