"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/services/api-client";
import { useAuthStore } from "@/store/auth-store";

export default function LoginPage() {
  const router = useRouter();
  const setSession = useAuthStore((state) => state.setSession);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { data } = await apiClient.post<{ accessToken: string }>("/auth/login", {
        email,
        password,
      });
      setSession(data.accessToken, email);
      router.push("/dashboard");
    } catch {
      setError("Credenciales inválidas");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 p-6">
        <h1 className="text-xl font-semibold">Zentrix</h1>
        <p className="text-sm text-black/60 dark:text-white/60">
          Consola de administración MDM
        </p>
        <div className="space-y-1">
          <label className="text-sm" htmlFor="email">Correo</label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded border border-black/10 dark:border-white/20 px-3 py-2 bg-transparent"
          />
        </div>
        <div className="space-y-1">
          <label className="text-sm" htmlFor="password">Contraseña</label>
          <input
            id="password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded border border-black/10 dark:border-white/20 px-3 py-2 bg-transparent"
          />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded bg-black text-white dark:bg-white dark:text-black py-2 text-sm font-medium disabled:opacity-50"
        >
          {loading ? "Ingresando..." : "Ingresar"}
        </button>
      </form>
    </div>
  );
}
