import { createClient } from "npm:@supabase/supabase-js@2.57.4";
const json = (data: unknown, status = 200) => new Response(JSON.stringify(data), {status, headers: {"Content-Type": "application/json", "Cache-Control": "no-store"}});
Deno.serve(async (req) => {
  if (req.method !== "POST") return json({error:"Method not allowed"},405);
  const token = req.headers.get("Authorization") || "";
  if (!/^Bearer [A-Za-z0-9._~+/=-]{20,4096}$/.test(token)) return json({error:"Sign in required"},401);
  try {
    // Hostinger is the sole identity authority for this bridge. Never accept
    // email, role or user ID supplied by the Android client.
    const response = await fetch("https://yawarconsulting.com/api/v1/auth.php?action=me", {
      headers: {Authorization:token}, redirect:"error", signal:AbortSignal.timeout(10000)
    });
    if (!response.ok) return json({error:"Hostinger session expired. Sign in again."},401);
    const identity = await response.json();
    const user = identity.user;
    if (!identity.ok || !user || !Number.isSafeInteger(Number(user.id)) || Number(user.id)<1 || !user.email) {
      return json({error:"Invalid identity response"},401);
    }
    const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      {auth:{persistSession:false,autoRefreshToken:false}});
    const {data:link,error:linkError} = await admin.auth.admin.generateLink({type:"magiclink",email:user.email});
    if (linkError || !link?.user || !link.properties?.hashed_token) throw new Error("Could not create messaging identity");
    const uid=link.user.id;
    const mappedRole = user.role === "call_center" ? "yhcs" : user.role;
    if (!["patient","doctor","hospital","yhcs","admin"].includes(mappedRole)) return json({error:"Unsupported account role"},403);
    const {error:profileError} = await admin.from("care_profiles").upsert({
      id:uid,display_name:user.full_name || "",phone:user.phone || "",role:mappedRole,
      updated_at:new Date().toISOString()
    });
    if(profileError) throw new Error("Could not synchronize account profile");
    const {error:metadataError} = await admin.auth.admin.updateUserById(uid,{
      app_metadata:{hostinger_user_id:String(user.id),care_role:mappedRole}
    });
    if(metadataError) throw new Error("Could not synchronize identity");
    const {data:session,error:sessionError} = await admin.auth.verifyOtp({
      token_hash:link.properties.hashed_token,type:"magiclink"
    });
    if(sessionError || !session.session) throw new Error("Could not create messaging session");
    return json({access_token:session.session.access_token,refresh_token:session.session.refresh_token,
      expires_at:session.session.expires_at,user_id:uid});
  } catch (_) { return json({error:"Messaging sign-in is temporarily unavailable"},503); }
});
