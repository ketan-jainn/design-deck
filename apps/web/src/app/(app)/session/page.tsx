"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import { ArrowRight, Check, Eye, Loader2, RotateCcw, Sparkles, X } from "lucide-react";
import { Suspense, useEffect, useState } from "react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import type { AnswerKey, Grade, Question } from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Textarea } from "@/components/ui/textarea";

const QTYPE_LABEL: Record<string, string> = {
  recall: "Recall",
  scenario: "Scenario",
  why: "Why?",
  choose: "Choose best",
  proscons: "Pros / Cons",
  followup: "Follow-up",
};

export default function SessionPage() {
  return (
    <Suspense fallback={<div className="flex min-h-screen items-center justify-center"><Loader2 className="h-6 w-6 animate-spin text-primary" /></div>}>
      <SessionContent />
    </Suspense>
  );
}

function SessionContent() {
  const router = useRouter();
  const size = Math.min(50, Math.max(5, Number(useSearchParams().get("size") ?? 10)));
  const [idx, setIdx] = useState(0);
  const [revealed, setRevealed] = useState(false);
  const [answer, setAnswer] = useState("");
  const [grade, setGrade] = useState<Grade | null>(null);
  const [stats, setStats] = useState({ got: 0, missed: 0 });
  const { data, isLoading } = useQuery({ queryKey: ["session", size], queryFn: () => api.startSession({ size }), staleTime: Infinity, refetchOnWindowFocus: false });
  const cards = data?.questions ?? [];
  const card = cards[idx];
  const submit = useMutation({ mutationFn: api.submitAttempt });
  const grader = useMutation({
    mutationFn: api.grade,
    onSuccess: setGrade,
    onError: (error) => toast.error(error instanceof Error ? error.message : "AI grading failed"),
  });

  useEffect(() => {
    setRevealed(false);
    setAnswer("");
    setGrade(null);
  }, [idx]);

  function handleResult(result: "got" | "missed") {
    if (!card) return;
    submit.mutate({ questionId: card.id, selfRating: result, userAnswer: answer || undefined, aiScore: grade?.score, aiFeedback: grade ?? undefined });
    setStats((s) => ({ ...s, [result]: s[result] + 1 }));
    setIdx((n) => n + 1);
  }

  if (isLoading) return <div className="flex min-h-screen items-center justify-center"><Loader2 className="h-6 w-6 animate-spin text-primary" /></div>;
  if (!card) return <SessionSummary stats={stats} total={cards.length} onExit={() => router.push("/home")} />;

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col px-5 pt-4">
      <div className="flex items-center gap-3 pt-2">
        <button onClick={() => router.push("/home")} className="grid h-9 w-9 place-items-center rounded-full bg-secondary text-muted-foreground" aria-label="Exit session">
          <X className="h-4 w-4" />
        </button>
        <Progress value={(idx / cards.length) * 100} className="h-2 flex-1" />
        <span className="text-xs font-bold tabular-nums text-muted-foreground">{idx + 1} / {cards.length}</span>
      </div>
      <div className="mt-6 flex-1">
        <div className="mb-4 flex flex-wrap items-center gap-2">
          {card.category && <Badge variant="outline" className="border-transparent px-2.5 py-1 text-[11px] font-bold" style={{ backgroundColor: `${card.category.color}22`, color: card.category.color }}>{card.category.name}</Badge>}
          <Badge variant="secondary" className="px-2.5 py-1 text-[11px] font-bold">{QTYPE_LABEL[card.qtype] ?? card.qtype}</Badge>
        </div>
        <h2 className="text-[22px] font-bold leading-tight tracking-tight">{card.prompt}</h2>
        {!revealed && (
          <div className="mt-6">
            <label className="mb-2 block text-xs font-bold uppercase tracking-wider text-muted-foreground">Answer (optional, AI will grade)</label>
            <Textarea placeholder="Type your answer or just think it, then reveal..." value={answer} onChange={(e) => setAnswer(e.target.value)} className="min-h-32 resize-none text-base" />
            {answer.length > 20 && (
              <Button variant="outline" className="mt-2 w-full" disabled={grader.isPending} onClick={() => grader.mutate({ questionId: card.id, userAnswer: answer })}>
                {grader.isPending ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Grading...</> : <><Sparkles className="mr-2 h-4 w-4" /> Grade with AI</>}
              </Button>
            )}
          </div>
        )}
        {grade && <GradeCard grade={grade} />}
        {revealed && card.answerKey && <AnswerKeyCard answerKey={card.answerKey} />}
      </div>
      <div className="sticky bottom-0 -mx-5 border-t border-border bg-background/95 px-5 py-4 pb-6 backdrop-blur">
        {!revealed ? (
          <Button className="h-14 w-full text-base font-bold" variant="secondary" onClick={() => setRevealed(true)}>
            <Eye className="mr-2 h-5 w-5" /> Reveal answer
          </Button>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            <Button className="h-14 border-destructive/40 bg-destructive/10 text-destructive hover:bg-destructive/20" variant="outline" onClick={() => handleResult("missed")}><RotateCcw className="mr-2 h-5 w-5" strokeWidth={2.5} /><span className="font-bold">Missed</span></Button>
            <Button className="h-14 font-bold shadow-md shadow-primary/20" onClick={() => handleResult("got")}><Check className="mr-2 h-5 w-5" strokeWidth={2.5} />Got it</Button>
          </div>
        )}
      </div>
    </div>
  );
}

