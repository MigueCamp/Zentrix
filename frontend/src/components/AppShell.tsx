"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuthStore } from "@/store/auth-store";
import { NAV_LINKS } from "./nav-links";

export function AppShell({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { email, accessToken, hasHydrated, clearSession } = useAuthStore();

  useEffect(() => {
    if (hasHydrated && !accessToken) {
      router.replace("/login");
    }
  }, [hasHydrated, accessToken, router]);

  if (!hasHydrated || !accessToken) {
    return null;
  }

  return (
    <div className="flex min-h-screen">
      <aside className="w-60 shrink-0 border-r border-black/10 dark:border-white/10 p-4">
        <p className="font-semibold mb-4">Zentrix</p>
        <nav className="flex flex-col gap-1">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className="text-sm rounded px-2 py-1.5 hover:bg-black/5 dark:hover:bg-white/10"
            >
              {link.label}
            </Link>
          ))}
        </nav>
      </aside>
      <div className="flex-1 flex flex-col">
        <header className="flex items-center justify-between border-b border-black/10 dark:border-white/10 px-6 py-3">
          <span className="text-sm text-black/60 dark:text-white/60">{email}</span>
          <button
            onClick={() => {
              clearSession();
              router.replace("/login");
            }}
            className="text-sm rounded px-3 py-1.5 border border-black/10 dark:border-white/10 hover:bg-black/5 dark:hover:bg-white/10"
          >
            Cerrar sesión
          </button>
        </header>
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
