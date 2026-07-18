"use client";

import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, ArrowRight, Loader2 } from "lucide-react";
import { api } from "@/lib/api";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function QuestionDetail({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const { id } = use(params);
  const { data, isLoading } = useQuery({ queryKey: ["question", id], queryFn: () => api.question(id) });
  if (isLoading) return <div className="flex min-h-screen items-center justify-center"><Loader2 className="h-6 w-6 animate-spin text-primary" /></div>;
  if (!data) return <div className="p-8 text-center">Not found</div>;
  const k = data.answerKey;
  return (
    <div className="mx-auto max-w-md px-5 pb-20 pt-6">
      <button onClick={() => router.back()} className="mb-4 flex items-center gap-1 text-sm text-muted-foreground">
        <ArrowLeft className="h-4 w-4" /> Back
      </button>
      <div className="mb-3 flex flex-wrap gap-2">
        {data.category && <Badge variant="outline" className="border-transparent px-2.5 py-1 text-[11px] font-bold" style={{ backgroundColor: `${data.category.color}22`, color: data.category.color }}>{data.category.name}</Badge>}
        <Badge variant="secondary" className="px-2.5 py-1 text-[11px] font-bold">{data.qtype}</Badge>
      </div>
      <h1 className="text-2xl font-bold leading-tight">{data.prompt}</h1>
      {k && <AnswerKeyCard answerKey={k} />}
      <Button className="mt-6 h-12 w-full font-bold" onClick={() => router.push("/session?size=10")}>Practice now</Button>
    </div>
  );
}

import { use } from "react";
import type { AnswerKey } from "@/lib/types";

function AnswerKeyCard({ answerKey: k }: { answerKey: AnswerKey }) {
  return (
    <div className="mt-6 space-y-4 rounded-2xl border border-primary/30 bg-primary/5 p-4">
      <div>
        <h3 className="mb-2 text-xs font-black uppercase tracking-widest text-primary">Answer key</h3>
        <ul className="space-y-1.5">
          {(k.bullets ?? []).map((b, i) => <li key={i} className="flex gap-2 text-[15px] leading-snug"><span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-primary" /><span>{b}</span></li>)}
        </ul>
      </div>
      {k.explanation && <p className="text-sm leading-relaxed text-muted-foreground">{k.explanation}</p>}
      {(k.followUps ?? []).length > 0 && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">Follow-ups</h4><ul className="space-y-1">{k.followUps.map((f, i) => <li key={i} className="flex gap-2 text-sm"><ArrowRight className="mt-0.5 h-3.5 w-3.5 shrink-0 text-warning" /><span>{f}</span></li>)}</ul></div>}
      {(k.commonMistakes ?? []).length > 0 && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">Common mistakes</h4><ul className="space-y-1">{k.commonMistakes.map((m, i) => <li key={i} className="text-sm text-muted-foreground">- {m}</li>)}</ul></div>}
      {k.whenNotToUse && <div><h4 className="mb-1 text-[11px] font-bold uppercase tracking-widest text-muted-foreground">When not to use</h4><p className="text-sm text-muted-foreground">{k.whenNotToUse}</p></div>}
    </div>
  );
}
