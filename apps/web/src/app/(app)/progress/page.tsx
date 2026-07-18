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
    <div className="mx-auto max-w-md px-5 pt-8">
      <h1 className="mb-6 text-3xl font-black tracking-tight">Progress</h1>
      <div className="grid grid-cols-3 gap-3">
        <Stat icon={<Target className="h-4 w-4" />} value={data?.accuracy ?? 0} suffix="%" label="Accuracy" />
        <Stat icon={<Flame className="h-4 w-4 text-warning" />} value={data?.streak ?? 0} label="Streak" />
        <Stat icon={<TrendingUp className="h-4 w-4 text-primary" />} value={data?.totalAnswered ?? 0} label="Answered" />
      </div>
      <section className="mt-8">
        <h2 className="mb-3 text-sm font-bold uppercase tracking-wider text-muted-foreground">Topic mastery</h2>
        <div className="space-y-3">
          {topics.map((c) => (
            <div key={c.slug}>
              <div className="mb-1.5 flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full" style={{ backgroundColor: c.color }} />
                  <span className="font-semibold">{c.name}</span>
                </div>
                <span className="text-xs font-bold tabular-nums">{c.mastery}%</span>
              </div>
              <Progress value={c.mastery} className="h-1.5" />
            </div>
          ))}
          {(data?.totalAnswered ?? 0) === 0 && <p className="text-sm text-muted-foreground">Answer cards to build mastery.</p>}
        </div>
      </section>
    </div>
  );
}

function Stat({ icon, value, suffix, label }: { icon: React.ReactNode; value: number; suffix?: string; label: string }) {
  return (
    <Card className="border-border">
      <CardContent className="flex flex-col items-start gap-1 p-3">
        <div className="text-muted-foreground">{icon}</div>
        <div className="text-2xl font-black tabular-nums">{value}{suffix}</div>
        <div className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">{label}</div>
      </CardContent>
    </Card>
  );
}
