"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, Flame, Home, Search, Settings as SettingsIcon, Zap } from "lucide-react";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/home", label: "Home", icon: Home },
  { href: "/browse", label: "Browse", icon: Search },
  { href: "/progress", label: "Progress", icon: BarChart3 },
  { href: "/settings", label: "Settings", icon: SettingsIcon },
] as const;

export function AppNavigation() {
  const pathname = usePathname();

  return (
    <>
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-36 flex-col border-r border-border/80 bg-card/35 lg:flex">
        <Link href="/home" className="flex h-20 items-center gap-2 border-b border-border/80 px-5">
          <span className="relative flex h-9 w-9 items-center justify-center text-primary">
            <Zap className="h-8 w-8 fill-primary/15" strokeWidth={2.5} />
            <Flame className="absolute -right-0.5 bottom-0 h-4 w-4 fill-warning/20 text-warning" />
          </span>
          <span className="text-sm font-black tracking-tight">RapidSD</span>
        </Link>

        <nav className="flex-1 px-3 py-5" aria-label="Primary navigation">
          <ul className="space-y-2">
            {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
              const active = pathname === href || pathname.startsWith(`${href}/`);
              return (
                <li key={href}>
                  <Link
                    href={href}
                    aria-current={active ? "page" : undefined}
                    className={cn(
                      "flex items-center gap-3 rounded-xl px-3 py-3 text-xs font-semibold transition-colors",
                      active
                        ? "bg-primary/15 text-primary ring-1 ring-inset ring-primary/20"
                        : "text-muted-foreground hover:bg-accent hover:text-foreground",
                    )}
                  >
                    <Icon className="h-5 w-5 shrink-0" strokeWidth={active ? 2.5 : 2} />
                    {label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <div className="border-t border-border/60 px-4 py-4 text-center text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/60">
          Rapid-fire learning
        </div>
      </aside>

      <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-card/95 backdrop-blur supports-backdrop-filter:bg-card/80 lg:hidden">
        <ul className="mx-auto grid max-w-md grid-cols-4">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href || pathname.startsWith(`${href}/`);
            return (
              <li key={href}>
                <Link
                  href={href}
                  aria-current={active ? "page" : undefined}
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
    </>
  );
}
