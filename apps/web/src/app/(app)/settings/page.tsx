"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { LogOut } from "lucide-react";
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
    <div className="mx-auto max-w-md px-5 pt-8">
      <h1 className="mb-6 text-3xl font-black tracking-tight">Settings</h1>
      <Card>
        <CardContent className="space-y-4 p-5">
          <div className="space-y-2">
            <Label>Email</Label>
            <p className="text-sm text-muted-foreground">{data?.email ?? "..."}</p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="dn">Display name</Label>
            <Input id="dn" value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="goal">Daily goal (cards)</Label>
            <Input id="goal" type="number" min={1} max={100} value={goal} onChange={(e) => setGoal(Number(e.target.value))} />
          </div>
          <Button className="w-full font-semibold" onClick={() => saveMutation.mutate({ displayName, dailyGoal: goal })} disabled={saveMutation.isPending}>
            {saveMutation.isPending ? "Saving..." : "Save"}
          </Button>
        </CardContent>
      </Card>
      <Button variant="outline" className="mt-4 w-full" onClick={signOut}>
        <LogOut className="mr-2 h-4 w-4" /> Sign out
      </Button>
      <p className="mt-6 text-center text-xs text-muted-foreground">RapidSD v1</p>
    </div>
  );
}
