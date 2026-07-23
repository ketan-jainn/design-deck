"use client";

import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useMemo, useState } from "react";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";

const PAGE_SIZE = 8;

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
  const [page, setPage] = useState(1);
  const cats = useQuery({ queryKey: ["categories"], queryFn: api.categories, staleTime: Infinity, refetchOnWindowFocus: false });
  const questions = useQuery({
    queryKey: ["questions-list", activeTopic],
    queryFn: () => api.questions({ topic: activeTopic }),
    staleTime: Infinity,
    refetchOnWindowFocus: false,
  });
  const filtered = useMemo(() => {
    const needle = q.trim().toLowerCase();
    if (!needle) return questions.data ?? [];
    return (questions.data ?? []).filter((row) => row.prompt.toLowerCase().includes(needle));
  }, [questions.data, q]);
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const visiblePages = paginationRange(page, totalPages);

  useEffect(() => {
    setPage(1);
  }, [q, activeTopic]);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  return (
    <div className="mx-auto max-w-6xl px-5 pb-8 pt-8 lg:px-10 lg:pb-12 lg:pt-10">
      <div className="mb-5 lg:mb-7">
        <p className="mb-1 hidden text-xs font-semibold uppercase tracking-[0.18em] text-primary lg:block">Question library</p>
        <h1 className="text-3xl font-black tracking-tight lg:text-4xl">Browse <span className="hidden lg:inline">system design questions</span></h1>
        <p className="mt-2 hidden text-sm text-muted-foreground lg:block">Explore engineering concepts, patterns, and trade-offs.</p>
      </div>

      <div className="lg:grid lg:grid-cols-[12rem_minmax(0,1fr)] lg:gap-7">
        <aside className="hidden lg:block">
          <h2 className="mb-3 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">Filter by topic</h2>
          <div className="space-y-1.5">
            <DesktopTopicButton active={!activeTopic} onClick={() => setActiveTopic(undefined)} color="var(--color-muted-foreground)" label="All questions" />
            {(cats.data ?? []).map((category) => (
              <DesktopTopicButton
                key={category.slug}
                active={activeTopic === category.slug}
                onClick={() => setActiveTopic(category.slug)}
                color={category.color}
                label={category.name}
              />
            ))}
          </div>
        </aside>

        <div className="min-w-0">
          <div className="relative mb-4">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search questions, patterns, and principles..." className="h-10 bg-card/50 pl-9" />
          </div>

          <div className="-mx-5 mb-4 overflow-hidden lg:hidden">
            <div className="topic-chip-scroll px-5">
              <div className="flex w-max gap-2">
                <TopicChip active={!activeTopic} onClick={() => setActiveTopic(undefined)} color="var(--color-muted-foreground)" label="All" />
                {(cats.data ?? []).map((category) => (
                  <TopicChip key={category.slug} active={activeTopic === category.slug} onClick={() => setActiveTopic(category.slug)} color={category.color} label={category.name} />
                ))}
              </div>
            </div>
          </div>

          <ul className="grid gap-2.5 lg:grid-cols-2">
            {paginated.map((row) => (
              <li key={row.id}>
                <Link href={`/q/${row.id}`} className="group flex h-full min-h-32 flex-col rounded-xl border border-border bg-card p-4 transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:bg-accent hover:shadow-lg hover:shadow-black/10">
                  <div className="mb-3 flex items-center gap-2">
                    {row.category && <Badge variant="outline" className="border-transparent px-2 py-0.5 text-[10px] font-bold" style={{ backgroundColor: `${row.category.color}22`, color: row.category.color }}>{row.category.name}</Badge>}
                    {row.category && <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: row.category.color }} />}
                  </div>
                  <p className="line-clamp-3 text-sm font-semibold leading-snug group-hover:text-primary">{row.prompt}</p>
                  <div className="mt-auto pt-3 text-[10px] font-medium uppercase tracking-wide text-muted-foreground">
                    {row.qtype.replaceAll("_", " ")} <span className="px-1.5">·</span> {row.difficulty}
                  </div>
                </Link>
              </li>
            ))}
            {filtered.length === 0 && !questions.isLoading && (
              <li className="col-span-full flex min-h-52 items-center justify-center rounded-xl border border-dashed border-border bg-card/30 text-sm text-muted-foreground">
                No questions match your filters.
              </li>
            )}
          </ul>

          {filtered.length > PAGE_SIZE && (
            <Pagination className="mt-7">
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    href="#"
                    aria-disabled={page === 1}
                    className={page === 1 ? "pointer-events-none opacity-40" : undefined}
                    onClick={(event) => {
                      event.preventDefault();
                      setPage((current) => Math.max(1, current - 1));
                    }}
                  />
                </PaginationItem>
                {visiblePages.map((item, index) => item === "ellipsis" ? (
                  <PaginationItem key={`ellipsis-${index}`}><PaginationEllipsis /></PaginationItem>
                ) : (
                  <PaginationItem key={item}>
                    <PaginationLink
                      href="#"
                      isActive={page === item}
                      onClick={(event) => {
                        event.preventDefault();
                        setPage(item);
                      }}
                    >
                      {item}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                <PaginationItem>
                  <PaginationNext
                    href="#"
                    aria-disabled={page === totalPages}
                    className={page === totalPages ? "pointer-events-none opacity-40" : undefined}
                    onClick={(event) => {
                      event.preventDefault();
                      setPage((current) => Math.min(totalPages, current + 1));
                    }}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </div>
      </div>
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

function DesktopTopicButton({ active, onClick, color, label }: { active: boolean; onClick: () => void; color: string; label: string }) {
  return (
    <button
      onClick={onClick}
      className={`flex w-full items-center gap-2 rounded-lg border px-3 py-2 text-left text-xs font-semibold transition-colors ${active ? "border-primary/30 bg-primary/15 text-primary" : "border-transparent text-muted-foreground hover:border-border hover:bg-card hover:text-foreground"}`}
    >
      <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: color }} />
      <span className="truncate">{label}</span>
    </button>
  );
}

function paginationRange(current: number, total: number): Array<number | "ellipsis"> {
  if (total <= 5) return Array.from({ length: total }, (_, index) => index + 1);

  const pages = new Set([1, total, current - 1, current, current + 1]);
  const validPages = [...pages].filter((value) => value > 0 && value <= total).sort((a, b) => a - b);
  const result: Array<number | "ellipsis"> = [];

  validPages.forEach((value, index) => {
    if (index > 0 && value - validPages[index - 1] > 1) result.push("ellipsis");
    result.push(value);
  });

  return result;
}
