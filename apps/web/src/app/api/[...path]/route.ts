import { NextRequest } from "next/server";
import { proxy } from "@/lib/server-api";

type Params = { params: Promise<{ path: string[] }> };

async function handler(req: NextRequest, { params }: Params) {
  const { path } = await params;
  return proxy(req, `/api/${path.join("/")}`);
}

export const GET = handler;
export const POST = handler;
export const PATCH = handler;
