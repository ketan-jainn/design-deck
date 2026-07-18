"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import { Suspense, useState } from "react";
import { api } from "@/lib/api";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";

export default function BrowsePage() {
  return (
    <Suspense fallback={null}>
      <BrowseContent />
    </Suspense>
  );
}

function BrowseContent() {
  const initialTopic = useSearchParams().get("topic") ?? undefined;
  const [q, setQ] = useState("");
  const [activeTopic, setActiveTopic] = useState<string | undefined>(initialTopic);
  const cats = useQuery({ queryKey: ["categories"], queryFn: api.categories });
  const questions = useQuery({ queryKey: ["questions-list", activeTopic, q], queryFn: () => api.questions({ topic: activeTopic, q }) });

  return (
    <div className="mx-auto max-w-md px-5 pt-8">
      <h1 className="mb-4 text-3xl font-black tracking-tight">Browse</h1>
      <div className="relative mb-4">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search questions" className="pl-9" />
      </div>
      <div className="-mx-5 mb-4 overflow-x-auto px-5">
        <div className="flex gap-2 whitespace-nowrap pb-1">
          <TopicChip active={!activeTopic} onClick={() => setActiveTopic(undefined)} color="var(--color-muted-foreground)" label="All" />
          {(cats.data ?? []).map((c) => (
            <TopicChip key={c.slug} active={activeTopic === c.slug} onClick={() => setActiveTopic(c.slug)} color={c.color} label={c.name} />
          ))}
        </div>
      </div>
      <ul className="space-y-2">
        {(questions.data ?? []).map((row) => (
          <li key={row.id}>
            <Link href={`/q/${row.id}`} className="block rounded-xl border border-border bg-card p-4 transition-colors hover:bg-accent">
              <div className="mb-2 flex items-center gap-2">
                {row.category && <Badge variant="outline" className="border-transparent px-2 py-0.5 text-[10px] font-bold" style={{ backgroundColor: `${row.category.color}22`, color: row.category.color }}>{row.category.name}</Badge>}
              </div>
              <p className="text-sm font-semibold leading-snug">{row.prompt}</p>
            </Link>
          </li>
        ))}
        {(questions.data ?? []).length === 0 && !questions.isLoading && <li className="py-8 text-center text-sm text-muted-foreground">No questions match.</li>}
      </ul>
    </div>
  );
}

function TopicChip({ active, onClick, color, label }: { active: boolean; onClick: () => void; color: string; label: string }) {
  return (
    <button onClick={onClick} className="flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs font-bold transition-colors" style={{ borderColor: active ? color : "var(--color-border)", backgroundColor: active ? `${color}22` : "transparent", color: active ? color : "var(--color-foreground)" }}>
      <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: color }} />
      {label}
    </button>
  );
}
