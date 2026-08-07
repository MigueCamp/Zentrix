"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/auth-store";

export default function RootPage() {
  const router = useRouter();
  const accessToken = useAuthStore((state) => state.accessToken);

  useEffect(() => {
    router.replace(accessToken ? "/dashboard" : "/login");
  }, [accessToken, router]);

  return null;
}
