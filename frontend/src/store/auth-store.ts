import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AuthState {
  accessToken: string | null;
  email: string | null;
  hasHydrated: boolean;
  setSession: (accessToken: string, email: string) => void;
  clearSession: () => void;
  setHasHydrated: () => void;
}

// hasHydrated tracks whether the persisted store finished loading from localStorage.
// AppShell must wait for it before redirecting to /login, or a hard page reload
// always bounces the user (accessToken briefly reads null before rehydration).
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      email: null,
      hasHydrated: false,
      setSession: (accessToken, email) => set({ accessToken, email }),
      clearSession: () => set({ accessToken: null, email: null }),
      setHasHydrated: () => set({ hasHydrated: true }),
    }),
    {
      name: "zentrix-auth",
      partialize: (state) => ({ accessToken: state.accessToken, email: state.email }),
      onRehydrateStorage: () => (state) => state?.setHasHydrated(),
    }
  )
);
