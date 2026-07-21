import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";
const ACCESS = "rsd_access";
const REFRESH = "rsd_refresh";

export async function proxy(req: NextRequest, upstreamPath: string) {
  const body = req.method === "GET" ? undefined : await req.text();
  const cookieStore = await cookies();
  const access = cookieStore.get(ACCESS)?.value ?? req.cookies.get(ACCESS)?.value;
  const headers: HeadersInit = {
    "content-type": req.headers.get("content-type") ?? "application/json",
  };
  if (access) headers.authorization = `Bearer ${access}`;

  const url = new URL(upstreamPath, API_BASE_URL);
  req.nextUrl.searchParams.forEach((value, key) => url.searchParams.set(key, value));

  const res = await fetch(url, { method: req.method, headers, body, cache: "no-store" });
  const text = await res.text();
  const out = new NextResponse(text, {
    status: res.status,
    headers: { "content-type": res.headers.get("content-type") ?? "application/json" },
  });

  const auth = safeJson(text) as { accessToken?: string; refreshToken?: string } | null;
  if (res.ok && auth?.accessToken) {
    out.cookies.set(ACCESS, auth.accessToken, cookieOptions(15 * 60));
    if (auth.refreshToken) out.cookies.set(REFRESH, auth.refreshToken, cookieOptions(30 * 24 * 60 * 60));
  }
  if (upstreamPath === "/api/auth/logout") {
    out.cookies.delete(ACCESS);
    out.cookies.delete(REFRESH);
  }
  return out;
}

export function route(upstreamPath: string) {
  return (req: NextRequest) => proxy(req, upstreamPath);
}

function cookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.COOKIE_SECURE === "true",
    path: "/",
    maxAge,
  };
}

function safeJson(text: string) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}
