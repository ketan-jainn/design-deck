"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, Home, Search, Settings as SettingsIcon } from "lucide-react";
import { cn } from "@/lib/utils";

const TABS = [
  { href: "/home", label: "Home", icon: Home },
  { href: "/browse", label: "Browse", icon: Search },
  { href: "/progress", label: "Progress", icon: BarChart3 },
  { href: "/settings", label: "Settings", icon: SettingsIcon },
] as const;

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const hideNav = pathname.startsWith("/session");
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <main className={cn("flex-1", !hideNav && "pb-20")}>{children}</main>
      {!hideNav && (
        <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/80">
          <ul className="mx-auto grid max-w-md grid-cols-4">
            {TABS.map(({ href, label, icon: Icon }) => {
              const active = pathname === href || pathname.startsWith(`${href}/`);
              return (
                <li key={href}>
                  <Link
                    href={href}
                    className={cn(
                      "flex flex-col items-center gap-1 py-2.5 text-[11px] font-medium transition-colors",
                      active ? "text-primary" : "text-muted-foreground",
                    )}
                  >
                    <Icon className={cn("h-5 w-5", active && "stroke-[2.5]")} />
                    {label}
                  </Link>
                </li>
              );
            })}
          </ul>
          <div style={{ height: "env(safe-area-inset-bottom)" }} />
        </nav>
      )}
    </div>
  );
}
