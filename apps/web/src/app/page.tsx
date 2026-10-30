import { redirect } from "next/navigation";
import { cookies } from "next/headers";

export default async function IndexPage() {
  const cookieStore = await cookies();
  redirect(cookieStore.has("designdeck_access") ? "/home" : "/auth");
}
