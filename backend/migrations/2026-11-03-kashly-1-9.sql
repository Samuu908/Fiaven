CREATE OR REPLACE FUNCTION public.fv_sync(p_token text, p_since bigint, p_changes jsonb)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'extensions'
AS $function$
declare
  me record; c jsonb; ex record; v_exists boolean; v_kind text; v_id text; v_upd bigint;
  rejected jsonb := '[]'::jsonb; forced jsonb := '[]'::jsonb; applied int := 0;
  v_count int; v_cursor bigint; v_records jsonb;
begin
  select * into me from fv_identity(p_token);
  if not found then return jsonb_build_object('error', 'Sesión inválida', 'status', 401); end if;
  perform pg_advisory_xact_lock(hashtext('fv:' || me.business_id));

  for c in select value from jsonb_array_elements(case when jsonb_typeof(p_changes) = 'array' then p_changes else '[]'::jsonb end) with ordinality as t(value, n) where n <= 500 loop
    v_kind := c->>'kind'; v_id := c->>'id';
    if v_kind is null or v_kind not in ('product','customer','sale','payment','setting','note','closing','quote','supplier','purchase','spayment','expense','order') then continue; end if;
    if v_id is null or length(v_id) = 0 or length(v_id) > 64 then continue; end if;
    if jsonb_typeof(c->'data') is distinct from 'string' or length(c->>'data') > 200000 then continue; end if;
    select * into ex from fv_records r where r.business_id = me.business_id and r.kind = v_kind and r.id = v_id;
    v_exists := found;
    if me.role <> 'owner' and (v_kind in ('product','setting') or (v_kind in ('sale','payment','closing','purchase','spayment','expense') and v_exists))
       and not (v_kind in ('sale','payment','expense') and v_exists and fv_is_void_only(ex.data, c->>'data', coalesce((c->>'deleted')::boolean, false))) then
      rejected := rejected || jsonb_build_array(jsonb_build_object('kind', v_kind, 'id', v_id));
      if v_exists then
        forced := forced || jsonb_build_array(jsonb_build_object('kind', ex.kind, 'id', ex.id, 'data', ex.data, 'updatedAt', ex.updated_at, 'deleted', ex.deleted, 'author', ex.author));
      end if;
      continue;
    end if;
    v_upd := coalesce(case when (c->>'updatedAt') ~ '^[0-9]+$' then (c->>'updatedAt')::bigint end, (extract(epoch from now()) * 1000)::bigint);
    if v_exists and ex.updated_at > v_upd then
      forced := forced || jsonb_build_array(jsonb_build_object('kind', ex.kind, 'id', ex.id, 'data', ex.data, 'updatedAt', ex.updated_at, 'deleted', ex.deleted, 'author', ex.author));
      continue;
    end if;
    insert into fv_records (business_id, kind, id, data, updated_at, deleted, author)
    values (me.business_id, v_kind, v_id, c->>'data', v_upd, coalesce((c->>'deleted')::boolean, false), me.username)
    on conflict (business_id, kind, id) do update set
      data = excluded.data, updated_at = excluded.updated_at, deleted = excluded.deleted, seq = nextval('public.fv_seq');
    applied := applied + 1;
  end loop;

  select count(*) into v_count from (
    select 1 from fv_records where business_id = me.business_id and seq > coalesce(p_since, 0) limit 1001) q;
  select coalesce(jsonb_agg(jsonb_build_object('kind', kind, 'id', id, 'data', data, 'updatedAt', updated_at,
           'deleted', deleted, 'author', author) order by seq), '[]'::jsonb), max(seq)
    into v_records, v_cursor
    from (select * from fv_records where business_id = me.business_id and seq > coalesce(p_since, 0) order by seq limit 1000) p;

  return jsonb_build_object(
    'cursor', coalesce(v_cursor, coalesce(p_since, 0)),
    'more', v_count > 1000,
    'records', v_records || forced,
    'rejected', rejected,
    'applied', applied
  );
end $function$;

-- Kashly 1.9: account changes, team edits, gastos (expense) and pedidos (order) records.
alter table public.fv_sessions drop constraint if exists fv_sessions_username_fkey;
alter table public.fv_sessions add constraint fv_sessions_username_fkey
  foreign key (username) references public.fv_users(username) on delete cascade on update cascade;

