import { NextResponse, type NextRequest } from "next/server";

const PRIVATE_PREFIXES = ["/home", "/session", "/browse", "/progress", "/settings"];

export function middleware(req: NextRequest) {
  const isPrivate = PRIVATE_PREFIXES.some((path) => req.nextUrl.pathname.startsWith(path));
  if (isPrivate && !req.cookies.has("rsd_access")) {
    return NextResponse.redirect(new URL("/auth", req.url));
  }
  return NextResponse.next();
}

export const config = {
  matcher: ["/home/:path*", "/session/:path*", "/browse/:path*", "/progress/:path*", "/settings/:path*"],
};
