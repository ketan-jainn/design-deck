"use client";

import { useQuery } from "@tanstack/react-query";
import { Flame, Target, TrendingUp } from "lucide-react";
import { api } from "@/lib/api";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";

export default function ProgressPage() {
  const { data } = useQuery({ queryKey: ["progress-summary"], queryFn: api.progress });
  const topics = [...(data?.strongest ?? []), ...(data?.weakest ?? [])].filter(
    (v, i, arr) => arr.findIndex((x) => x.slug === v.slug) === i,
  );
  return (
    <div className="mx-auto max-w-6xl px-5 pb-8 pt-8 lg:px-10 lg:pb-12 lg:pt-10">
      <div className="mb-6 lg:mb-8">
        <p className="mb-1 hidden text-xs font-semibold uppercase tracking-[0.18em] text-primary lg:block">Performance</p>
        <h1 className="text-3xl font-black tracking-tight lg:text-4xl">Progress</h1>
        <p className="mt-2 hidden text-sm text-muted-foreground lg:block">Track your consistency and topic mastery over time.</p>
      </div>

      <div className="grid grid-cols-3 gap-3 lg:gap-5">
        <Stat icon={<Target className="h-4 w-4" />} value={data?.accuracy ?? 0} suffix="%" label="Accuracy" />
        <Stat icon={<Flame className="h-4 w-4 text-warning" />} value={data?.streak ?? 0} label="Streak" />
        <Stat icon={<TrendingUp className="h-4 w-4 text-primary" />} value={data?.totalAnswered ?? 0} label="Answered" />
      </div>

      <section className="mt-8 rounded-2xl border border-border bg-card/40 lg:mt-10">
        <div className="border-b border-border px-5 py-4 lg:px-6">
          <h2 className="text-sm font-bold uppercase tracking-wider text-muted-foreground">Topic mastery</h2>
        </div>
        <div className="grid gap-x-10 gap-y-5 p-5 lg:grid-cols-2 lg:p-6">
          {topics.map((c) => (
            <div key={c.slug} className="rounded-xl border border-border/70 bg-card p-4">
              <div className="mb-3 flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full" style={{ backgroundColor: c.color }} />
                  <span className="font-semibold">{c.name}</span>
                </div>
                <span className="text-xs font-bold tabular-nums">{c.mastery}%</span>
              </div>
              <Progress value={c.mastery} className="h-1.5" />
            </div>
          ))}
          {(data?.totalAnswered ?? 0) === 0 && (
            <p className="col-span-full flex min-h-32 items-center justify-center text-sm text-muted-foreground">
              Answer cards to build mastery.
            </p>
          )}
        </div>
      </section>
    </div>
  );
}

function Stat({ icon, value, suffix, label }: { icon: React.ReactNode; value: number; suffix?: string; label: string }) {
  return (
    <Card className="border-border bg-linear-to-br from-card to-card/60">
      <CardContent className="flex flex-col items-start gap-1 p-3 lg:p-5">
        <div className="mb-1 text-muted-foreground lg:rounded-lg lg:bg-secondary lg:p-2">{icon}</div>
        <div className="text-2xl font-black tabular-nums lg:text-3xl">{value}{suffix}</div>
        <div className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">{label}</div>
      </CardContent>
    </Card>
  );
}
