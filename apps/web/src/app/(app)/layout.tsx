"use client";

import { usePathname } from "next/navigation";
import { AppNavigation } from "@/components/app-navigation";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const hideNav = pathname.startsWith("/session");

  if (hideNav) {
    return <main className="min-h-screen bg-background">{children}</main>;
  }

  return (
    <div className="min-h-screen bg-background lg:pl-36">
      <AppNavigation />
      <main className="min-h-screen overflow-x-hidden pb-20 lg:pb-0">{children}</main>
    </div>
  );
}
