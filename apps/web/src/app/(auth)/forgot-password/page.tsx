"use client";

import Link from "next/link";
import { useState } from "react";
import { ArrowLeft, Zap } from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await api.forgotPassword({ email });
      setSent(true);
      toast.success("Check your email for the reset link.");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Could not send reset link");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-5 py-8">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center gap-3 text-center">
          <div className="grid h-14 w-14 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-lg">
            <Zap className="h-7 w-7" strokeWidth={2.5} />
          </div>
          <h1 className="text-2xl font-black tracking-tight">Reset password</h1>
          <p className="text-sm text-muted-foreground">Enter your email and we will send you a reset link.</p>
        </div>
        {sent ? (
          <div className="space-y-4 rounded-xl border border-border bg-card p-5 text-center">
            <p className="text-sm">
              If an account exists for <span className="font-semibold">{email}</span>, a reset link is on its way.
            </p>
            <p className="text-xs text-muted-foreground">The link expires in 1 hour.</p>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <Button type="submit" disabled={loading} className="h-11 w-full text-base font-semibold">
              {loading ? "Sending..." : "Send reset link"}
            </Button>
          </form>
        )}
        <div className="mt-6 flex justify-center">
          <Link href="/auth" className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-4 w-4" />
            Back to sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