create or replace function public.fv_account_update(p_token text, p_password text, p_username text, p_display_name text, p_new_password text)
returns jsonb language plpgsql security definer set search_path to 'public', 'extensions' as $f$
declare
  me record; v_hash text; v_title text; v_user text := lower(btrim(coalesce(p_username, '')));
  v_name text := btrim(coalesce(p_display_name, '')); v_pass text := coalesce(p_new_password, ''); v_role text;
begin
  select * into me from fv_identity(p_token);
  if not found then return jsonb_build_object('error', 'Sesión inválida', 'status', 401); end if;
  select pass_hash, title, role into v_hash, v_title, v_role from fv_users where username = me.username;
  if v_hash <> crypt(coalesce(p_password, ''), v_hash) then
    return jsonb_build_object('error', 'La contraseña actual no es correcta', 'status', 403);
  end if;
  if v_user = '' then v_user := me.username; end if;
  if v_name = '' then v_name := me.display_name; end if;
  if length(v_name) < 2 or length(v_name) > 60 then return jsonb_build_object('error', 'El nombre debe tener de 2 a 60 letras', 'status', 400); end if;
  if v_user !~ '^[a-z0-9._&-]{3,32}$' then
    return jsonb_build_object('error', 'El usuario debe tener 3 a 32 letras, números, punto, guion o &', 'status', 400);
  end if;
  if v_user <> me.username and exists (select 1 from fv_users where username = v_user) then
    return jsonb_build_object('error', 'Ese usuario ya existe', 'status', 409);
  end if;
  if v_pass <> '' and (length(v_pass) < 6 or length(v_pass) > 100) then
    return jsonb_build_object('error', 'La contraseña nueva debe tener al menos 6 caracteres', 'status', 400);
  end if;
  update fv_users set username = v_user, display_name = v_name,
    pass_hash = case when v_pass <> '' then crypt(v_pass, gen_salt('bf', 10)) else pass_hash end
  where username = me.username;
  if v_user <> me.username then
    update fv_records set author = v_user where business_id = me.business_id and author = me.username;
    update fv_files set author = v_user where business_id = me.business_id and author = me.username;
    delete from fv_login_fails where username in (me.username, v_user);
  end if;
  if v_pass <> '' then delete from fv_sessions where username = v_user and token <> p_token; end if;
  return jsonb_build_object(
    'token', p_token,
    'user', jsonb_build_object('username', v_user, 'displayName', v_name, 'role', v_role, 'title', coalesce(v_title, '')),
    'business', jsonb_build_object('id', me.business_id, 'name', me.business_name)
  );
end $f$;

create or replace function public.fv_team_update(p_token text, p_username text, p_display_name text, p_new_password text)
returns jsonb language plpgsql security definer set search_path to 'public', 'extensions' as $f$
declare me record; t record; v_user text := lower(btrim(coalesce(p_username, '')));
  v_name text := btrim(coalesce(p_display_name, '')); v_pass text := coalesce(p_new_password, '');
begin
  select * into me from fv_identity(p_token);
  if not found then return jsonb_build_object('error', 'Sesión inválida', 'status', 401); end if;
  if me.role <> 'owner' then return jsonb_build_object('error', 'Solo el dueño puede gestionar el equipo', 'status', 403); end if;
  select * into t from fv_users where username = v_user and business_id = me.business_id;
  if not found then return jsonb_build_object('error', 'Usuario no encontrado', 'status', 404); end if;
  if v_name <> '' and (length(v_name) < 2 or length(v_name) > 60) then return jsonb_build_object('error', 'El nombre debe tener de 2 a 60 letras', 'status', 400); end if;
  if v_pass <> '' and t.username = me.username then return jsonb_build_object('error', 'Cambia tu propia contraseña en Mi cuenta', 'status', 400); end if;
  if v_pass <> '' and (length(v_pass) < 6 or length(v_pass) > 100) then return jsonb_build_object('error', 'La contraseña debe tener al menos 6 caracteres', 'status', 400); end if;
  update fv_users set
    display_name = case when v_name <> '' then v_name else display_name end,
    pass_hash = case when v_pass <> '' then crypt(v_pass, gen_salt('bf', 10)) else pass_hash end
  where username = t.username;
  if v_pass <> '' then delete from fv_sessions where username = t.username; end if;
  return jsonb_build_object('ok', true);
end $f$;
