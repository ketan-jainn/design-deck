"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { BookOpen, ChevronRight, Clock, Flame, Target, Zap } from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";

export default function HomePage() {
  const router = useRouter();
  const [size, setSize] = useState(10);
  const { data } = useQuery({ queryKey: ["progress-summary"], queryFn: api.progress });
  const goalPct = data ? Math.min(100, Math.round(((data.todayCount ?? 0) / (data.dailyGoal || 1)) * 100)) : 0;

  return (
    <div className="mx-auto max-w-6xl px-5 pt-8 lg:px-10 lg:pb-12 lg:pt-0">
      <header className="mb-6 flex items-center justify-between lg:-mx-10 lg:mb-10 lg:min-h-28 lg:border-b lg:border-border/80 lg:px-10">
        <div className="flex items-center gap-4">
          <div className="hidden h-12 w-12 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary lg:flex">
            <Zap className="h-7 w-7 fill-primary/10" strokeWidth={2.5} />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">Rapid-fire</p>
            <h1 className="text-3xl font-black tracking-tight lg:text-4xl">Today</h1>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 rounded-full bg-secondary px-3 py-1.5 lg:rounded-xl lg:border lg:border-border lg:bg-card lg:px-4 lg:py-3">
            <Flame className="h-4 w-4 text-warning" />
            <span className="text-sm font-bold">{data?.streak ?? 0}</span>
            <span className="hidden text-xs text-muted-foreground lg:inline">day streak</span>
          </div>
          <Card className="hidden w-64 border-primary/20 bg-linear-to-r from-primary/10 via-card to-card lg:block">
            <CardContent className="p-3.5">
              <div className="mb-2 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Target className="h-4 w-4 text-primary" />
                  <span className="text-xs font-semibold">Daily goal</span>
                </div>
                <span className="text-xs text-muted-foreground">{data?.todayCount ?? 0} / {data?.dailyGoal ?? 10}</span>
              </div>
              <Progress value={goalPct} className="h-1.5" />
            </CardContent>
          </Card>
        </div>
      </header>

      <Card className="border-primary/20 bg-linear-to-br from-primary/10 via-card to-card lg:hidden">
        <CardContent className="p-5">
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Target className="h-4 w-4 text-primary" />
              <span className="text-sm font-semibold">Daily goal</span>
            </div>
            <span className="text-sm text-muted-foreground">{data?.todayCount ?? 0} / {data?.dailyGoal ?? 10}</span>
          </div>
          <Progress value={goalPct} className="h-2" />
        </CardContent>
      </Card>

      <section className="mt-6 lg:mx-auto lg:mt-0 lg:max-w-xl">
        <h2 className="mb-3 text-sm font-bold uppercase tracking-wider text-muted-foreground">Start a session</h2>
        <div className="mb-3 grid grid-cols-3 gap-2 lg:gap-3">
          {[10, 20, 50].map((n) => (
            <button key={n} onClick={() => setSize(n)} className={`rounded-xl border-2 py-3 text-center transition-all lg:py-4 ${size === n ? "border-primary bg-primary/10 text-primary shadow-lg shadow-primary/10" : "border-border bg-card text-muted-foreground hover:border-muted-foreground/50 hover:text-foreground"}`}>
              <div className="text-xl font-black lg:text-2xl">{n}</div>
              <div className="text-[10px] font-medium uppercase tracking-wider">cards</div>
            </button>
          ))}
        </div>
        <Button className="h-14 w-full text-base font-bold shadow-lg shadow-primary/20" onClick={() => router.push(`/session?size=${size}`)}>
          <Zap className="mr-2 h-5 w-5" strokeWidth={2.5} />
          Start rapid-fire
        </Button>
        {(data?.dueCount ?? 0) > 0 && (
          <div className="mt-3 flex items-center gap-2 rounded-lg bg-secondary/50 px-3 py-2 text-sm">
            <Clock className="h-4 w-4 text-warning" />
            <span className="font-medium">{data?.dueCount} cards due for review</span>
          </div>
        )}
      </section>

      <div className="lg:mt-10 lg:grid lg:grid-cols-2 lg:gap-6">
        <TopicList title="Weakest topics" empty="Answer some cards to see this." items={data?.weakest ?? []} link />
        <TopicList title="Strongest topics" empty="Complete a few sessions to reveal your strengths." items={data?.strongest ?? []} />
      </div>
    </div>
  );
}

function TopicList({ title, empty, items, link }: { title: string; empty: string; items: NonNullable<Awaited<ReturnType<typeof api.progress>>["weakest"]>; link?: boolean }) {
  return (
    <section className="mt-8 lg:mt-0">
      <h2 className="mb-3 text-sm font-bold uppercase tracking-wider text-muted-foreground">{title}</h2>
      {items.length === 0 ? (
        <div className="flex min-h-36 flex-col items-center justify-center rounded-xl border border-border bg-card/60 px-6 text-center lg:min-h-44">
          <BookOpen className="mb-3 h-8 w-8 text-muted-foreground/30" />
          <p className="text-sm text-muted-foreground">{empty}</p>
        </div>
      ) : (
        <ul className="min-h-36 space-y-2 rounded-xl border border-border bg-card/40 p-2 lg:min-h-44">
          {items.map((c) => {
            const inner = (
              <>
                <div className="flex items-center gap-3">
                  <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: c.color }} />
                  <span className="font-semibold">{c.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-primary">{c.mastery}%</span>
                  {link && <ChevronRight className="h-4 w-4 text-muted-foreground" />}
                </div>
              </>
            );
            return <li key={c.slug}>{link ? <Link href={`/browse?topic=${c.slug}`} className="flex items-center justify-between rounded-lg border border-border/80 bg-card px-4 py-3 transition-colors hover:bg-accent">{inner}</Link> : <div className="flex items-center justify-between rounded-lg border border-border/80 bg-card px-4 py-3">{inner}</div>}</li>;
          })}
        </ul>
      )}
    </section>
  );
}