function AnswerKeyCard({ answerKey: k }: { answerKey: AnswerKey }) {
  return (
    <div className="mt-6 space-y-4 rounded-2xl border border-primary/30 bg-primary/5 p-4">
      <div><h3 className="mb-2 text-xs font-black uppercase tracking-widest text-primary">Answer key</h3><ul className="space-y-1.5">{(k.bullets ?? []).map((b, i) => <li key={i} className="flex gap-2 text-[15px] leading-snug"><span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-primary" /><span>{b}</span></li>)}</ul></div>
      {k.explanation && <p className="text-sm leading-relaxed text-muted-foreground">{k.explanation}</p>}
      {(k.followUps ?? []).length > 0 && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">Interviewer follow-ups</h4><ul className="space-y-1">{k.followUps.map((f, i) => <li key={i} className="flex gap-2 text-sm"><ArrowRight className="mt-0.5 h-3.5 w-3.5 shrink-0 text-warning" /><span>{f}</span></li>)}</ul></div>}
      {(k.commonMistakes ?? []).length > 0 && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">Common mistakes</h4><ul className="space-y-1">{k.commonMistakes.map((m, i) => <li key={i} className="text-sm text-muted-foreground">- {m}</li>)}</ul></div>}
      {k.whenNotToUse && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">When not to use</h4><p className="text-sm text-muted-foreground">{k.whenNotToUse}</p></div>}
    </div>
  );
}

function GradeCard({ grade }: { grade: Grade }) {
  const tone = grade.score >= 85 ? "text-primary" : grade.score >= 60 ? "text-warning" : "text-destructive";
  return (
    <div className="mt-4 space-y-3 rounded-2xl border border-border bg-card p-4">
      <div className="flex items-center justify-between"><div className="flex items-center gap-2"><Sparkles className="h-4 w-4 text-primary" /><span className="text-xs font-black uppercase tracking-widest">AI grade</span></div><span className={`text-3xl font-black tabular-nums ${tone}`}>{grade.score}</span></div>
      {grade.summary && <p className="text-sm">{grade.summary}</p>}
      {grade.missing.length > 0 && <FeedbackList label="Missing" items={grade.missing} tone="text-warning" />}
      {grade.wrong.length > 0 && <FeedbackList label="Incorrect" items={grade.wrong} tone="text-destructive" />}
      {grade.improvements.length > 0 && <FeedbackList label="Improve" items={grade.improvements} tone="text-primary" />}
    </div>
  );
}

function FeedbackList({ label, items, tone }: { label: string; items: string[]; tone: string }) {
  return <div><h4 className={`mb-1 text-[11px] font-bold uppercase tracking-widest ${tone}`}>{label}</h4><ul className="space-y-1">{items.map((it, i) => <li key={i} className="text-sm">- {it}</li>)}</ul></div>;
}

function SessionSummary({ stats, total, onExit }: { stats: { got: number; missed: number }; total: number; onExit: () => void }) {
  const pct = total === 0 ? 0 : Math.round((stats.got / total) * 100);
  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col items-center justify-center px-5 text-center">
      <div className="mb-4 grid h-20 w-20 place-items-center rounded-full bg-primary text-primary-foreground"><Check className="h-10 w-10" strokeWidth={3} /></div>
      <h1 className="text-3xl font-black">Session complete</h1>
      <p className="mt-2 text-muted-foreground">{stats.got} of {total} correct</p>
      <div className="mt-6 w-full max-w-xs"><div className="text-6xl font-black text-primary">{pct}%</div><p className="mt-1 text-xs font-bold uppercase tracking-widest text-muted-foreground">Accuracy</p></div>
      <Button className="mt-8 h-12 w-full max-w-xs font-bold" onClick={onExit}>Back to home</Button>
    </div>
  );
}
