"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { LogOut, Mail, SlidersHorizontal, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function SettingsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data } = useQuery({ queryKey: ["profile-self"], queryFn: api.me });
  const [goal, setGoal] = useState(10);
  const [displayName, setDisplayName] = useState("");
  const saveMutation = useMutation({
    mutationFn: api.updateMe,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["profile-self"] });
      toast.success("Saved");
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Save failed"),
  });

  useEffect(() => {
    if (data) {
      setGoal(data.dailyGoal ?? 10);
      setDisplayName(data.displayName ?? "");
    }
  }, [data]);

  async function signOut() {
    await api.logout();
    router.replace("/auth");
  }

  return (
    <div className="mx-auto max-w-5xl px-5 pb-8 pt-8 lg:px-10 lg:pb-12 lg:pt-10">
      <div className="mb-6 lg:mb-8">
        <p className="mb-1 hidden text-xs font-semibold uppercase tracking-[0.18em] text-primary lg:block">Account</p>
        <h1 className="text-3xl font-black tracking-tight lg:text-4xl">Settings</h1>
        <p className="mt-2 hidden text-sm text-muted-foreground lg:block">Manage your profile and practice preferences.</p>
      </div>

      <div className="grid gap-5 lg:grid-cols-[minmax(0,0.8fr)_minmax(0,1.4fr)]">
        <Card className="h-fit border-border bg-card/60">
          <CardContent className="p-5 lg:p-6">
            <div className="mb-6 flex items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-full border border-primary/20 bg-primary/10 text-primary">
                <UserRound className="h-6 w-6" />
              </div>
              <div className="min-w-0">
                <p className="truncate font-bold">{data?.displayName || "RapidSD learner"}</p>
                <p className="text-xs text-muted-foreground">Your account</p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-xl border border-border bg-background/30 p-3">
              <Mail className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
              <div className="min-w-0">
                <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Email</p>
                <p className="mt-1 truncate text-sm">{data?.email ?? "..."}</p>
              </div>
            </div>

            <Button variant="outline" className="mt-5 w-full" onClick={signOut}>
              <LogOut className="mr-2 h-4 w-4" /> Sign out
            </Button>
            <p className="mt-4 text-center text-[10px] font-medium uppercase tracking-widest text-muted-foreground/60">RapidSD v1</p>
          </CardContent>
        </Card>

        <Card className="border-border">
          <CardContent className="p-5 lg:p-6">
            <div className="mb-6 flex items-center gap-3 border-b border-border pb-4">
              <div className="rounded-lg bg-secondary p-2 text-primary">
                <SlidersHorizontal className="h-5 w-5" />
              </div>
              <div>
                <h2 className="font-bold">Practice preferences</h2>
                <p className="text-xs text-muted-foreground">Personalize your daily learning target.</p>
              </div>
            </div>

            <div className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="dn">Display name</Label>
                <Input id="dn" value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="goal">Daily goal (cards)</Label>
                <Input id="goal" type="number" min={1} max={100} value={goal} onChange={(e) => setGoal(Number(e.target.value))} />
                <p className="text-xs text-muted-foreground">Choose between 1 and 100 cards per day.</p>
              </div>
              <Button className="w-full font-semibold lg:w-auto lg:min-w-36" onClick={() => saveMutation.mutate({ displayName, dailyGoal: goal })} disabled={saveMutation.isPending}>
                {saveMutation.isPending ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
